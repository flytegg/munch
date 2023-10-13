package gg.joshbaker.munch.server

import java.util.*

data class Server(
    val uid: UUID,
    var name: String = "Unknown server"
) {
    companion object {
        val servers = mutableMapOf<UUID, Server>()

        fun findServerByUid(uid: UUID): Server? = servers[uid]
    }
}