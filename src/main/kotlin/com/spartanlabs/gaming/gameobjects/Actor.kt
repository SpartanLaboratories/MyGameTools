package com.spartanlabs.gaming.gameobjects
// Organizational
// Spartan Laboratories
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
// Standard Library
import kotlin.math.hypot

abstract class Actor(
    location : Point = Point(),
    dimensions: Dimensions = Dimensions(),
):VisibleObject(location = location, dimensions = dimensions) {
    var baseSpeed : Double      = 10.0
        set(value) {
            if (value < 0) throw IllegalArgumentException("base speed cannot be negative.")
            field = value
        }
    var speedModifier : Double  = 1.0
        set(value) {
            if (value < 0) throw IllegalArgumentException("base speed cannot be negative.")
            field = value
        }

    val speed get() = baseSpeed * speedModifier
    var destination = Point(location)
    val isAtDestination get() = location == destination
    val isOneStepAway : Boolean  get() = location.distanceFrom(destination)
        .getOrThrow() < speed

    val locmod get() =
        hypot(location.x - destination.x, location.y - destination.y).let { hypotenuse ->
            Point(
                speed * (destination.x - location.x) / hypotenuse,
                speed * (destination.y - location.y) / hypotenuse
            )
        }
    override fun tick() {
        super.tick()
        move()
    }
    internal fun move() {
        if (!isAtDestination)
            if (isOneStepAway)
                location.setTo(destination)
            else location += locmod
    }
}