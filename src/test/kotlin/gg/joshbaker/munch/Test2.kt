package gg.joshbaker.munch

fun main() {
    val munch: Munch = Munch.Builder {
        mongo {
            uri = "mongodb://localhost:27017"
            database = "munch-test"
            collection = "messages"
        }
        handler = TestMessageHandler
        server = "test02"
    }.build()

    munch.start()

    munch.message {
        header = "TEST"
        content = System.currentTimeMillis().toString()
    }

    /*munch.message {
        header = "test"
        content = "asdasdasd"
    }

    munch.stop()*/
}
