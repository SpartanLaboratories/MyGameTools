import com.spartanlabs.geometry.Point
import java.lang.Math.pow
import kotlin.math.hypot
import kotlin.math.sqrt

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

    val isAtDestination get() = location == destination
    val isOneStepAway   get() = speed > sqrt(pow(location.x - destination.x,2.0) + pow(location.y - destination.y,2.0))

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
        if (!isAtDestination)
            if (isOneStepAway)
                location.setTo(destination)
            else location += locmod
    }
}