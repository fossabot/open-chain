package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor
import java.nio.charset.StandardCharsets.*

@NoArgConstructor
data class NetworkAddress(
    var host: String,
    var port: Int) {

    fun get(buffer: ByteBuf) {
        host = buffer.readCharSequence(buffer.readInt(), UTF_8).toString()
        port = buffer.readInt()
    }

    fun send(buffer: ByteBuf) {
        buffer.writeInt(host.length)
        buffer.writeCharSequence(host, UTF_8)
        buffer.writeInt(port)
    }

}