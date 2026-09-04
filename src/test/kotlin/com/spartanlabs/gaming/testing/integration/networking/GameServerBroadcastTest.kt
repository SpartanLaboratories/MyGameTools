package com.spartanlabs.gaming.testing.integration.networking

//region 1. Organization Internal
// 1.1 Spartan Laboratories
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
// 1.2 Spartan Gaming
import com.spartanlabs.gaming.gameobjects.Actor
import com.spartanlabs.gaming.gameobjects.Alive
import com.spartanlabs.gaming.gameobjects.VisibleObject
import com.spartanlabs.gaming.networking.GameServer
//endregion

//region 4. Programming Infrastructure and Support
// 4.3 Testing
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
//endregion

/**
 * Covers [GameServer.broadcast] leaving non-visible objects out of the world-state message and
 * tagging an [Actor] / [Alive] with its own snapshot type on the wire.
 */
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

    @Test
    fun `an Actor is broadcast as an actor snapshot`() {
        val server = fixture.startServer(maxConnections = 4)
        val ports = fixture.client().handshake("alice").getOrThrow()
        assertTrue(fixture.awaitPlayers(expected = 1), "the player was never admitted")
        val channel = fixture.channel(ports)

        val actor = Actor(location = Point(5.0, 6.0)).apply { destination = Point(20.0, 6.0) }
        assertTrue(server.broadcast(listOf(actor)).isSuccess)

        val message = channel.receive().getOrThrow()
        assertTrue(message.contains("\"type\":\"actor\""), "expected an actor-tagged snapshot: $message")
        assertTrue(message.contains("destination"), "actor snapshots carry a destination: $message")
    }

    @Test
    fun `an Alive is broadcast as an alive snapshot`() {
        val server = fixture.startServer(maxConnections = 4)
        val ports = fixture.client().handshake("alice").getOrThrow()
        assertTrue(fixture.awaitPlayers(expected = 1), "the player was never admitted")
        val channel = fixture.channel(ports)

        val unit = Alive(location = Point(5.0, 6.0), dimensions = Dimensions(4.0, 4.0), maxHealth = 100.0)
            .apply { faction = "red" }
        assertTrue(server.broadcast(listOf(unit)).isSuccess)

        val message = channel.receive().getOrThrow()
        assertTrue(message.contains("\"type\":\"alive\""), "expected an alive-tagged snapshot: $message")
        assertTrue(message.contains("\"faction\":\"red\""), "alive snapshots carry the faction: $message")
    }

    @Test
    fun `two players can each open their own dedicated channel at once`() {
        val server = fixture.startServer(maxConnections = 4)

        val p1Ports = fixture.client().handshake("p1").getOrThrow()
        val p2Ports = fixture.client().handshake("p2").getOrThrow()
        assertTrue(fixture.awaitPlayers(expected = 2), "both players were never admitted")

        val p1Channel = fixture.channel(p1Ports)
        val p2Channel = fixture.channel(p2Ports)

        assertTrue(server.push("p1", "for-p1").isSuccess)
        assertTrue(server.push("p2", "for-p2").isSuccess)

        assertEquals("for-p1", p1Channel.receive().getOrThrow(), "p1's channel gets only p1's message")
        assertEquals("for-p2", p2Channel.receive().getOrThrow(), "p2's channel gets only p2's message")
    }
}
