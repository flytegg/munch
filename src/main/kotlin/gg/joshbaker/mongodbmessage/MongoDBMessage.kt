package gg.joshbaker.mongodbmessage

import com.mongodb.client.MongoCollection
import gg.joshbaker.mongodbmessage.packet.Packet
import gg.joshbaker.mongodbmessage.packet.PacketHandler
import gg.joshbaker.mongodbmessage.packet.asPacket
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bson.Document
import java.util.*

class MongoDBMessage(
    private val collection: MongoCollection<Document>,
    private val packetHandler: PacketHandler
) {
    private var subscriber: Job? = null
    private var publisher: Job? = null

    fun start() {
        runBlocking {
            val changeStream = collection.watch()
            subscriber = launch {
                while (true) {
                    println("sub")
                    changeStream.iterator().tryNext()?.fullDocument?.let {
                        collection.deleteOne(it)
                        packetHandler.handle(it.asPacket())
                    }
                }
            }

            publisher = launch {
                while (true) {
                    println("pub")
                    queue.poll()?.let {
                        collection.insertOne(it.asDocument())
                        println("published packet $it")
                    }
                }
            }
        }
    }

    fun close() {
        subscriber?.cancel()
        publisher?.cancel()
    }

    private val queue: Queue<Packet> = LinkedList()

    fun queue(vararg packets: Packet) {
        queue.addAll(packets)
    }
}