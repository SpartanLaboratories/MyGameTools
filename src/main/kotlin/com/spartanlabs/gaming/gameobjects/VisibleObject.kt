package com.spartanlabs.gaming.gameobjects
//region 1. Organization Internal
// 1.1 Spartan Laboratories
import com.spartanlabs.generaltools.Color
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Square
import com.spartanlabs.geometry.serializations.DimensionsSnapshot
//endregion

//region 2. Intended Function
import kotlinx.serialization.Serializable
//endregion

/**
 * A [GameObject] that has something to draw: an [area], a [color], a [texture], and a
 * facing [angle]. It does not move on its own - [Actor] is the moving subclass - but it can
 * carry [subObjects] that are drawn relative to it (a health bar, a nameplate, ...).
 *
 * The four secondary constructors are conveniences for the common case of "just give me a
 * box at a position", accepting a [Dimensions]/[Point] pair, raw sizes, or raw coordinates
 * in place of a fully built [Square].
 *
 * @param area the object's position and size as one [Square]
 * @param color the tint applied when drawing, opaque white by default
 * @param texture the name of the texture to draw, `"default.png"` by default
 */
open class VisibleObject(
    val area: Square = Square(
        dimensions  = Dimensions(width = 25.0, height = 25.0),
        location    = Point()
    ),
    var color: Color = Color(255, 255, 255, 255),
    var texture: String = "default.png") : GameObject(area.location) {

    /** Creates a visible object of [dimensions] at [location]. */
    constructor(dimensions: Dimensions = Dimensions(width = 25.0, height = 25.0), location: Point = Point(x = 0.0, y = 0.0))
            : this(Square(dimensions = dimensions, location = location))

    /** Creates a visible object [width] by [height] at [location]. */
    constructor(width: Double = 25.0, height: Double = 25.0, location: Point = Point(x = 0.0, y = 0.0))
            : this(Square(dimensions = Dimensions(width = width, height = height), location = location))

    /** Creates a visible object of [dimensions] at ([x], [y]). */
    constructor(dimensions: Dimensions = Dimensions(width = 25.0, height = 25.0), x: Double = 0.0, y: Double = 0.0)
            : this(Square(dimensions = dimensions, location = Point(x = x, y = y)))

    /** Creates a visible object [width] by [height] at ([x], [y]). */
    constructor(width: Double = 25.0, height: Double = 25.0, x: Double = 0.0, y: Double = 0.0)
            : this(Square(dimensions = Dimensions(width = width, height = height), location = Point(x = x, y = y)))

    /** Shortcut for [area]'s size; reads and writes straight through to it. */
    var dimensions
        get() = area.dimensions
        set(value) { area.dimensions = value }

    /**
     * The direction the object faces, in whole degrees counter-clockwise from the positive
     * x-axis.
     *
     * A value outside `0..359` is normalised into range (a full turn is 360 degrees) and the
     * out-of-range assignment is logged, rather than rejected, so a caller doing raw angle
     * arithmetic does not have to wrap it themselves.
     */
    var angle = 0
        set(value) {
            field = value.mod(360)
            if (value != field) log.warn("Angle {} normalised to {} degrees.", value, field)
        }

    /**
     * Whether this object rotates to face a direction, i.e. whether its [angle] is meaningful
     * to a renderer. `false` by default: a plain object is drawn upright regardless of [angle].
     */
    var turns: Boolean = false

    /** Objects drawn relative to this one, e.g. a health bar. Drawn in insertion order. */
    val subObjects: MutableList<VisibleObject> = mutableListOf()

    /** A plain visible object has no per-frame logic of its own. */
    override fun onUpdate() {}
}

/**
 * An immutable, serializable copy of a [Color]'s channels.
 *
 * @property r red channel, `0..255`
 * @property g green channel, `0..255`
 * @property b blue channel, `0..255`
 * @property a alpha channel, `0..255`
 */
@Serializable
data class ColorSnapshot(val r: Int, val g: Int, val b: Int, val a: Int) {
    companion object {
        /** Takes a snapshot of [color]'s channels. */
        infix fun from(color: Color): ColorSnapshot =
            ColorSnapshot(color.red, color.green, color.blue, color.alpha)
    }
}

/**
 * An immutable, serializable copy of a [VisibleObject]'s drawable state, including its
 * [subObjects] snapshotted recursively.
 *
 * @property gameObject the underlying [GameObjectSnapshot] (position)
 * @property dimensions the object's size at snapshot time
 * @property color the object's tint at snapshot time
 * @property texture the object's texture name at snapshot time
 * @property angle the object's facing in degrees at snapshot time
 * @property turns whether [angle] is meaningful for this object at snapshot time
 * @property subObjects snapshots of the object's [VisibleObject.subObjects], in order
 */
@Serializable
data class VisibleObjectSnapshot(
    val gameObject: GameObjectSnapshot,
    val dimensions: DimensionsSnapshot,
    val color: ColorSnapshot,
    val texture: String,
    val angle: Int,
    val turns: Boolean,
    val subObjects: List<VisibleObjectSnapshot>) {
    companion object {
        /** Takes a snapshot of [visibleObject] and, recursively, its sub-objects. */
        infix fun from(visibleObject: VisibleObject): VisibleObjectSnapshot = VisibleObjectSnapshot(
            GameObjectSnapshot.from(visibleObject),
            DimensionsSnapshot.from(visibleObject.dimensions),
            ColorSnapshot.from(visibleObject.color),
            visibleObject.texture,
            angle = visibleObject.angle,
            turns = visibleObject.turns,
            subObjects = visibleObject.subObjects.map(::from)
        )
    }
}
