package gg.flyte.munch.message

import com.mongodb.client.MongoCollection
import gg.flyte.munch.Munch
import gg.flyte.munch.Munch.Companion.log
import gg.flyte.munch.server.ServerRegistry
import org.bson.Document
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MessageSubscriber(
    private val collection: MongoCollection<Document>,
    private val handler: MessageHandler,
    private val settings: Munch.Builder.SubscriberSettings
) {
    private val service = Executors.newSingleThreadScheduledExecutor()

    fun start() {
        service.scheduleAtFixedRate({
            collection.find().first()?.let { handler.handleInternally(it.asMessage()) }
        }, 0L, settings.period, TimeUnit.MILLISECONDS)

        service.scheduleAtFixedRate({
            ServerRegistry.values().forEach {
                if (Instant.ofEpochMilli(it.lastKeepAlive).isBefore(Instant.now().minusMillis(settings.timeoutServerPeriod))) {
                    log("Muncher $it timed out")
                    ServerRegistry.unregister(it.id)
                }
            }
        }, settings.timeoutCheckPeriod, settings.timeoutCheckPeriod, TimeUnit.MILLISECONDS)
    }

    fun stop() {
        service.shutdown()
    }
}