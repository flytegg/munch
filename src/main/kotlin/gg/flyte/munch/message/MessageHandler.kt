package gg.flyte.munch.message

interface MessageHandler {
    fun handle(message: Message)
}