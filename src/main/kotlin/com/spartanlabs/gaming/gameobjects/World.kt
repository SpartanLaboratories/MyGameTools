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
     * Objects to drop from [gameObjects] at the end of the current [tick]. An object adds
     * itself here during its own tick - a dying [Alive] with [Alive.DeathResponse.REMOVAL]
     * does - and [tick] removes them once the pass is done, so removal never disturbs iteration.
     */
    val removeList: ArrayList<GameObject> = ArrayList()

    /**
     * Spatial index of the [VisibleObject]s in [gameObjects], keyed by world position and
     * rebuilt from scratch at the start of every [tick]. Between ticks it reflects the
     * positions the objects held when the last tick began.
     */
    val quadtree: Quadtree<Double, VisibleObject> = Quadtree()

    /**
     * Adds [gameObject] to [gameObjects]; for an [Alive] it also sets [Alive.world] so a
     * [Alive.DeathResponse.REMOVAL] death can reach [removeList]. Adding straight to
     * [gameObjects] still works, but then an [Alive] needs its [Alive.world] set by hand.
     *
     * @param gameObject the object to bring into the world
     */
    fun add(gameObject: GameObject) {
        gameObjects.add(gameObject)
        if (gameObject is Alive) gameObject.world = this
    }

    /**
     * Advances the world by one frame: rebuilds [quadtree] from [gameObjects], [GameObject.tick]s
     * every owned object, then drops everything queued in [removeList].
     *
     * Objects are ticked over a snapshot of [gameObjects], so an object may add to [gameObjects]
     * or [removeList] during its own tick without disturbing the current pass.
     */
    fun tick() {
        rebuildQuadtree()

        val ticking = gameObjects.toList()
        log.debug("World tick: advancing {} game object(s)", ticking.size)
        ticking.forEach(GameObject::tick)

        if (removeList.isNotEmpty()) {
            log.debug("World removing {} game object(s)", removeList.size)
            gameObjects.removeAll(removeList.toSet())
            removeList.clear()
        }
    }

    /** Clears [quadtree] and re-inserts every [VisibleObject] in [gameObjects] at its current position. */
    private fun rebuildQuadtree() {
        quadtree.clear()
        val visible = gameObjects.filterIsInstance<VisibleObject>()
        visible.forEach { quadtree.insert(it.location.x, it.location.y, it) }
        log.debug("World rebuilt its quadtree from {} visible object(s)", visible.size)
    }
}
