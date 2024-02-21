package gg.joshbaker.munch.message

import gg.joshbaker.munch.Munch
import gg.joshbaker.munch.Munch.Companion.log
import gg.joshbaker.munch.exception.MalformedMessageException
import gg.joshbaker.munch.server.Server
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DefaultMessageHandler(
    private val munch: Munch,
    private val providedHandler: MessageHandler
) : MessageHandler {
    private val handledMessages = mutableListOf<UUID>()
    private val cleanupService = Executors.newSingleThreadScheduledExecutor()

    fun stop() {
        cleanupService.shutdown()
        handledMessages.clear()
    }

    override fun handle(message: Message) {
        if (message.uid in handledMessages) return

        handledMessages += message.uid
        cleanupService.schedule({
            munch.clean(message.uid.toString())
            handledMessages -= message.uid
        }, munch.messageLifetime, TimeUnit.MILLISECONDS)

        val sender = message.sender ?: throw MalformedMessageException()
        if (sender == munch.server.uid) return
        if (!message.isGlobal() && munch.server.uid !in message.destinations) return

        when (message.header) {
            Message.Header.MUNCH_HANDSHAKE_CONNECT -> {
                val server = Server(message.sender ?: throw MalformedMessageException(), message.content)
                Server.servers += server.uid to server
                log(Server.servers)
                log("[DefaultMessageHandler - MUNCH_HANDSHAKE_CONNECT] Discovered new Muncher: $server")
                munch.message {
                    destinations = setOf(server.uid)
                    header = Message.Header.MUNCH_HANDSHAKE_CONFIRM
                    content = munch.server.name
                }
            }

            Message.Header.MUNCH_HANDSHAKE_CONFIRM -> {
                val server = Server(message.sender ?: throw MalformedMessageException(), message.content)
                Server.servers += server.uid to server
                log(Server.servers)
                log("[DefaultMessageHandler - MUNCH_HANDSHAKE_CONFIRM] Discovered new Muncher: $server")
            }

            else -> providedHandler.handle(message)
        }
    }
}