package com.spartanlabs.gaming.networking

//region 2. Intended Function
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
//endregion

//region 4. Programming Infrastructure and Support
// 4.3 Testing
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
//endregion

/** Covers how [GameServer] routes a player's datagrams: `INPUT` to [MouseAction], the rest raw. */
class GameServerInputTest {

    private val fixture = ServerFixture()

    @AfterTest
    fun tearDown() = fixture.close()

    @Test
    fun `inputMessage round-trips through the INPUT verb`() {
        val action = MouseAction(MouseActionType.MOVE, button = -1, x = 4.5, y = 6.5)

        val message = GameServer.inputMessage(action)

        assertTrue(message.startsWith("${GameServer.INPUT_VERB} "))
        assertEquals(
            action,
            Json.decodeFromString<MouseAction>(message.removePrefix("${GameServer.INPUT_VERB} "))
        )
    }

    @Test
    fun `an INPUT datagram is decoded into a MouseAction for the sending player`() {
        val channel = channelFor("alice")
        val action = MouseAction(MouseActionType.PRESS, button = 0, x = 12.0, y = 34.0)

        assertTrue(channel.send(GameServer.inputMessage(action)).isSuccess)

        assertEquals("alice" to action, fixture.awaitPlayerInput())
        assertNull(fixture.awaitPlayerMessage(NEGATIVE_TIMEOUT_MILLIS), "INPUT must not also fire onPlayerMessage")
    }

    @Test
    fun `a malformed INPUT payload is dropped rather than reaching either callback`() {
        val channel = channelFor("alice")

        assertTrue(channel.send("${GameServer.INPUT_VERB} not-json").isSuccess)

        assertNull(fixture.awaitPlayerInput(NEGATIVE_TIMEOUT_MILLIS), "a payload that will not parse must be dropped")
        assertNull(fixture.awaitPlayerMessage(NEGATIVE_TIMEOUT_MILLIS), "a malformed INPUT must not fall through to onPlayerMessage")
    }

    @Test
    fun `a non-INPUT datagram is passed verbatim to onPlayerMessage`() {
        val channel = channelFor("alice")

        assertTrue(channel.send("CHAT hello there").isSuccess)

        assertEquals("alice" to "CHAT hello there", fixture.awaitPlayerMessage())
        assertNull(fixture.awaitPlayerInput(NEGATIVE_TIMEOUT_MILLIS), "a non-INPUT message must not fire onPlayerInput")
    }

    /** Handshakes [name], waits for the roster to catch up, and opens their dedicated channel. */
    private fun channelFor(name: String): FakePlayerChannel {
        fixture.startServer(maxConnections = 4)
        val ports = fixture.client().handshake(name).getOrThrow()
        assertTrue(fixture.awaitPlayers(expected = 1), "the player was never admitted")
        return fixture.channel(ports)
    }

    companion object {
        /** Short wait for a callback that is expected *not* to fire. */
        private const val NEGATIVE_TIMEOUT_MILLIS = 500L
    }
}
