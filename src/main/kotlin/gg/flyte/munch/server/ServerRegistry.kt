package gg.flyte.munch.server

import java.util.*

object ServerRegistry {
    val servers = mutableMapOf<UUID, Server>()

    fun findServerByUid(uid: UUID): Server? = servers[uid]
}