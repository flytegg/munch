package gg.flyte.munch.server

import java.util.*

object ServerRegistry {
    private val servers = mutableMapOf<UUID, Server>()

    fun register(id: UUID, name: String) = servers.put(id, Server(id, name))

    fun unregister(id: UUID) = servers.remove(id)

    fun findById(id: UUID): Server? = servers[id]

    fun findByName(name: String): Server? = servers.values.find { it.name == name }

    fun values(): Collection<Server> = servers.values
}