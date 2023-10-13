package gg.joshbaker.munch

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters.eq
import gg.joshbaker.munch.message.DefaultMessageHandler
import gg.joshbaker.munch.message.Message
import gg.joshbaker.munch.message.MessageHandler
import gg.joshbaker.munch.message.asMessage
import gg.joshbaker.munch.server.Server
import org.bson.Document
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates


class Munch(
    private val collection: MongoCollection<Document>,
    val server: Server,
    handler: MessageHandler
) {
    private val defaultHandler: MessageHandler

    private val messageQueue: Queue<Message> = LinkedList()

    fun message(builder: Message.Builder.() -> Unit) {
        messageQueue += Message.Builder(builder).build().apply { sender = server.uid }
        println(messageQueue)
    }

    init {
        defaultHandler = DefaultMessageHandler(this, handler)
        message {
            header = "HANDSHAKE_CONNECT"
            content = server.name
        }
    }

    private var publisher: ScheduledExecutorService? = null
    private var subscriber: ScheduledExecutorService? = null

    fun start() {
        publisher = Executors.newSingleThreadScheduledExecutor().apply {
            scheduleAtFixedRate({
                println("pub")
                messageQueue.poll()?.let { collection.insertOne(it.asDocument()) }
            }, 0L, 50L, TimeUnit.MILLISECONDS)
        }

        /*
         Subscriber is blocking and not continuing to run?

         Assume the Document.asMessage() call is producing errors but they are consumed by the executor service so not shown in terminal
         */
        subscriber = Executors.newSingleThreadScheduledExecutor().apply {
            scheduleAtFixedRate({
                println("sub")
                collection.find(
                    /*and(
                        eq("sender", server.uid.toString()),
                        not(elemMatch("destinations", eq("\$in", server.uid.toString())))
                    )*/
                ).first()?.let {
                    defaultHandler.handle(it.asMessage())
                }
            }, 0L, 50L, TimeUnit.MILLISECONDS)
        }
    }

    fun stop() {
        publisher?.shutdown()
        subscriber?.shutdown()
    }

    fun clean(uid: String) {
        collection.deleteOne(eq("_id", uid))
    }

    companion object {
        val NULL_UUID = UUID(0, 0)
    }

    class Builder(init: Builder.() -> Unit) {
        private var mongo by Delegates.notNull<Mongo>()

        inner class Mongo {
            var uri by Delegates.notNull<String>()
            var database by Delegates.notNull<String>()
            var collection by Delegates.notNull<String>()
        }

        fun mongo(init: Mongo.() -> Unit) {
            mongo = Mongo().apply(init)
        }

        var handler by Delegates.notNull<MessageHandler>()
        var server by Delegates.notNull<String>()

        init {
            apply(init)
        }

        fun build(): Munch {
            val collection = MongoClients.create(mongo.uri).getDatabase(mongo.database).getCollection(mongo.collection)
            val server = Server(UUID.randomUUID(), server)
            Server.servers += server.uid to server

            return Munch(collection, server, handler)
        }
    }
}

/*private var subscriber: ScheduledExecutorService? = null
private var publisher: ScheduledExecutorService? = null

fun start() {
    subscriber = Executors.newSingleThreadScheduledExecutor().apply {
        scheduleAtFixedRate({
            while (true) {
                collection.find(Filters.eq("destination", )).first()?.let {
                    packetHandler.handle(it.asPacket())
                    collection.deleteOne(it)
                }
                sleep(50)
            }
        }, 0L, 50L, TimeUnit.MILLISECONDS)
    }

    publisher = Executors.newSingleThreadScheduledExecutor().apply {
        scheduleAtFixedRate({
            while (true) {
                queue.poll()?.let {
                    collection.insertOne(it.asDocument())
                    println("published packet $it")
                }
                sleep(50)
            }
        }, 0L, 50L, TimeUnit.MILLISECONDS)
    }
}

fun close() {
    subscriber?.shutdown()
    publisher?.shutdown()
}

private val queue: Queue<Packet> = LinkedList()

fun queue(vararg packets: Packet) {
    queue.addAll(packets)
}
}*/