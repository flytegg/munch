package gg.flyte.munch.message

import gg.flyte.munch.Munch
import org.bson.Document
import java.util.*

data class Message(
    val id: UUID = UUID.randomUUID(),
    val destinations: Set<UUID>,
    var sender: UUID? = null,
    val header: String,
    val content: String,
) {
    fun asDocument(): Document {
        return Document().apply {
            this["_id"] = id.toString()
            this["destinations"] = destinations.asStringSet()
            this["sender"] = sender.toString()
            this["header"] = header.uppercase()
            this["content"] = content
        }
    }

    fun isGlobal() = Munch.NULL_UUID in destinations

    class Header {
        companion object {
            const val MUNCH_HANDSHAKE_CONNECT = "MUNCH_HANDSHAKE_CONNECT"
            const val MUNCH_HANDSHAKE_CONFIRM = "MUNCH_HANDSHAKE_CONFIRM"
            const val MUNCH_HANDSHAKE_KEEPALIVE = "MUNCH_HANDSHAKE_KEEPALIVE"
            const val MUNCH_HANDSHAKE_END = "MUNCH_HANDSHAKE_END"
        }
    }

    class Builder(init: Builder.() -> Unit) {
        init {
            apply(init)
        }

        var destinations: Set<UUID>? = null
        var header: String? = null
        var content: String? = null

        fun build() = Message(
            destinations = destinations.takeIf { !it.isNullOrEmpty() } ?: setOf(Munch.NULL_UUID),
            header = header ?: throw IllegalStateException("header is null. All fields must be initialized."),
            content = content ?: throw IllegalStateException("content is null. All fields must be initialized.")
        )
    }
}

fun Set<UUID>.asStringSet(): Set<String> {
    return map { it.toString() }.toSet()
}

fun List<String>.asUUIDSet(): Set<UUID> {
    return map { UUID.fromString(it) }.toSet()
}

fun Document.asMessage(): Message {
    return Message(
        id = UUID.fromString(getString("_id")),
        destinations = getList("destinations", String::class.java).asUUIDSet(),
        sender = UUID.fromString(getString("sender")),
        header = getString("header"),
        content = getString("content")
    )
}