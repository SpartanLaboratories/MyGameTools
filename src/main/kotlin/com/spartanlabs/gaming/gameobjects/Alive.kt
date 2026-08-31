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
 * It carries a red [healthBar] sub-object that follows the actor and whose width tracks
 * [health] as a fraction of its maximum, both refreshed every [tick].
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

    /** How much health this actor removes from a target when it attacks; defaults to `10.0`. */
    var damage: Double = 10.0

    /**
     * The side this actor belongs to, used to tell friend from foe. Defaults to
     * [DEFAULT_FACTION]; callers compare factions however their game needs.
     */
    var faction: String = DEFAULT_FACTION

    /**
     * The [Player] this actor belongs to, or `null` when it is unowned.
     *
     * Kept in step with [Player.ownedAlives] in both directions: assigning a new owner pulls
     * this actor off the previous owner's roster and adds it to the new one, and
     * [Player.own] / [Player.disown] drive this property.
     */
    var owner: Player? = null
        set(value) {
            if (field === value) return
            field?.removeFromRoster(this)
            field = value
            value?.addToRoster(this)
            log.debug("An Alive is now owned by {}", value?.name ?: "no one")
        }

    /** `true` when this actor has an [owner]. */
    val hasOwner: Boolean get() = owner != null

    /**
     * How far, in world units, this actor can perceive other objects. Must not be negative;
     * defaults to [DEFAULT_VISION_RANGE].
     *
     * @throws IllegalArgumentException if assigned a negative value.
     */
    var visionRange: Double = DEFAULT_VISION_RANGE
        set(value) {
            if (value < 0) {
                log.warn("Rejected vision range {}: vision range cannot be negative.", value)
                require(value >= 0) { "vision range cannot be negative." }
            }
            field = value
        }

    /** `true` while [health] is above zero. */
    val isAlive get() = health.value > 0.0

    /** [healthBar]'s width at full health, captured before any tick scales it down. */
    private val fullHealthBarWidth = dimensions.width

    /**
     * [healthBar]'s vertical offset from the actor's origin, fixed at the actor's starting size:
     * half the actor's height to clear its top edge, plus half the bar's height so the bar rests
     * just above it.
     */
    private val healthBarYOffset = dimensions.height * (0.5 + (HEALTH_BAR_HEIGHT_FRACTION / 2))

    /**
     * A red bar drawn a fraction of the actor's height above its origin.
     *
     * Built from fresh [Dimensions]/[Point] rather than the constructor arguments, so that
     * sizing and positioning it never mutates the actor's own geometry (which shares those
     * objects). Its position is refreshed every [tick] to follow the actor.
     */
    val healthBar = VisibleObject(
        area = Square(
            dimensions = Dimensions(
                width = fullHealthBarWidth,
                height = dimensions.height * HEALTH_BAR_HEIGHT_FRACTION
            ),
            location = Point(x = location.x, y = location.y + healthBarYOffset)
        ),
        color = Color(255, 0, 0)
    )

    init {
        subObjects.add(healthBar)
        log.debug("Spawned an Alive at {} with {} health", location, maxHealth)
    }

    /** Advances the actor, then moves [healthBar] onto it and resizes it to the current [health] fraction. */
    override fun onUpdate() {
        super.onUpdate()
        healthBar.location.setTo(location.x, location.y + healthBarYOffset)
        healthBar.dimensions.width = fullHealthBarWidth * health.fractionOfMax.coerceIn(0.0, 1.0)
    }

    /**
     * `true` when [other] lies within this actor's [visionRange] of its [location].
     *
     * Carries the failed [Result] from [Point.distanceFrom] (rather than throwing) when
     * either position holds a NaN coordinate.
     *
     * @param other the object to test for visibility
     */
    fun canSee(other: GameObject): Result<Boolean> =
        location.distanceFrom(other.location).map { distance -> distance <= visionRange }

    companion object {
        /** [healthBar]'s height as a fraction of actor height; also feeds [healthBarYOffset]. */
        private const val HEALTH_BAR_HEIGHT_FRACTION = 0.2

        /** The [visionRange] a new [Alive] starts with, in world units. */
        const val DEFAULT_VISION_RANGE = 900.0

        /** The [faction] a new [Alive] starts in. */
        const val DEFAULT_FACTION = "neutral"
    }
}
