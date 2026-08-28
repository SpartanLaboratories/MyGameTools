package com.spartanlabs.gaming.networking

// Organizational
// Spartan Gaming
import com.spartanlabs.gaming.gameobjects.VisibleObject
import com.spartanlabs.gaming.gameobjects.VisibleObjectSnapshot

// Spartan Laboratories
import com.spartanlabs.webtools.MultiConnectionUDPServer
import com.spartanlabs.webtools.UDPConnection

// Intended Function
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.util.concurrent.ConcurrentHashMap

// Programming Generics (Tests, Logging, Profiling)
import org.slf4j.LoggerFactory
import org.slf4j.Logger

/** Shared slf4j logger for the game networking layer. */
private val log: Logger = LoggerFactory.getLogger("GameServer")

/**
 * A [MultiConnectionUDPServer] that speaks the game's protocol.
 *
 * The base class does all of the socket work: it listens on
 * [MultiConnectionUDPServer.COMMON_LISTEN_PORT] for `Iam <name> <address>` handshakes,
 * allocates each client a dedicated port pair, replies with `TXRXON <sendPort> <receivePort>`
 * and then calls [onClientConnect]. This class supplies the game-specific half of that
 * contract:
 *
 * - it accepts at most [maxConnections] players and refuses the rest,
 * - it starts listening on every accepted player's dedicated connection and forwards
 *   their messages to [onPlayerMessage], tagged with the player they came from,
 * - it serializes world state to JSON and broadcasts it with [broadcast].
 *
 * Construction starts the server: the base class spawns its handshake thread from its own
 * `init` block, so players may begin connecting as soon as the constructor returns. Only one
 * instance can exist per JVM at a time, since the common ports are fixed - construct a second
 * one before calling [shutDown] on the first and the bind will fail.
 *
 * @property maxConnections the largest number of players allowed on the server at once
 * @param onPlayerMessage invoked with the sending player's name and the raw message text for
 * every datagram received from any player. Called on that player's listener thread, so it
 * should return quickly and must be safe to call concurrently for different players.
 */
