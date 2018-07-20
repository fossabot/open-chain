package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor

@NoArgConstructor
data class HeartBeat(
    var type: Type
) : Packet() {

    enum class Type { PING, PONG }

    override fun get(buffer: ByteBuf) {
        super.get(buffer)

        type = if (buffer.readBoolean()) Type.PING else Type.PONG
    }

    override fun send(buffer: ByteBuf) {
        super.send(buffer)

        buffer.writeBoolean(type == Type.PING)
    }

}
