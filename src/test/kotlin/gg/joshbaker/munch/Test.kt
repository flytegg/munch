package gg.joshbaker.munch

import gg.joshbaker.munch.message.Message
import gg.joshbaker.munch.message.MessageHandler

fun main() {
    val munch: Munch = Munch.Builder {
        mongo {
            uri = "mongodb://localhost:27017"
            database = "munch-test"
            collection = "messages"
        }
        handler = TestMessageHandler
        server = "test01"
    }.build()

    munch.start()

    /*munch.message {
        header = "test"
        content = "asdasdasd"
    }

    munch.stop()*/
}

object TestMessageHandler : MessageHandler {
    override fun handle(message: Message) {
        println("received message $message")
    }
}