class GameServer(
    val maxConnections: Int,
    private val onPlayerMessage: (playerName: String, message: String) -> Unit = { _, _ -> }
) : MultiConnectionUDPServer() {

    /**
     * The players that finished the handshake, were accepted, and are being listened to,
     * keyed by the name they handshook with.
     *
     * Concurrent because it is written from the base class's handshake thread while the game
     * thread reads it to [broadcast] and [push].
     */
    private val players = ConcurrentHashMap<String, UDPConnection>()

    /** How many players are connected right now. */
    val playerCount: Int get() = players.size

    /** The names of the players connected right now, as a detached copy. */
    val playerNames: Set<String> get() = players.keys.toSet()

    /**
     * Accepts a freshly handshaken client as a player, unless the server is already full.
     *
     * The base class has, by this point, already told the client which ports to use, so a
     * refusal cannot be a handshake rejection - the connection is terminated instead, which
     * releases its ports and leaves the client with a port pair that never answers.
     *
     * A player that handshakes under a name that is already connected replaces it, and the
     * stale connection is terminated so its ports are not leaked.
     *
     * @param connection the connection the base class just registered
     */
    override fun onClientConnect(connection: UDPConnection) {
        admit(connection)
            .andThen { admitted -> listenTo(admitted) }
            .onFailure { cause ->
                // The single failure path for both a refusal and a connection that could not be
                // listened to. Removing by value leaves an existing player of the same name
                // untouched, and is a no-op when this connection was never admitted.
                log.warn("Dropping '{}': {}", connection.name, cause.message)
                players.remove(connection.name, connection)
                connection.terminate()
            }
    }

    /**
     * Decides whether a freshly handshaken client may join.
     *
     * A player returning under a name that is already connected is always admitted - they
     * replace their previous connection rather than counting a second time against the cap.
     *
     * @param connection the connection the base class just registered
     * @return the admitted connection, or [Result.failure] carrying the reason it was refused
     */
    private fun admit(connection: UDPConnection): Result<UDPConnection> = when {
        !isFullyConstructed ->
            Result.failure(IllegalStateException("it handshook before the server finished starting up"))

        players.size >= maxConnections && !players.containsKey(connection.name) ->
            Result.failure(IllegalStateException("the server is full (${players.size}/$maxConnections)"))

        else -> Result.success(connection)
    }

    /**
     * Registers an admitted [connection] as a player and starts listening to it, terminating
     * the stale connection of a player who is reconnecting so that its ports are not leaked.
     *
     * @param connection the connection [admit] accepted
     * @return [Result.success] once the player is being listened to, or the failure that prevented it
     */
    private fun listenTo(connection: UDPConnection): Result<Unit> {
        players.put(connection.name, connection)?.let { stale ->
            log.info("'{}' reconnected, terminating their previous connection", connection.name)
            stale.terminate()
        }
        log.info("'{}' joined ({}/{})", connection.name, players.size, maxConnections)
        return connection.actuate { message -> onPlayerMessage(connection.name, message) }
    }

    /**
     * Whether this instance's own fields have been assigned yet.
     *
     * [MultiConnectionUDPServer] starts accepting handshakes from its `init` block, which the
     * JVM runs before a subclass's fields are initialized. A client already retrying `Iam` when
     * the server boots can therefore reach [onClientConnect] while [players] is still `null`,
     * so that window is detected and refused instead of failing on the handshake thread.
     */
    @Suppress("SENSELESS_COMPARISON")
    private val isFullyConstructed: Boolean get() = players != null

    /**
     * Snapshots and broadcasts the given world state to every connected player.
     * @param visibleObjects the objects the players should be told about
     * @return [Result.success] if the state reached every player, or the first failure encountered
     */
    fun broadcast(visibleObjects: Iterable<VisibleObject>): Result<Unit> =
        broadcast(visibleObjects.map { visibleObject -> VisibleObjectSnapshot from visibleObject })

    /**
     * Broadcasts already-taken snapshots to every connected player as a
     * `STATE <json>` message.
     * @param snapshots the world state to serialize and send
     * @return [Result.success] if the state reached every player, or the first failure encountered
     */
    fun broadcast(snapshots: List<VisibleObjectSnapshot>): Result<Unit> =
        runCatching { Json.encodeToString(snapshots) }
            .onFailure { cause -> log.error("Could not serialize {} snapshot(s)", snapshots.size, cause) }
            .andThen { json -> pushToAllPlayers("$STATE_VERB $json") }

    /**
     * Sends a message to every connected player on their own dedicated connection.
     *
     * This is not the inherited [pushToAll], which sends to each client's address on the
     * common handshake port; this uses the private port pair each player was handed.
     *
     * @param message the text to send to all players
     * @return [Result.success] if the message reached every player, or the first failure encountered
     */
    fun pushToAllPlayers(message: String): Result<Unit> {
        log.debug("Pushing a message to all {} player(s)", players.size)
        return players.values.fold(Result.success(Unit)) { pushed, connection ->
            // Every player is pushed to regardless of their predecessors' outcome; only the
            // reported Result short-circuits, not the delivery.
            connection.push(message).let { outcome -> pushed.andThen { outcome } }
        }
    }

    /**
     * Sends a message to a single player on their dedicated connection.
     * @param playerName the name the player handshook with
     * @param message the text to send
     * @return [Result.success] if the datagram was sent, or [Result.failure] if no such player
     * is connected or the send failed
     */
    fun push(playerName: String, message: String): Result<Unit> =
        players[playerName]?.push(message)
            ?: Result.failure(NoSuchElementException("No connected player named '$playerName'"))

    /**
     * Disconnects a single player, releasing their dedicated ports. They are free to handshake
     * again afterwards.
     * @param playerName the name the player handshook with
     * @return [Result.success] if their connection was released, or [Result.failure] if no such
     * player is connected or the release failed
     */
    fun disconnect(playerName: String): Result<Unit> =
        players.remove(playerName)?.let { connection ->
            log.info("Disconnecting '{}'", playerName)
            connection.terminate()
        } ?: Result.failure(NoSuchElementException("No connected player named '$playerName'"))

    /**
     * Shuts the server down, disconnecting every player and releasing the common handshake
     * ports. Once called, this instance should be discarded - there is no restart.
     *
     * The inherited [stop] does the actual teardown; this wrapper exists to also forget the
     * players, so it should be preferred over calling [stop] directly.
     *
     * @return [Result.success] if every step succeeded, or the first failure encountered
     */
    fun shutDown(): Result<Unit> {
        log.info("Shutting down the game server and its {} player(s)", players.size)
        players.clear()
        return stop()
    }

    companion object {
        /** The verb that opens a world-state broadcast sent by [broadcast]. */
        const val STATE_VERB = "STATE"
    }
}

/**
 * Chains a [Result]-returning [transform] onto this result, short-circuiting on failure.
 *
 * WebTools keeps its own `flatMap` for this `internal`, so the composition the networking
 * layer is written in is reproduced here rather than reached for across the module boundary.
 *
 * @param transform applied to the encapsulated value if this result is a success
 * @return [transform]'s result if this is a success, otherwise this failure unchanged
 */
private inline fun <T, R> Result<T>.andThen(transform: (T) -> Result<R>): Result<R> =
    fold(onSuccess = transform, onFailure = { cause -> Result.failure(cause) })
