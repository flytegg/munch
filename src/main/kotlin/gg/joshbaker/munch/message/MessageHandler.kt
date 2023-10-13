package gg.joshbaker.munch.message

interface MessageHandler {
    fun handle(message: Message)
}