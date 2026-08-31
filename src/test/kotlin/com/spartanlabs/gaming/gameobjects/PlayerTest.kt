package com.spartanlabs.gaming.gameobjects

//region 1. Organization Internal
// 1.1 Spartan Laboratories
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
//endregion

//region 4. Programming Infrastructure and Support
// 4.3 Testing
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
//endregion

/** Covers [Player] owning, releasing, and reporting its roster of [Alive]s. */
class PlayerTest {

    private fun alive(maxHealth: Double = 100.0) = Alive(
        location = Point(0.0, 0.0),
        dimensions = Dimensions(width = 10.0, height = 10.0),
        maxHealth = maxHealth
    )

    @Test
    fun `a new player owns nothing`() {
        assertTrue(Player("alice").ownedAlives.isEmpty())
    }

    @Test
    fun `own adds an actor to the roster`() {
        val player = Player("alice")
        val unit = alive()

        assertTrue(player.own(unit))

        assertEquals(listOf(unit), player.ownedAlives)
        assertTrue(player.owns(unit))
    }

    @Test
    fun `owning the same actor twice is a no-op`() {
        val player = Player("alice")
        val unit = alive()
        player.own(unit)

        assertFalse(player.own(unit))

        assertEquals(1, player.ownedAlives.size)
    }

    @Test
    fun `disown removes an owned actor and reports it`() {
        val player = Player("alice")
        val unit = alive()
        player.own(unit)

        assertTrue(player.disown(unit))

        assertFalse(player.owns(unit))
        assertTrue(player.ownedAlives.isEmpty())
    }

    @Test
    fun `disowning an actor that was never owned reports false`() {
        assertFalse(Player("alice").disown(alive()))
    }

    @Test
    fun `livingAlives excludes owned actors whose health has run out`() {
        val player = Player("alice")
        val healthy = alive()
        val dead = alive().apply { health.value = 0.0 }
        player.own(healthy)
        player.own(dead)

        assertEquals(listOf(healthy), player.livingAlives)
    }
}
