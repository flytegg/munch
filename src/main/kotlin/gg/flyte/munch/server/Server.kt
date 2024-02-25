package gg.flyte.munch.server

import java.util.*

data class Server(
    val id: UUID,
    var name: String = "Unknown server",
    var lastKeepAlive: Long = -1L
)