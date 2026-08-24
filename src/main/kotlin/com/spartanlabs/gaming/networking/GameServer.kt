package com.spartanlabs.gaming.networking

// Intended Function
import com.spartanlabs.gaming.gameobjects.VisibleObject
import com.spartanlabs.gaming.gameobjects.VisibleObjectSnapshot
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.net.InetAddress

// Programming Generics (Tests, Logging, Profiling)
import org.slf4j.LoggerFactory
import org.slf4j.Logger
val log: Logger = LoggerFactory.getLogger("GameServer")

class GameServer(maxConnections: Int) {
    private val connections = arrayListOf<Connection>()
    fun addConnection(name:String, address: InetAddress){
        val index = connections.size + 1
        connections.add(Connection(name, address, 9999, 9999-index))
    }
    fun start() = connections.forEach{ connection -> connection.actuate(::onClientMessage) }
    fun stop()  = connections.forEach{ connection -> connection.terminate() }
    fun push(message: String) = connections.forEach { connection ->  connection.push(message) }
    fun push(visibleObject: VisibleObject) = push(Json.encodeToString(VisibleObjectSnapshot from visibleObject))
    fun push(visibleObjects : List<VisibleObject>) = visibleObjects.forEach { push(it) }

    private fun onClientMessage(message: String) {
        log.info("A connection has received the following message: $message")
    }
}