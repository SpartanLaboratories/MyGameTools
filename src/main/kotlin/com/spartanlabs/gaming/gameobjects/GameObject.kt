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
 * A game object has only a [location] and a per-frame update hook. Subclasses layer behaviour
 * on top by overriding [onUpdate] for their own logic, or [tick] when they need to run work
 * around the base step (calling `super.tick()` to keep the update firing).
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
     * Advances this object by one simulation step.
     *
     * The base implementation just runs [onUpdate]; overrides add their own work and should
     * call `super.tick()` so the update still fires. Called once per frame by the owning
     * game loop, which this library does not provide.
     */
    open fun tick() {
        onUpdate()
    }

    /** This object's own per-frame logic, run by [tick]. */
    abstract fun onUpdate()
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
