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

class GameServer(maxConnections: Int) :  {

}