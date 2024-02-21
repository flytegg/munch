package gg.flyte.munch

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters.eq
import gg.flyte.munch.message.DefaultMessageHandler
import gg.flyte.munch.message.Message
import gg.flyte.munch.message.MessageHandler
import gg.flyte.munch.message.asMessage
import gg.flyte.munch.server.Server
import org.bson.Document
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class Munch private constructor(
    private val collection: MongoCollection<Document>,
    val server: Server,
    handler: MessageHandler,
    private val publisherPeriod: Long,
    private val subscriberPeriod: Long,
    val messageLifetime: Long,
) {
    private val defaultHandler: DefaultMessageHandler
    private val messageQueue: Queue<Message> = LinkedList()

    fun message(builder: Message.Builder.() -> Unit) {
        messageQueue += Message.Builder(builder).build().apply { sender = server.uid }
    }

    init {
        Companion.server = server
        defaultHandler = DefaultMessageHandler(this, handler)
        message {
            header = Message.Header.MUNCH_HANDSHAKE_CONNECT
            content = server.name
        }
    }

    private var publisher: ScheduledExecutorService? = null
    private var subscriber: ScheduledExecutorService? = null

    fun start() {
        publisher = Executors.newSingleThreadScheduledExecutor().apply {
            scheduleAtFixedRate({
                messageQueue.poll()?.let {
                   log("Published $it")
                    collection.insertOne(it.asDocument())
                }
            }, 0L, publisherPeriod, TimeUnit.MILLISECONDS)
        }

        subscriber = Executors.newSingleThreadScheduledExecutor().apply {
            scheduleAtFixedRate({
                collection.find().forEach { defaultHandler.handle(it.asMessage()) }
            }, 0L, subscriberPeriod, TimeUnit.MILLISECONDS)
        }
    }

    fun stop() {
        message {
            header = Message.Header.MUNCH_HANDSHAKE_END
            content = "Muncher disconnected"
        }
        publisher?.shutdown()
        subscriber?.shutdown()
        defaultHandler.stop()
    }

    fun clean(uid: String) = collection.deleteOne(eq("_id", uid))

    companion object {
        val NULL_UUID = UUID(0, 0)
        private lateinit var server: Server

        fun log(message: Any) {
            println("[${server.name} - ${server.uid}] $message")
        }
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
        var publisherPeriod = 1L
        var subscriberPeriod = 1L
        var messageLifetime = 500L

        init {
            apply(init)
        }

        fun build(): Munch {
            val collection = MongoClients.create(mongo.uri).getDatabase(mongo.database).getCollection(mongo.collection)
            val server = Server(UUID.randomUUID(), server)
            return Munch(collection, server, handler, publisherPeriod, subscriberPeriod, messageLifetime)
        }
    }
}
