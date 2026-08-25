import com.spartanlabs.gaming.networking.GameServer
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.test.Test

class GameServerTest {
    @Test
    fun test() {
        // Instantiate the server
        /*
            This should make the server start listening
            on the main port for any declaration messages
         */
        GameServer(5)
        println("Game server started")

        // Find our own IP address
        val address = resolveLocalAddress()
        // Write the declaration message
        val declarationMessage = "Iam InternalTestClass $address".toByteArray(Charsets.UTF_8)
        // Create the send socket that is going to be sending the declaration message
        val sendSocket = DatagramSocket()
        println("Send socket created")

        /* Now before sending the declaration message
        we need to start listening for a possible response
         */
        Thread {
            val buffer = ByteArray(1024)
            val packet = DatagramPacket(buffer, buffer.size)
            try {
                println("Starting to listen")
                DatagramSocket(9999).receive(packet)
                println("Received a common port message")
                val message = String(packet.data, 0, packet.length, Charsets.UTF_8).trim()
                println("Received common port message: $message")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
        println("Listen thread started")
        Thread.sleep(1000)
        // Send the declaration message
        sendSocket.send(DatagramPacket(declarationMessage, declarationMessage.size, address, 9998))
        println("Declaration message sent")
        sendSocket.close()

    }
    private fun resolveLocalAddress(): InetAddress =
        try {
            DatagramSocket().use { probe ->
                probe.connect(InetAddress.getByName("8.8.8.8"), 80)
                probe.localAddress
            }
        } catch (e: Exception) {
            InetAddress.getLoopbackAddress()
        }
}