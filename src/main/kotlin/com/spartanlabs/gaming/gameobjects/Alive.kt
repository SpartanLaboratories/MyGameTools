package com.spartanlabs.gaming.gameobjects

//region 1. Organization Internal
// 1.1 Spartan Laboratories
import com.spartanlabs.generaltools.Color
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Square
//endregion

/**
 * An [Actor] with [health] that can be depleted.
 *
 * It carries a red [healthBar] sub-object whose width tracks [health] as a fraction of its
 * maximum, refreshed every [tick].
 *
 * @param location where the actor starts
 * @param dimensions the actor's size
 * @param maxHealth the actor's starting and maximum health; must be positive
 */
class Alive(
    location: Point,
    dimensions: Dimensions,
    maxHealth: Double
) : Actor(location = location, dimensions = dimensions) {

    /** The actor's health, from `maxHealth` down to (and past) zero. */
    var health: StatGroup = StatGroup(value = maxHealth, maxValue = maxHealth, cap = maxHealth)

    /** `true` while [health] is above zero. */
    val isAlive get() = health.value > 0.0

    /** [healthBar]'s width at full health, captured before any tick scales it down. */
    private val fullHealthBarWidth = dimensions.width

    /**
     * A red bar drawn a fraction of the actor's height off its origin.
     *
     * Built from fresh [Dimensions]/[Point] rather than the constructor arguments, so that
     * sizing and positioning it never mutates the actor's own geometry (which shares those
     * objects).
     */
    val healthBar = VisibleObject(
        area = Square(
            dimensions = Dimensions(
                width = fullHealthBarWidth,
                height = dimensions.height * HEALTH_BAR_HEIGHT_FRACTION
            ),
            location = Point(
                x = location.x,
                y = location.y - dimensions.height * HEALTH_BAR_HEIGHT_FRACTION
            )
        ),
        color = Color(255, 0, 0)
    )

    init {
        subObjects.add(healthBar)
        log.debug("Spawned an Alive at {} with {} health", location, maxHealth)
    }

    /** Advances the actor, then resizes [healthBar] to the current [health] fraction. */
    override fun tick() {
        super.tick()
        healthBar.dimensions.width = fullHealthBarWidth * health.fractionOfMax.coerceIn(0.0, 1.0)
    }

    companion object {
        /** [healthBar]'s height, and its offset off the actor's origin, as a fraction of actor height. */
        private const val HEALTH_BAR_HEIGHT_FRACTION = 0.2
    }
}
