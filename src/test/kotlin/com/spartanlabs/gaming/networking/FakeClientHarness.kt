package com.spartanlabs.gaming.networking

//region 1. Organization Internal
// 1.1 Spartan Laboratories
import com.spartanlabs.webtools.MultiConnectionUDPServer
//endregion

//region 2. Intended Function
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
//endregion

//region 4. Programming Infrastructure and Support
// 4.1 Logging
import org.slf4j.LoggerFactory
//endregion

/**
 * Drives the client half of the [GameServer] handshake protocol over real sockets.
 *
 * The harness owns the single socket bound to the common send port, so exactly one of these
 * exists per test even when several fake players handshake - every reply for this host lands
 * on that one socket regardless of which player it is addressed to.
 *
 * Everything happens over the loopback address, so the tests neither need a network nor
 * disturb one.
 */
internal class FakeClientHarness : AutoCloseable {

    /** The address fake players claim to live at, and where replies are delivered. */
    val address: InetAddress = InetAddress.getLoopbackAddress()

    /** Bound to the common send port, where the server delivers its `TXRXON` replies. */
    private val replies = DatagramSocket(MultiConnectionUDPServer.COMMON_SEND_PORT)

    /**
     * Performs a full `Iam` handshake for [name] and reads back the dedicated ports.
     *
     * Note that a successful result only proves the server replied - it is sent before the
     * server admits the player, so callers that care about the roster must wait for it
     * separately via [ServerFixture.awaitPlayers].
     *
     * @param name the player name to handshake under
     * @param timeoutMillis how long to wait for the reply
     * @return the ports the server allocated, or the failure that prevented the handshake
     */
    fun handshake(name: String, timeoutMillis: Int = REPLY_TIMEOUT_MILLIS): Result<ServerPorts> {
        log.info("Fake client '{}' is handshaking", name)
        return push("$HANDSHAKE_VERB $name ${address.hostAddress}")
            .andThen { awaitReply(timeoutMillis) }
            .andThen(::parseReply)
    }

    /**
     * Reads the next message the server sends to the common send port.
     * @param timeoutMillis how long to wait before giving up
     * @return the decoded message, or the failure that prevented reading one
     */
    fun awaitReply(timeoutMillis: Int = REPLY_TIMEOUT_MILLIS): Result<String> = runCatching {
        replies.soTimeout = timeoutMillis
        val packet = DatagramPacket(ByteArray(BUFFER_BYTES), BUFFER_BYTES)
        replies.receive(packet)
        String(packet.data, 0, packet.length, Charsets.UTF_8).trim()
            .also { reply -> log.debug("Fake client received '{}'", reply) }
    }

    /**
     * Sends a datagram to the server's common listen port.
     * @param message the text to send
     * @return [Result.success] if the datagram was sent, or the failure that prevented it
     */
    private fun push(message: String): Result<Unit> = runCatching {
        DatagramSocket().use { socket ->
            message.toByteArray(Charsets.UTF_8).let { payload ->
                socket.send(
                    DatagramPacket(payload, payload.size, address, MultiConnectionUDPServer.COMMON_LISTEN_PORT)
                )
            }
        }
    }

    /**
     * Pulls the dedicated port pair out of a `<address> TXRXON <sendPort> <receivePort>` reply.
     * @param reply the raw reply text
     * @return the parsed ports, or [Result.failure] if the reply was not a well-formed `TXRXON`
     */
    private fun parseReply(reply: String): Result<ServerPorts> = runCatching {
        val tokens = reply.split(' ')
        require(tokens.size > RECEIVE_PORT_INDEX && tokens[VERB_INDEX] == HANDSHAKE_REPLY_VERB) {
            "Expected '<address> $HANDSHAKE_REPLY_VERB <sendPort> <receivePort>' but got '$reply'"
        }
        ServerPorts(tokens[SEND_PORT_INDEX].toInt(), tokens[RECEIVE_PORT_INDEX].toInt())
    }

    /** Releases the common send port so the next test can bind it. */
    override fun close() {
        replies.close()
    }

    companion object {
        /** Shared slf4j logger for all [FakeClientHarness] instances. */
        private val log = LoggerFactory.getLogger(FakeClientHarness::class.java)

        /** The verb that opens a client handshake. */
        private const val HANDSHAKE_VERB = "Iam"

        /** The verb the server answers a handshake with. */
        private const val HANDSHAKE_REPLY_VERB = "TXRXON"

        /** Index of the verb within a split reply, which is preceded by the echoed address. */
        private const val VERB_INDEX = 1

        /** Index of the server's send port within a split reply. */
        private const val SEND_PORT_INDEX = 2

        /** Index of the server's receive port within a split reply. */
        private const val RECEIVE_PORT_INDEX = 3

        /** How long to wait for a handshake reply before declaring the handshake failed. */
        private const val REPLY_TIMEOUT_MILLIS = 4000

        /** Size of the buffer incoming datagrams are read into. */
        private const val BUFFER_BYTES = 8192
    }
}
