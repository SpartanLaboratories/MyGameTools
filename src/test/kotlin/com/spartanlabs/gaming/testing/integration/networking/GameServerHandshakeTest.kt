package com.spartanlabs.gaming.testing.integration.networking

//region 1. Organization Internal
// 1.2 Spartan Gaming
import com.spartanlabs.gaming.networking.GameServer
//endregion

//region 4. Programming Infrastructure and Support
// 4.3 Testing
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
//endregion

/** Covers how [GameServer] turns a completed `Iam` handshake into a tracked player. */
class GameServerHandshakeTest {

    private val fixture = ServerFixture()

    @AfterTest
    fun tearDown() = fixture.close()

    @Test
    fun `a client that completes the handshake becomes a tracked player`() {
        val server = fixture.startServer(maxConnections = 4)

        val handshake = fixture.client().handshake("alice")

        assertTrue(handshake.isSuccess, "handshake failed: ${handshake.exceptionOrNull()}")
        assertTrue(fixture.awaitPlayers(expected = 1), "the player was never admitted")
        assertEquals(1, server.playerCount)
        assertEquals(setOf("alice"), server.playerNames)
    }

    @Test
    fun `the handshake reply hands out a distinct dedicated port pair`() {
        fixture.startServer(maxConnections = 4)

        val ports = fixture.client().handshake("alice").getOrThrow()

        assertTrue(ports.serverSendPort > 0, "send port should be a real port")
        assertTrue(ports.serverReceivePort > 0, "receive port should be a real port")
        assertNotEquals(
            ports.serverSendPort,
            ports.serverReceivePort,
            "a player cannot be sent to and listened to on one port"
        )
    }

    @Test
    fun `each player is handed its own port pair`() {
        fixture.startServer(maxConnections = 4)

        val first = fixture.client().handshake("alice").getOrThrow()
        val second = fixture.client().handshake("bob").getOrThrow()

        assertEquals(9996, first.serverReceivePort, "the first registration's receive port")
        assertEquals(9994, second.serverReceivePort, "the second registration's receive port")
        assertNotEquals(first.serverReceivePort, second.serverReceivePort, "ports must not be reused")
    }

    @Test
    fun `reconnecting under the same name from a new origin does not count twice`() {
        val server = fixture.startServer(maxConnections = 4)

        assertTrue(fixture.client().handshake("alice").isSuccess)
        assertTrue(fixture.awaitPlayers(expected = 1))
        assertTrue(fixture.client().handshake("alice").isSuccess, "a returning player should be let back in")
        fixture.settle()

        assertEquals(1, server.playerCount, "a reconnect replaces the old connection")
        assertEquals(setOf("alice"), server.playerNames)
    }

    @Test
    fun `a retransmitted Iam from the same origin does not register a second player`() {
        val server = fixture.startServer(maxConnections = 4)
        val client = fixture.client()

        val first = client.handshake("alice").getOrThrow()
        assertTrue(fixture.awaitPlayers(expected = 1))
        val second = client.handshake("alice").getOrThrow()
        fixture.settle()

        assertEquals(1, server.playerCount, "a retransmit registers nothing new")
        assertEquals(setOf("alice"), server.playerNames)
        assertEquals(first, second, "the server repeats the first reply")
    }

    @Test
    fun `an unrecognised message is ignored rather than admitting a player`() {
        val server = fixture.startServer(maxConnections = 4)

        // "Iam" is the only verb the handshake port acts on; anything else is noise.
        val client = fixture.client()
        assertTrue(client.handshake("alice").isSuccess)
        assertTrue(fixture.awaitPlayers(expected = 1))

        assertEquals(1, server.playerCount, "only the real handshake should have registered")
    }
}
