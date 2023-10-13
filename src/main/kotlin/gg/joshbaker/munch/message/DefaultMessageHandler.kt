package gg.joshbaker.munch.message

import gg.joshbaker.munch.Munch
import gg.joshbaker.munch.exception.MalformedConnectionException
import gg.joshbaker.munch.server.Server

class DefaultMessageHandler(
    private val munch: Munch,
    private val providedHandler: MessageHandler
) : MessageHandler {
    override fun handle(message: Message) {
        println("[DefaultMessageHandler] Received: $message")
        when (message.header) {
            "HANDSHAKE_CONNECT" -> {
                val server = Server(message.sender ?: throw MalformedConnectionException(), message.content)
                Server.servers += server.uid to server
                println("[DefaultMessageHandler] Discovered new Muncher: $server")
                munch.message {
                    destinations = listOf(server.uid)
                    header = "HANDSHAKE_CONFIRM"
                    content = munch.server.name
                }
            }

            "HANDSHAKE_CONFIRM" -> {
                val server = Server(message.sender ?: throw MalformedConnectionException(), message.content)
                Server.servers += server.uid to server
                println("[DefaultMessageHandler] Discovered new Muncher: $server")
            }

            "DELETE" -> munch.clean(message.content)

            else -> providedHandler.handle(message)
        }
    }
}