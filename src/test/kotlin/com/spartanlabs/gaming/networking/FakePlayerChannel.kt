package com.spartanlabs.gaming.networking

//region 2. Intended Function
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
//endregion

/**
 * The client end of a single player's dedicated connection, once the handshake has handed
 * out a [ServerPorts] pair.
 *
 * Only one of these can exist per server: the port allocation in `MultiConnectionUDPServer`
 * steps by one rather than two, so a second player's [ServerPorts.serverSendPort] is the same
 * number as the first player's [ServerPorts.serverReceivePort], which the server itself is
 * already bound to on this host. Tests that need several players therefore handshake them but
 * only open a channel for one.
 *
 * @param ports the dedicated ports the server allocated to this player
 */
internal class FakePlayerChannel(private val ports: ServerPorts) : AutoCloseable {

    /** Everything happens over loopback, matching [FakeClientHarness.address]. */
    private val address: InetAddress = InetAddress.getLoopbackAddress()

    /**
     * Bound to the port the server pushes to. Binding happens in this constructor rather than
     * on first read, so datagrams the server sends before a test calls [receive] are buffered
     * by the OS instead of being dropped.
     */
    private val incoming = DatagramSocket(ports.serverSendPort)

    /**
     * Sends a message to the server on this player's dedicated connection.
     * @param message the text to send
     * @return [Result.success] if the datagram was sent, or the failure that prevented it
     */
    fun send(message: String): Result<Unit> = runCatching {
        DatagramSocket().use { socket ->
            message.toByteArray(Charsets.UTF_8).let { payload ->
                socket.send(DatagramPacket(payload, payload.size, address, ports.serverReceivePort))
            }
        }
    }

    /**
     * Reads the next message the server pushed to this player.
     * @param timeoutMillis how long to wait before giving up
     * @return the decoded message, or the failure that prevented reading one
     */
    fun receive(timeoutMillis: Int = RECEIVE_TIMEOUT_MILLIS): Result<String> = runCatching {
        incoming.soTimeout = timeoutMillis
        val packet = DatagramPacket(ByteArray(BUFFER_BYTES), BUFFER_BYTES)
        incoming.receive(packet)
        String(packet.data, 0, packet.length, Charsets.UTF_8).trim()
    }

    /** Releases the dedicated port so the next test can bind it. */
    override fun close() {
        incoming.close()
    }

    companion object {
        /** How long to wait for a pushed message before declaring the read failed. */
        private const val RECEIVE_TIMEOUT_MILLIS = 4000

        /** Size of the buffer incoming datagrams are read into. */
        private const val BUFFER_BYTES = 8192
    }
}
