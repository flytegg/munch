package gg.joshbaker.mongodbmessage.packet

import org.bson.Document
import java.util.UUID

data class Packet(
    val uid: UUID,
    val header: String,
    val content: String
) {
    fun asDocument(): Document {
        return Document().apply {
            this["_id"] = uid.toString()
            this["header"] = header
            this["content"] = content
        }
    }
}

fun Document.asPacket(): Packet {
    return Packet(
        UUID.fromString(getString("_id")),
        getString("header"),
        getString("content")
    )
}