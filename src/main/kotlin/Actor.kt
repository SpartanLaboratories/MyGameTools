import com.spartanlabs.geometry.Point
import kotlin.math.abs
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
    val speed get() = baseSpeed * speedModifier
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
        if (abs(location.x - destination.x) < locmod.x)
            location.x = destination.x
        else location.x += locmod.x
        if (abs(location.y - destination.y) < locmod.y)
            location.y = destination.y
        else location.y += locmod.y
    }
}