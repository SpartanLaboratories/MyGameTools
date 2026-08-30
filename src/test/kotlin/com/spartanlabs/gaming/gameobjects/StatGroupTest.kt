package com.spartanlabs.gaming.gameobjects

//region 4. Programming Infrastructure and Support
// 4.3 Testing
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
//endregion

/** Covers [StatGroup]'s construction guards and derived readouts. */
class StatGroupTest {

    @Test
    fun `fractionOfMax and fractionOfCap report value against their ceilings`() {
        val stat = StatGroup(value = 50.0, maxValue = 100.0, cap = 200.0)

        assertEquals(0.5, stat.fractionOfMax)
        assertEquals(0.25, stat.fractionOfCap)
    }

    @Test
    fun `isMaxed and isCapped flip at their ceilings`() {
        assertFalse(StatGroup(value = 99.0, maxValue = 100.0, cap = 120.0).isMaxed)
        assertTrue(StatGroup(value = 100.0, maxValue = 100.0, cap = 120.0).isMaxed)
        assertFalse(StatGroup(value = 119.0, maxValue = 100.0, cap = 120.0).isCapped)
        assertTrue(StatGroup(value = 120.0, maxValue = 100.0, cap = 120.0).isCapped)
    }

    @Test
    fun `a non-positive maxValue is rejected at construction`() {
        assertFailsWith<IllegalArgumentException> {
            StatGroup(value = 0.0, maxValue = 0.0, cap = 10.0)
        }
    }

    @Test
    fun `a non-positive cap is rejected at construction`() {
        assertFailsWith<IllegalArgumentException> {
            StatGroup(value = 5.0, maxValue = 10.0, cap = -1.0)
        }
    }
}
