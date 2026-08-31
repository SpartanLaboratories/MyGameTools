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
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
//endregion

/** Covers [Alive.visionRange] and the [Alive.canSee] check built on it. */
class AliveVisionTest {

    private fun alive(x: Double = 0.0, y: Double = 0.0) = Alive(
        location = Point(x, y),
        dimensions = Dimensions(width = 20.0, height = 10.0),
        maxHealth = 100.0
    )

    @Test
    fun `a new Alive starts with the default vision range`() {
        assertEquals(Alive.DEFAULT_VISION_RANGE, alive().visionRange)
    }

    @Test
    fun `an object inside the vision range is seen`() {
        val watcher = alive()
        watcher.visionRange = 50.0
        val target = alive(x = 40.0)

        assertTrue(watcher.canSee(target).getOrThrow())
    }

    @Test
    fun `an object exactly on the vision boundary is seen`() {
        val watcher = alive()
        watcher.visionRange = 40.0
        val target = alive(x = 40.0)

        assertTrue(watcher.canSee(target).getOrThrow())
    }

    @Test
    fun `an object beyond the vision range is not seen`() {
        val watcher = alive()
        watcher.visionRange = 30.0
        val target = alive(x = 40.0)

        assertFalse(watcher.canSee(target).getOrThrow())
    }

    @Test
    fun `canSee reports a failure instead of throwing when a coordinate is NaN`() {
        val watcher = alive()
        val target = alive(x = Double.NaN)

        assertTrue(watcher.canSee(target).isFailure)
    }

    @Test
    fun `a negative vision range is rejected`() {
        assertFailsWith<IllegalArgumentException> { alive().visionRange = -1.0 }
    }
}
