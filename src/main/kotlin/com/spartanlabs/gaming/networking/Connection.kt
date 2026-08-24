package com.spartanlabs.gaming.networking

import com.spartanlabs.webtools.UDPSendReceiveServer
import java.net.InetAddress

internal class Connection(val name:String, address: InetAddress, sendPort: Int, receivePort: Int) {
    private val server = UDPSendReceiveServer(address, sendPort, receivePort)
    fun actuate(onMessage: (message: String) -> Unit) {
        server.startListening { message, senderAddress ->
            onMessage(message)
        }
    }
    fun terminate() = server.close()
    fun push(message: String) = server.send(message)
}