import com.spartanlabs.geometry.Point
import kotlin.math.hypot

abstract class Actor():VisibleObject() {
    var baseSpeed : Double      = 10.0
        set(value) {
            if (value < 0) throw IllegalArgumentException("base speed cannot be negative.")
            field = value
        }
    var speedModifier : Double  = 1.0
        set(value) {
            if (value < 0) throw IllegalArgumentException("base speed cannot be negative.")
            field = value
        }
    val speed = baseSpeed * speedModifier
    var destination = Point(location)
    val locmod get() =
        hypot(location.x - destination.x, location.y - destination.y).let { hypotenuse ->
            Point(
                speed * (destination.x - location.x) / hypotenuse,
                speed * (destination.y - location.y) / hypotenuse
            )
        }
    override fun tick() {
        super.tick()
        move()
    }
    internal fun move() {
        location.modBy(locmod)
    }
}