import com.spartanlabs.geometry.Point
import java.util.logging.Logger

open class GameObject (val location: Point = Point(x = 0.0, y = 0.0) ) {
    constructor(x:Double, y:Double):this(Point(x=x,y=y))
}