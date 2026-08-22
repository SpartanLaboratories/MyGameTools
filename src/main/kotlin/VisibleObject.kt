import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Square

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
    override fun tick(){
        super.tick()
        draw()
    }
    abstract fun draw()
}