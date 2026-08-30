package com.spartanlabs.gaming.gameobjects
//region 1. Organization Internal
// 1.1 Spartan Laboratories
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
//endregion

//region 3. Utility / Catch-all
// 3.2 Kotlin
// 3.2.1 Standard library
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt
//endregion

/**
 * A [VisibleObject] that can move towards a [destination] under its own power.
 *
 * Movement is driven by [tick]: every tick the actor steps [speed] units along the
 * straight line towards [destination], snapping exactly onto it once it is within a
 * single step. Setting [destination] also re-aims [angle] at the new target.
 *
 * @param location the actor's starting position; also its initial [destination].
 * @param dimensions the actor's size.
 */
open class Actor(
    location : Point = Point(),
    dimensions: Dimensions = Dimensions(),
):VisibleObject(location = location, dimensions = dimensions) {
    /**
     * The actor's unmodified movement rate in units per tick.
     *
     * @throws IllegalArgumentException if assigned a negative value.
     */
    var baseSpeed : Double      = 10.0
        set(value) {
            if (value < 0) {
                log.warn("Rejected base speed {}: base speed cannot be negative.", value)
                require(value >= 0) { "base speed cannot be negative." }
            }
            field = value
        }

    /**
     * Multiplier applied to [baseSpeed] to produce the effective [speed]
     * (e.g. slows, hastes). Defaults to `1.0`.
     *
     * @throws IllegalArgumentException if assigned a negative value.
     */
    var speedModifier : Double  = 1.0
        set(value) {
            if (value < 0) {
                log.warn("Rejected speed modifier {}: speed modifier cannot be negative.", value)
                require(value >= 0) { "speed modifier cannot be negative." }
            }
            field = value
        }

    /** Effective movement rate in units per tick: [baseSpeed] scaled by [speedModifier]. */
    val speed get() = baseSpeed * speedModifier

    /**
     * The point the actor is moving towards.
     *
     * Assigning a new destination also updates [angle] to face it, measured in
     * whole degrees counter-clockwise from the positive x-axis and normalised to
     * `0..359`. The angle is computed from the actor's position at assignment time;
     * it is not re-derived as the actor moves. If the new destination coincides with
     * the actor's current position, or holds a NaN coordinate, [angle] is left
     * unchanged (there is no direction to face).
     */
    var destination = Point(location)
        set(value) {
            field = value
            val dx = value.x - location.x
            val dy = value.y - location.y
            when {
                dx.isNaN() || dy.isNaN() ->
                    log.warn("Destination {} has a NaN coordinate; angle left at {} degrees.", value, angle)
                dx == 0.0 && dy == 0.0 ->
                    log.debug("Destination {} matches current position; angle left at {} degrees.", value, angle)
                else -> {
                    angle = Math.toDegrees(atan2(dy, dx)).roundToInt().mod(360)
                    log.debug("Destination set to {}; angle now {} degrees.", value, angle)
                }
            }
        }

    /** `true` once the actor has reached its [destination]. */
    val isAtDestination get() = location == destination

    /**
     * `true` when the actor is close enough to reach its [destination] in a single step.
     *
     * Carries the failed [Result] from [Point.distanceFrom] (rather than throwing) when
     * [location] or [destination] holds a NaN coordinate.
     */
    val isOneStepAway : Result<Boolean> get() = location.distanceFrom(destination)
        .map { distance -> distance < speed }

    /**
     * The displacement to apply this tick: a vector of length [speed] pointing from
     * [location] towards [destination].
     */
    val locmod get() =
        hypot(location.x - destination.x, location.y - destination.y).let { hypotenuse ->
            Point(
                speed * (destination.x - location.x) / hypotenuse,
                speed * (destination.y - location.y) / hypotenuse
            )
        }

    /** Advances the actor by one simulation step, then moves it towards [destination]. */
    override fun tick() {
        super.tick()
        move().onFailure { cause -> log.error("Actor was not moved this tick.", cause) }
    }

    /**
     * Steps the actor towards [destination], snapping onto it once within one step.
     *
     * @return [Result.success] once the step is applied (including the no-op case where
     * the actor is already at its [destination]), or the failed [Result] from
     * [isOneStepAway] when the distance to [destination] cannot be measured.
     */
    internal fun move(): Result<Unit> =
        if (isAtDestination) Result.success(Unit)
        else isOneStepAway.map { oneStepAway ->
            if (oneStepAway) {
                location.setTo(destination)
                log.debug("Arrived at destination {}.", destination)
            } else location += locmod
        }
}
