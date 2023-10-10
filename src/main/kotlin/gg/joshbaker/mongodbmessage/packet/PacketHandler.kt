package gg.joshbaker.mongodbmessage.packet

interface PacketHandler {
    fun handle(packet: Packet)
}