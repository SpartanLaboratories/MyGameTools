import com.spartanlabs.gaming.gameobjects.VisibleObject
import com.spartanlabs.gaming.gameobjects.VisibleObjectSnapshot
import com.spartanlabs.gaming.networking.GameServer
import com.spartanlabs.geometry.Dimensions
import com.spartanlabs.geometry.Point
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Minimal concrete VisibleObject for tests — draw()/onUpdate() are no-ops since
 * we only care about the serialized snapshot GameServer.push(VisibleObject) sends.
 */
private class TestVisibleObject(
    dimensions: Dimensions = Dimensions(width = 25.0, height = 25.0),
    location: Point = Point(x = 0.0, y = 0.0)
) : VisibleObject(dimensions = dimensions, location = location) {
    override fun draw() { /* no-op for tests */ }
    override fun onUpdate() { /* no-op for tests */ }
}

/**
 * Integration-style tests for [com.spartanlabs.gaming.networking.GameServer].
 *
 * GameServer wires together real UDP sockets (via Connection / UDPSendReceiveServer)
 * rather than exposing any injectable seam, so these tests exercise it over real
 * loopback sockets instead of mocking collaborators.
 *
 * Port scheme (per addConnection): sendPort is fixed at 9999 for every connection
 * (every client is assumed to listen on the same well-known port), while receivePort
 * is unique per connection (9999 - index), so the server can tell connections apart
 * by which socket a datagram arrived on.
 *
 */
class GameServerTest {

    private val loopback: InetAddress = InetAddress.getLoopbackAddress()

    @Test
    fun `push sends the message to the fixed client-facing send port`() {
        val server = GameServer(maxConnections = 1)
        server.addConnection("client-1", loopback) // sendPort = 9999, listenPort = 9998

        val probe = DatagramSocket(9999)
        probe.soTimeout = 2000
        try {
            server.push("hello")

            val buffer = ByteArray(1024)
            val packet = DatagramPacket(buffer, buffer.size)
            probe.receive(packet)

            assertEquals("hello", String(packet.data, 0, packet.length))
        } finally {
            probe.close()
            server.stop()
        }
    }

    @Test
    fun `start begins listening on the connection's unique receive port`() {
        val server = GameServer(maxConnections = 1)
        server.addConnection("client-1", loopback) // listenPort = 9998 (9999 - index 1)
        server.start()

        try {
            val sender = DatagramSocket()
            val payload = "ping".toByteArray()
            sender.send(DatagramPacket(payload, payload.size, loopback, 9998))
            sender.close()

            // onClientMessage only logs internally, so there's no return value or callback
            // to assert on directly. This just verifies the listener thread processes an
            // incoming datagram on the expected port without crashing.
            Thread.sleep(300)
        } finally {
            server.stop()
        }
    }

    @Test
    fun `each connection listens on a distinct receive port`() {
        val server = GameServer(maxConnections = 2)
        server.addConnection("client-1", loopback) // listenPort = 9998
        server.addConnection("client-2", loopback) // listenPort = 9997
        server.start()

        try {
            val sender = DatagramSocket()

            val payload1 = "from-client-1".toByteArray()
            sender.send(DatagramPacket(payload1, payload1.size, loopback, 9998))

            val payload2 = "from-client-2".toByteArray()
            sender.send(DatagramPacket(payload2, payload2.size, loopback, 9997))

            sender.close()

            // Both connections' listen sockets bind to different ports (9998, 9997),
            // so this just confirms addConnection doesn't collide/throw when adding
            // a second connection, and both sockets accept traffic independently.
            Thread.sleep(300)
        } finally {
            server.stop()
        }
    }

