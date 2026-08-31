package com.spartanlabs.gaming.gameobjects

//region 1. Organization Internal
// 1.1 Spartan Laboratories
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.serializations.PointSnapshot
//endregion

//region 2. Intended Function
import kotlinx.serialization.Serializable
//endregion

//region 4. Programming Infrastructure and Support
// 4.1 Logging
import org.slf4j.Logger
import org.slf4j.LoggerFactory
//endregion

/** Shared slf4j logger for the game-object layer; bound to the facade only, never an implementation. */
internal val log: Logger = LoggerFactory.getLogger("com.spartanlabs.gaming.gameobjects")

/**
 * The root of every entity the game simulates, visible or not.
 *
 * A game object has a [location], an [active] flag, and a per-frame update hook. Subclasses
 * put their per-frame behaviour in [onUpdate] (calling `super.onUpdate()` first); the owning
 * game loop calls [tick], which runs [onUpdate] only while the object is [active].
 *
 * @param location the object's position in world space; defaults to the origin.
 */
abstract class GameObject(val location: Point = Point()) {

    /**
     * Creates a game object at ([x], [y]).
     * @param x the horizontal position in world space
     * @param y the vertical position in world space
     */
    constructor(x: Double, y: Double) : this(Point(x = x, y = y))

    /**
     * Whether this object participates in the simulation. `true` by default; while `false`,
     * [tick] is a no-op, so the object neither updates nor moves until it is reactivated.
     *
     * [com.spartanlabs.gaming.gameobjects.VisibleObject] overrides this to also keep its
     * `visible` flag in step with it.
     */
    open var active: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                log.debug("A {} became {}", this::class.simpleName, if (value) "active" else "inactive")
            }
        }

    /**
     * Advances this object by one simulation step, unless it is inactive.
     *
     * Called once per frame by the owning game loop (which this library does not provide).
     * Runs [onUpdate] only while [active] is `true`; override [onUpdate], not this, to add
     * per-frame behaviour.
     */
    fun tick() {
        if (active) onUpdate()
    }

    /**
     * This object's own per-frame logic, run by [tick] while [active].
     *
     * The base implementation does nothing; a subclass overrides it and calls
     * `super.onUpdate()` first so an ancestor's per-frame work still runs.
     */
    protected open fun onUpdate() {}
}

/**
 * An immutable, serializable copy of a [GameObject]'s networkable state: its [location].
 *
 * @property location the object's position at the moment the snapshot was taken
 */
@Serializable
data class GameObjectSnapshot(val location: PointSnapshot) {
    companion object {
        /** Takes a snapshot of [gameObject]'s current state. */
        infix fun from(gameObject: GameObject): GameObjectSnapshot =
            GameObjectSnapshot(PointSnapshot.from(gameObject.location))
    }
}
