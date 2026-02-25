import com.spartanlabs.geometry.Point
import com.spartanlabs.geometry.Square

open class VisibleObject(
    val dimensions:Square = Square(
        dimensions  = Point(x = 25.0, y = 25.0),
        location    = Point()
)) : GameObject(dimensions.location){
    constructor(dimensions:Point = Point(x=25.0,y=25.0), location: Point = Point(x=0.0,y=0.0))
            :this(Square(dimensions=dimensions, location = location))
    constructor(width:Double = 25.0, height:Double = 25.0, location: Point = Point(x=0.0, y=0.0))
            :this(Square(dimensions = Point(x = width, y = height), location = location))
    constructor(dimensions:Point = Point(x = 25.0, y = 25.0), x:Double = 0.0, y:Double = 0.0)
            :this(Square(dimensions = dimensions, location = Point(x = x, y = y)))
    constructor(width: Double = 25.0, height: Double = 25.0, x: Double = 0.0, y: Double = 0.0)
            :this(Square(dimensions = Point(x = width, y = height), location = Point(x = x, y = y)))

}