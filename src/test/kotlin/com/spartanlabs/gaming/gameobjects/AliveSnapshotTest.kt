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
import kotlin.test.assertNull
//endregion

/** Covers [AliveSnapshot] capturing an [Alive]'s stats on top of its [ActorSnapshot], and its JSON round trip. */
class AliveSnapshotTest {

    private fun alive(maxHealth: Double = 100.0) = Alive(
        location = Point(3.0, 4.0),
        dimensions = Dimensions(width = 8.0, height = 6.0),
        maxHealth = maxHealth
    )

    @Test
    fun `from captures health, faction, damage, and the underlying actor state`() {
        val actor = alive(maxHealth = 120.0).apply {
            health.current = 45.0
            faction = "red"
            damage = ModularStat(7.5)
            destination = Point(30.0, 4.0)
        }

        val snapshot = AliveSnapshot from actor

        assertEquals(45.0, snapshot.health.value)
        assertEquals(120.0, snapshot.health.maxValue)
        assertEquals("red", snapshot.faction)
        assertEquals(7.5, snapshot.damage)
        assertEquals(30.0, snapshot.actor.destination.x)
        assertEquals(3.0, snapshot.actor.visibleObject.gameObject.location.x)
    }

    @Test
    fun `ownerName is null when unowned and the player name when owned`() {
        val actor = alive()

        assertNull((AliveSnapshot from actor).ownerName)

        Player("alice").own(actor)
        assertEquals("alice", (AliveSnapshot from actor).ownerName)
    }

    @Test
    fun `the snapshot survives a JSON round trip unchanged`() {
        val snapshot = AliveSnapshot from alive().apply { faction = "blue" }

        val restored = Json.decodeFromString<AliveSnapshot>(Json.encodeToString(snapshot))

        assertEquals(snapshot, restored)
    }
}
