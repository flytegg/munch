package gg.flyte.munch.server

import java.util.*

class Server(
    val id: UUID,
    var name: String = "Unknown server",
    var lastKeepAlive: Long = -1L
) {
    override fun toString() = "Server(id=$id, name=$name)"
}