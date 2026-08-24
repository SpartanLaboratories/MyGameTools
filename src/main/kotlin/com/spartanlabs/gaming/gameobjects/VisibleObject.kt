package com.spartanlabs.gaming.gameobjects
// Organizational
// Spartan Laboratories
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Square
// Spartan Gaming
import com.spartanlabs.geometry.serializations.DimensionsSnapshot
// Utility
import kotlinx.serialization.Serializable

/*
    A VISIBLE but STATIONARY com.spartanlabs.gaming.gameobjects.GameObject
    This should be used for GameObjects that are intended to be visible to the player
*/
abstract class VisibleObject(
    val area: Square = Square(
        dimensions  = Dimensions(width = 25.0, height = 25.0),
        location    = Point()
    ),
    var texture : String = "default.png") : GameObject(area.location){
    constructor(dimensions: Dimensions = Dimensions(width=25.0,height=25.0), location: Point = Point(x=0.0,y=0.0))
            :this(Square(dimensions=dimensions, location = location))
    constructor(width:Double = 25.0, height:Double = 25.0, location: Point = Point(x=0.0, y=0.0))
            :this(Square(dimensions = Dimensions(width = width, height = height), location = location))
    constructor(dimensions:Dimensions = Dimensions(width = 25.0, height = 25.0), x:Double = 0.0, y:Double = 0.0)
            :this(Square(dimensions = dimensions, location = Point(x = x, y = y)))
    constructor(width: Double = 25.0, height: Double = 25.0, x: Double = 0.0, y: Double = 0.0)
            :this(Square(dimensions = Dimensions(width = width, height = height), location = Point(x = x, y = y)))

    // A shortcut for accessing VO size
    var dimensions
        get() = area.dimensions
        set(value) { area.dimensions = value }
    // Stationary Object can face a certain way
    var angle = 0
        set(value) {
            assert(value in 0..360){ log.warn("Angle must be between 0 and 360.") }
            field = value
        }

    override fun tick(){
        super.tick()
        draw()
    }
    abstract fun draw()
}

@Serializable
data class VisibleObjectSnapshot (val gameObject: GameObjectSnapshot, val dimensions: DimensionsSnapshot, val angle: Int){
    companion object {
        infix fun from(visibleObject: VisibleObject): VisibleObjectSnapshot = VisibleObjectSnapshot(
            GameObjectSnapshot.from(visibleObject),
            DimensionsSnapshot.from(visibleObject.dimensions),
            angle = visibleObject.angle
        )
    }
}
