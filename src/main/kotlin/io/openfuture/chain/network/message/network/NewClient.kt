package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
data class NewClient(
    var nodeInfo: NodeInfo
) : Serializable {

    override fun read(buf: ByteBuf) {
        nodeInfo = NodeInfo::class.java.newInstance()
        nodeInfo.read(buf)
    }

    override fun write(buf: ByteBuf) {
        nodeInfo.write(buf)
    }

}