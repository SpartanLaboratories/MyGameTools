package com.spartanlabs.gaming.networking

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
        val client = fixture.client()

        val first = client.handshake("alice").getOrThrow()
        val second = client.handshake("bob").getOrThrow()

        assertNotEquals(first.serverReceivePort, second.serverReceivePort, "ports must not be reused")
    }

    @Test
    fun `reconnecting under the same name does not count twice`() {
        val server = fixture.startServer(maxConnections = 4)
        val client = fixture.client()

        assertTrue(client.handshake("alice").isSuccess)
        assertTrue(fixture.awaitPlayers(expected = 1))
        assertTrue(client.handshake("alice").isSuccess, "a returning player should be let back in")
        fixture.settle()

        assertEquals(1, server.playerCount, "a reconnect replaces the old connection")
        assertEquals(setOf("alice"), server.playerNames)
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
