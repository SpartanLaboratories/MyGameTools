package com.spartanlabs.gaming.gameobjects

//region 2. Intended Function
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
//endregion

//region 4. Programming Infrastructure and Support
// 4.3 Testing
import kotlin.test.Test
import kotlin.test.assertEquals
//endregion

/** Covers [StatGroupSnapshot] copying a [StatGroup]'s three numbers and its JSON round trip. */
class StatGroupSnapshotTest {

    @Test
    fun `from copies value, maxValue, and cap`() {
        val snapshot = StatGroupSnapshot from StatGroup(value = 30.0, maxValue = 100.0, cap = 150.0)

        assertEquals(30.0, snapshot.value)
        assertEquals(100.0, snapshot.maxValue)
        assertEquals(150.0, snapshot.cap)
    }

    @Test
    fun `the snapshot survives a JSON round trip unchanged`() {
        val snapshot = StatGroupSnapshot from StatGroup(value = 5.0, maxValue = 10.0, cap = 20.0)

        assertEquals(snapshot, Json.decodeFromString<StatGroupSnapshot>(Json.encodeToString(snapshot)))
    }
}
