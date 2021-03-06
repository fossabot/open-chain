package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.sync.SyncManager
import io.openfuture.chain.core.sync.SyncStatus.SYNCHRONIZED
import io.openfuture.chain.network.serialization.Serializable
import org.springframework.stereotype.Component

@Component
@Sharable
class SyncStatusHandler(
    private val syncManager: SyncManager

) : SimpleChannelInboundHandler<Serializable>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: Serializable) {
        if (SYNCHRONIZED == syncManager.getStatus()) {
            ctx.fireChannelRead(msg)
        }
    }

}