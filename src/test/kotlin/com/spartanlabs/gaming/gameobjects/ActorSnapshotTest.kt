package com.spartanlabs.gaming.gameobjects

//region 1. Organization Internal
// 1.1 Spartan Laboratories
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
//endregion

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

/** Covers [ActorSnapshot] capturing an [Actor]'s movement and drawable state, and its JSON round trip. */
class ActorSnapshotTest {

    private fun actor() = Actor(
        location = Point(3.0, 4.0),
        dimensions = Dimensions(width = 8.0, height = 6.0)
    )

    @Test
    fun `from captures speed, destination, and the underlying visible-object state`() {
        val actor = actor().apply {
            baseSpeed = 5.0
            speedModifier = 2.0
            destination = Point(30.0, 40.0)
        }

        val snapshot = ActorSnapshot from actor

        assertEquals(10.0, snapshot.speed) // baseSpeed * speedModifier
        assertEquals(30.0, snapshot.destination.x)
        assertEquals(40.0, snapshot.destination.y)
        assertEquals(3.0, snapshot.visibleObject.gameObject.location.x)
        assertEquals(4.0, snapshot.visibleObject.gameObject.location.y)
        assertEquals(8.0, snapshot.visibleObject.dimensions.width)
    }

    @Test
    fun `the snapshot survives a JSON round trip unchanged`() {
        val snapshot = ActorSnapshot from actor().apply { destination = Point(1.0, 2.0) }

        val restored = Json.decodeFromString<ActorSnapshot>(Json.encodeToString(snapshot))

        assertEquals(snapshot, restored)
    }
}
