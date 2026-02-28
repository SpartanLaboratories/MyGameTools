import com.spartanlabs.geometry.Point
import java.lang.Double.NaN
import kotlin.test.Test
import kotlin.test.assertNotEquals

class TestActor : Actor(){
    override fun draw() {}
    override fun onUpdate() {}
}
class Test {
    @Test
    fun testActorMovement(): Unit {
        val actor = TestActor()
        actor.destination.setTo(100.0, 1000.0)
        actor.baseSpeed = 10.0
        actor.speedModifier = 1.0
        actor.tick()
        assertNotEquals(NaN, actor.location.x)
        assertNotEquals(NaN, actor.location.y)
        println("${actor.location.x}, ${actor.location.y}")
        assertNotEquals(Point(0.0, 0.0), actor.location)
    }
}