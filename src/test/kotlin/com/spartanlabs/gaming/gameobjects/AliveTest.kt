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

/** Covers [Alive]'s health tracking and the health bar sub-object it manages. */
class AliveTest {

    private fun alive(maxHealth: Double = 100.0) = Alive(
        location = Point(0.0, 0.0),
        dimensions = Dimensions(width = 20.0, height = 10.0),
        maxHealth = maxHealth
    )

    @Test
    fun `building the health bar does not resize the actor`() {
        val actor = alive()

        assertEquals(20.0, actor.dimensions.width)
        assertEquals(10.0, actor.dimensions.height)
    }

    @Test
    fun `building the health bar does not move the actor`() {
        val actor = Alive(
            location = Point(3.0, 4.0),
            dimensions = Dimensions(width = 20.0, height = 10.0),
            maxHealth = 100.0
        )

        assertEquals(3.0, actor.location.x)
        assertEquals(4.0, actor.location.y)
    }

    @Test
    fun `the health bar shrinks proportionally as health drops`() {
        val actor = alive(maxHealth = 100.0)
        actor.health.value = 40.0

        actor.tick()

        assertEquals(20.0 * 0.4, actor.healthBar.dimensions.width, absoluteTolerance = 1e-9)
    }

    @Test
    fun `the health bar width never goes negative when health is below zero`() {
        val actor = alive(maxHealth = 100.0)
        actor.health.value = -10.0

        actor.tick()

        assertEquals(0.0, actor.healthBar.dimensions.width)
        assertFalse(actor.isAlive)
    }

    @Test
    fun `the health bar is registered as a sub-object`() {
        val actor = alive()

        assertTrue(actor.healthBar in actor.subObjects)
    }
}
