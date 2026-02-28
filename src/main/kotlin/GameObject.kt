import com.spartanlabs.geometry.Point
import org.slf4j.LoggerFactory

val log = LoggerFactory.getLogger("GameObject")
abstract class GameObject (val location: Point = Point() ) {
    constructor(x:Double, y:Double):this(Point(x=x,y=y))
    open fun tick(){
        onUpdate()
    }
    abstract fun onUpdate()
}