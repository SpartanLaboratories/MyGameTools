package com.spartanlabs.gaming.networking

/**
 * The dedicated port pair a [GameServer] handed a player in its `TXRXON` handshake reply.
 *
 * Both ports are named from the *server's* point of view, because that is how the protocol
 * reports them. A client therefore listens on [serverSendPort] and transmits to
 * [serverReceivePort] - the mirror image of what the names suggest.
 *
 * @property serverSendPort the port the server pushes to, and so the one a player listens on
 * @property serverReceivePort the port the server listens on, and so the one a player sends to
 */
internal data class ServerPorts(val serverSendPort: Int, val serverReceivePort: Int)
