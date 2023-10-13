package gg.joshbaker.munch.message

import gg.joshbaker.munch.Munch
import org.bson.Document
import java.util.*

data class Message(
    private val uid: UUID = UUID.randomUUID(),
    val destinations: List<UUID>,
    var sender: UUID? = null,
    val header: String,
    val content: String
) {
    fun asDocument(): Document {
        return Document().apply {
            this["_id"] = uid.toString()
            this["destinations"] = destinations.asStringList()
            this["header"] = header.uppercase()
            this["content"] = content
        }
    }

    fun isGlobal() = Munch.NULL_UUID in destinations

    class Builder(init: Builder.() -> Unit) {
        init {
            apply(init)
        }

        var destinations: List<UUID> = emptyList()
        var header: String? = null
        var content: String? = null

        fun build() = Message(
            destinations = destinations.takeIf { it.isNotEmpty() } ?: listOf(Munch.NULL_UUID),
            header = header ?: throw IllegalStateException("header is null. All fields must be initialized."),
            content = content ?: throw IllegalStateException("content is null. All fields must be initialized.")
        )
    }
}

fun List<UUID>.asStringList(): List<String> {
    return map { it.toString() }
}

fun List<String>.asUUIDList(): List<UUID> {
    return map { UUID.fromString(it) }
}

fun Document.asMessage(): Message {
    return Message(
        uid = UUID.fromString(getString("_id")),
        destinations = getList("destinations", String::class.java).asUUIDList(),
        sender = UUID.fromString(getString("sender")),
        header = getString("header"),
        content = getString("content")
    )
}