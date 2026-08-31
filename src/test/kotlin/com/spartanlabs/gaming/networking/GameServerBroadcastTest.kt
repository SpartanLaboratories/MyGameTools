package com.spartanlabs.gaming.networking

//region 1. Organization Internal
// 1.2 Spartan Gaming
import com.spartanlabs.gaming.gameobjects.VisibleObject
//endregion

//region 4. Programming Infrastructure and Support
// 4.3 Testing
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
//endregion

/** Covers [GameServer.broadcast] leaving non-visible objects out of the world-state message. */
class GameServerBroadcastTest {

    private val fixture = ServerFixture()

    @AfterTest
    fun tearDown() = fixture.close()

    @Test
    fun `a non-visible object is left out of the broadcast`() {
        val server = fixture.startServer(maxConnections = 4)
        val ports = fixture.client().handshake("alice").getOrThrow()
        assertTrue(fixture.awaitPlayers(expected = 1), "the player was never admitted")
        val channel = fixture.channel(ports)

        val shown = VisibleObject(texture = "shown.png")
        val hidden = VisibleObject(texture = "hidden.png").apply { visible = false }
        assertTrue(server.broadcast(listOf(shown, hidden)).isSuccess)

        val message = channel.receive().getOrThrow()
        assertTrue(message.contains("shown.png"), "the visible object should be broadcast")
        assertFalse(message.contains("hidden.png"), "the hidden object must not be broadcast")
    }
}
