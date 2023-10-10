# mongodb-message
Misuse a MongoDB collection as a messaging service, sending encoded packets as documents between subscribed destinations.

## This project is not in a working state.

### How to use

You can initialise a messaging instance like so:
```kt
object ExamplePacketHandler : PacketHandler {
    override fun handle(packet: Packet) {
        println("received packet $packet")
    }
}

val collection: MongoCollection<Document> = ...
val mongoDBMessage = MongoDBMessage(collection, ExamplePacketHandler)
```

To start subscribing and publishing, simply call `.start()` like so:
```kt
mongoDBMessage.start()
```

To add packets to the out-going publisher queue, you can call `.queue()` on your messaging instance:
```kt
mongoDBMessage.queue(Packet("TEST", "Example Packet Content")) // You can add multiple by separating with commas
```

Finally, on shutdown of your system/service/plugin you can call `.close()`:
```kt
mongoDBMessage.close()
```
This will close the publisher and subscriber jobs.
