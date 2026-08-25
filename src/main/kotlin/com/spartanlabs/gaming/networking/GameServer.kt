package com.spartanlabs.gaming.networking

// Organizational
// Spartan Gaming
import com.spartanlabs.gaming.gameobjects.VisibleObject
import com.spartanlabs.gaming.gameobjects.VisibleObjectSnapshot

// Intended Function
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.net.InetAddress
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketException

// Programming Generics (Tests, Logging, Profiling)
import org.slf4j.LoggerFactory
import org.slf4j.Logger

val log: Logger = LoggerFactory.getLogger("GameServer")

class GameServer(maxConnections: Int) {
    private val listening = true
    private var commonListenerThread: Thread? = null
    private val commonListenSocket = DatagramSocket(9998)
    private val commonSendPort = 9999
    private val commonSendSocket = DatagramSocket()
    init{
        commonListenerThread = Thread {
            val buffer = ByteArray(1024)
            while (listening) {
                try {
                    val packet = DatagramPacket(buffer, buffer.size)
                    commonListenSocket.receive(packet)
                    println("The server has received a message on the common listen port")
                    val text = String(packet.data, 0, packet.length, Charsets.UTF_8).trim()
                    println("The message is $text")
                    val message = text.split(' ')
                    when(message[0]) {
                        "Iam" -> {
                            println("Normal Communication Detected: ${message[0]}")
                            val address = InetAddress.getByName(message[2].removePrefix("/"))
                            println("Address: $address")
                            val connection = addConnection(message[1], address)
                            val declarationResponse = "$address TXRXON ${connection.sendPort} ${connection.receivePort}".toByteArray(Charsets.UTF_8)
                            commonListenSocket.send(DatagramPacket(declarationResponse, declarationResponse.size, address, commonSendPort))
                        }
                    }
                } catch (e: SocketException) {
                    break // socket was closed - stop listening
                } catch (e: Exception) {
                    println("Failed to handle incoming datagram: ${e.message}")
                }
            }
        }.apply {
            isDaemon = true
            start()
        }
    }

    private val connections = arrayListOf<Connection>()
    private fun addConnection(name:String, address: InetAddress):Connection{
        val portOffset = connections.size + 2
        val connection = Connection(name, address, 9999-portOffset, 9999-portOffset-1)
        connections.add(connection)
        return connection
    }
    fun start(onClientMessage:(String)->Unit) = connections.forEach{ connection -> connection.actuate(onClientMessage)}

    fun push(message: String) = connections.forEach { connection ->  connection.push(message) }
    fun push(visibleObject: VisibleObject) =         push(Json.encodeToString(VisibleObjectSnapshot from visibleObject))
    fun push(visibleObjects : List<VisibleObject>) = push(Json.encodeToString(visibleObjects.map { VisibleObjectSnapshot from it }))

    fun stop() {
        connections.forEach { connection -> connection.terminate() }
        commonListenerThread?.join(1000)
        commonListenSocket.close()
    }

}