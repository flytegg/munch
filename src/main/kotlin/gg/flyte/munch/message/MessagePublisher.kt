package gg.flyte.munch.message

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import gg.flyte.munch.Munch
import gg.flyte.munch.Munch.Companion.log
import org.bson.Document
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MessagePublisher(
    private val collection: MongoCollection<Document>,
    private val handler: MessageHandler,
    private val settings: Munch.Builder.PublisherSettings
) {
    private val service = Executors.newSingleThreadScheduledExecutor()
    private val cleanup = Executors.newSingleThreadScheduledExecutor()
    private val queue: Queue<Message> = LinkedList()

    fun start() {
        service.scheduleAtFixedRate({
            queue.poll()?.let { publish(it) }
        }, 0L, settings.period, TimeUnit.MILLISECONDS)
    }

    fun stop() {
        waitUntilEmpty().thenRun(this::terminate)
    }

    fun terminate() {
        service.shutdown()
        cleanup.shutdown()
    }

    fun queue(message: Message) = queue.add(message)

    private fun publish(message: Message) {
        with(message) {
            log("Published $this")
            collection.insertOne(asDocument())
            clean(this)
        }
    }

    private fun clean(message: Message) {
        with(message) {
            cleanup.schedule({
                collection.deleteOne(Filters.eq("_id", uid.toString()))
                handler.removeHandledMessage(uid)
            }, settings.messageLifetime, TimeUnit.MILLISECONDS)
        }
    }

    private fun waitUntilEmpty(): CompletableFuture<Void> = CompletableFuture<Void>().apply {
        fun check() {
            if (queue.isNotEmpty()) CompletableFuture.delayedExecutor(100, TimeUnit.MILLISECONDS).execute { check() }
            else complete(null)
        }

        check()
    }
}