package gg.flyte.munch.message

import gg.flyte.munch.Munch
import gg.flyte.munch.Munch.Companion.log
import gg.flyte.munch.exception.MalformedMessageException
import gg.flyte.munch.exception.UnknownServerException
import gg.flyte.munch.server.ServerRegistry
import java.time.Instant
import java.util.*

open class MessageHandler {
    private lateinit var munch: Munch

    fun injectMunch(munch: Munch) {
        this.munch = munch
    }

    private val handledMessages = mutableListOf<UUID>()

    fun removeHandledMessage(id: UUID) = handledMessages.remove(id)

    fun stop() {
        handledMessages.clear()
    }

    fun handleInternally(message: Message) {
        if (message.uid in handledMessages) return
        handledMessages += message.uid

        val sender = message.sender ?: throw MalformedMessageException()

        if (sender == munch.server.id) return
        if (!message.isGlobal() && munch.server.id !in message.destinations) return

        when (message.header) {

            Message.Header.MUNCH_HANDSHAKE_CONNECT -> with(ServerRegistry.register(sender, message.content)) {
                log("Discovered new Muncher: $this")

                munch.message {
                    destinations = setOf(id)
                    header = Message.Header.MUNCH_HANDSHAKE_CONFIRM
                    content = munch.server.name
                }
            }

            Message.Header.MUNCH_HANDSHAKE_CONFIRM -> ServerRegistry.register(sender, message.content).also {
                log("Discovered new Muncher: $it")
            }

            Message.Header.MUNCH_HANDSHAKE_END -> ServerRegistry.findById(sender)?.run {
                ServerRegistry.unregister(sender)
                log("Muncher $this disconnected")
            } ?: throw UnknownServerException(sender)

            Message.Header.MUNCH_HANDSHAKE_KEEPALIVE -> ServerRegistry.findById(sender)?.run {
                log("Received keep-alive from $this")
                lastKeepAlive = Instant.now().toEpochMilli()
            } ?: throw UnknownServerException(sender)

            else -> handle(message)

        }
    }

    open fun handle(message: Message) {
        throw NotImplementedError("Unable to handle $message as a MessageHandler implementation has not been provided")
    }
}