package com.spartanlabs.gaming.gameobjects

//region 1. Organization Internal
// 1.2 Spartan Gaming
import com.spartanlabs.gaming.spatial.Quadtree
//endregion

/**
 * A container for everything the game is simulating: a flat list of [gameObjects] and a
 * [quadtree] spatial index over the [VisibleObject]s among them.
 *
 * [World] does not run itself - an external game loop calls [tick] once per frame. Each tick
 * rebuilds [quadtree] from the current positions of the owned objects and then advances every
 * owned object by one step. The index is rebuilt first so that an object's [GameObject.tick]
 * can query a spatial index that stays consistent for the whole frame.
 */
class World {

    /** Every object this world owns, visible or not, in insertion order. */
    val gameObjects: ArrayList<GameObject> = ArrayList()

    /**
     * Spatial index of the [VisibleObject]s in [gameObjects], keyed by world position and
     * rebuilt from scratch at the start of every [tick]. Between ticks it reflects the
     * positions the objects held when the last tick began.
     */
    val quadtree: Quadtree<Double, VisibleObject> = Quadtree()

    /**
     * Advances the world by one frame: rebuilds [quadtree] from [gameObjects], then
     * [GameObject.tick]s every owned object.
     *
     * Objects are ticked over a snapshot of [gameObjects], so an object may add to or remove
     * from the world during its own tick without disturbing the current pass; such changes
     * take effect on the next [tick].
     */
    fun tick() {
        rebuildQuadtree()
        val ticking = gameObjects.toList()
        log.debug("World tick: advancing {} game object(s)", ticking.size)
        ticking.forEach(GameObject::tick)
    }

    /** Clears [quadtree] and re-inserts every [VisibleObject] in [gameObjects] at its current position. */
    private fun rebuildQuadtree() {
        quadtree.clear()
        val visible = gameObjects.filterIsInstance<VisibleObject>()
        visible.forEach { quadtree.insert(it.location.x, it.location.y, it) }
        log.debug("World rebuilt its quadtree from {} visible object(s)", visible.size)
    }
}