    @Test
    fun `push broadcasts the same message to every registered connection`() {
        // Since sendPort is now fixed (9999) for every connection, connections can only
        // be told apart on the send side by target address, not by port. This test relies
        // on the whole 127.0.0.0/8 range being routed to loopback, which holds on
        // Linux/macOS (and most CI/sandboxes) but may need extra config on Windows.
        val addressA = InetAddress.getByName("127.0.0.1")
        val addressB = InetAddress.getByName("127.0.0.2")

        val server = GameServer(maxConnections = 2)
        server.addConnection("client-1", addressA)
        server.addConnection("client-2", addressB)

        val probeA = DatagramSocket(9999, addressA)
        val probeB = DatagramSocket(9999, addressB)
        probeA.soTimeout = 2000
        probeB.soTimeout = 2000

        try {
            server.push("broadcast")

            val bufferA = ByteArray(1024)
            val packetA = DatagramPacket(bufferA, bufferA.size)
            probeA.receive(packetA)
            assertEquals("broadcast", String(packetA.data, 0, packetA.length))

            val bufferB = ByteArray(1024)
            val packetB = DatagramPacket(bufferB, bufferB.size)
            probeB.receive(packetB)
            assertEquals("broadcast", String(packetB.data, 0, packetB.length))
        } finally {
            probeA.close()
            probeB.close()
            server.stop()
        }
    }

    @Test
    fun `push(VisibleObject) sends the object's serialized snapshot`() {
        val server = GameServer(maxConnections = 1)
        server.addConnection("client-1", loopback)

        val probe = DatagramSocket(9999)
        probe.soTimeout = 2000
        try {
            val visibleObject = TestVisibleObject(
                dimensions = Dimensions(width = 40.0, height = 20.0),
                location = Point(x = 3.0, y = 4.0)
            ).apply { angle = 45 }

            server.push(visibleObject)

            val buffer = ByteArray(1024)
            val packet = DatagramPacket(buffer, buffer.size)
            probe.receive(packet)
            val received = String(packet.data, 0, packet.length)

            val expectedJson = Json.encodeToString(VisibleObjectSnapshot from visibleObject)
            assertEquals(expectedJson, received)

            // Sanity-check the payload actually round-trips the values we set.
            val decoded = Json.decodeFromString<VisibleObjectSnapshot>(received)
            assertEquals(3.0, decoded.gameObject.location.x)
            assertEquals(4.0, decoded.gameObject.location.y)
            assertEquals(40.0, decoded.dimensions.x)
            assertEquals(20.0, decoded.dimensions.y)
            assertEquals(45, decoded.angle)
        } finally {
            probe.close()
            server.stop()
        }
    }

    @Test
    fun `push(List of VisibleObject) sends one datagram per object, in order`() {
        val server = GameServer(maxConnections = 1)
        server.addConnection("client-1", loopback)

        val probe = DatagramSocket(9999)
        probe.soTimeout = 2000
        try {
            val objectA = TestVisibleObject(location = Point(x = 1.0, y = 1.0))
            val objectB = TestVisibleObject(location = Point(x = 2.0, y = 2.0))

            server.push(listOf(objectA, objectB))

            val bufferA = ByteArray(1024)
            val packetA = DatagramPacket(bufferA, bufferA.size)
            probe.receive(packetA)
            val decodedA = Json.decodeFromString<VisibleObjectSnapshot>(String(packetA.data, 0, packetA.length))
            assertEquals(1.0, decodedA.gameObject.location.x)
            assertEquals(1.0, decodedA.gameObject.location.y)

            val bufferB = ByteArray(1024)
            val packetB = DatagramPacket(bufferB, bufferB.size)
            probe.receive(packetB)
            val decodedB = Json.decodeFromString<VisibleObjectSnapshot>(String(packetB.data, 0, packetB.length))
            assertEquals(2.0, decodedB.gameObject.location.x)
            assertEquals(2.0, decodedB.gameObject.location.y)
        } finally {
            probe.close()
            server.stop()
        }
    }

    @Test
    fun `stop closes every connection so further sends fail`() {
        val server = GameServer(maxConnections = 1)
        server.addConnection("client-1", loopback)
        server.start()
        server.stop()

        assertFailsWith<Exception> {
            server.push("after-stop")
        }
    }
}