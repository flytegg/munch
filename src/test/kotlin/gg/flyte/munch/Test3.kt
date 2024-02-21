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
        server = "test03"

        publisherPeriod = 1000
        subscriberPeriod = 1000
    }.build()

    munch.start()

    munch.message {
        header = "test"
        content = "asdasdasd"
    }

    //munch.stop()
}
