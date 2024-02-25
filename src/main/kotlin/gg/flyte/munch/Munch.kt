package gg.flyte.munch

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import gg.flyte.munch.message.Message
import gg.flyte.munch.message.MessageHandler
import gg.flyte.munch.message.MessagePublisher
import gg.flyte.munch.message.MessageSubscriber
import gg.flyte.munch.server.Server
import org.bson.Document
import java.util.*
import kotlin.properties.Delegates

class Munch private constructor(
    private val collection: MongoCollection<Document>,
    val server: Server,
    val handler: MessageHandler = MessageHandler(),
    private val publisherSettings: Builder.PublisherSettings,
    private val subscriberSettings: Builder.SubscriberSettings,
    logLevel: LogLevel
) {
    private val publisher: MessagePublisher = MessagePublisher(this, collection, handler, publisherSettings)
    private val subscriber: MessageSubscriber = MessageSubscriber(collection, handler, subscriberSettings)

    init {
        Companion.server = server
        Companion.logLevel = logLevel

        handler.injectMunch(this)

        message {
            header = Message.Header.MUNCH_HANDSHAKE_CONNECT
            content = server.name
        }
    }

    fun start() {
        publisher.start()
        subscriber.start()
    }

    fun stop() {
        message {
            header = Message.Header.MUNCH_HANDSHAKE_END
            content = "Muncher disconnected"
        }
        publisher.stop()
        subscriber.stop()
        handler.stop()
    }

    fun message(builder: Message.Builder.() -> Unit): Message = Message.Builder(builder).build()
        .apply { sender = server.id }
        .also { publisher.queue(it) }

    fun terminate() {
        publisher.terminate()
        subscriber.stop()
        handler.stop()
    }

    enum class LogLevel {
        LOW, MEDIUM, HIGH
    }

    companion object {
        val NULL_UUID = UUID(0, 0)
        private lateinit var server: Server
        private lateinit var logLevel: LogLevel

        fun log(message: Any, level: LogLevel = LogLevel.MEDIUM) {
            if (level >= logLevel) println("[${server.name} - ${server.id}] $message")
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

        inner class PublisherSettings {
            var period = 1L
            var messageLifetime = 500L
            var keepAlivePeriod = 5000L
        }

        fun publisher(init: PublisherSettings.() -> Unit) = PublisherSettings().apply(init)

        inner class SubscriberSettings {
            var period = 1L
            var timeoutCheckPeriod = 1000L
            var timeoutServerPeriod = 10000L
        }

        fun subscriber(init: SubscriberSettings.() -> Unit) = SubscriberSettings().apply(init)

        var handler by Delegates.notNull<MessageHandler>()
        var server by Delegates.notNull<String>()
        private var publisherSettings: PublisherSettings? = null
        private var subscriberSettings: SubscriberSettings? = null
        var logLevel: LogLevel = LogLevel.MEDIUM

        init {
            apply(init)
        }

        fun build(): Munch {
            return Munch(
                collection = MongoClients.create(mongo.uri).getDatabase(mongo.database).getCollection(mongo.collection),
                server = Server(UUID.randomUUID(), server),
                handler = handler,
                publisherSettings = publisherSettings ?: PublisherSettings(),
                subscriberSettings = subscriberSettings ?: SubscriberSettings(),
                logLevel = logLevel
            )
        }
    }
}
