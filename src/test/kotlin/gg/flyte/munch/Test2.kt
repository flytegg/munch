package gg.flyte.munch

import io.github.cdimascio.dotenv.Dotenv

fun main() {
    val env = Dotenv.load()
    val munch: Munch = Munch.Builder {
        mongo {
            uri = env.get("MONGO_URI")
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
    }*/

    munch.stop()
}
