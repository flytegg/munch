package gg.joshbaker.mongodbmessage

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import gg.joshbaker.mongodbmessage.packet.Packet
import gg.joshbaker.mongodbmessage.packet.PacketHandler
import org.bson.Document
import java.util.*

fun main() {
    val mongoClient = MongoClients.create("mongodb://localhost:27017")
    val database: MongoDatabase = mongoClient.getDatabase("messages")
    val collection: MongoCollection<Document> = database.getCollection("messages")

    val mongoDBMessage = MongoDBMessage(collection, TestPacketHandler)
    mongoDBMessage.start()

    //mongoDBMessage.queue(Packet(UUID.randomUUID(), "TEST", "loololol"))
}

object TestPacketHandler : PacketHandler {
    override fun handle(packet: Packet) {
        println("received packet $packet")
    }
}