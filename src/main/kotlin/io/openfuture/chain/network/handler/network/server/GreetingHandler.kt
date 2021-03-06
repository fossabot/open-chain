package io.openfuture.chain.network.handler.network.server

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.component.ExplorerAddressesHolder
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.message.network.GreetingMessage
import io.openfuture.chain.network.message.network.GreetingResponseMessage
import io.openfuture.chain.network.message.network.NewClient
import org.springframework.stereotype.Component
import java.net.InetSocketAddress

@Component
@Sharable
class GreetingHandler(
    private val nodeKeyHolder: NodeKeyHolder,
    private val channelHolder: ChannelsHolder,
    private val explorerAddressesHolder: ExplorerAddressesHolder
) : SimpleChannelInboundHandler<GreetingMessage>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: GreetingMessage) {
        val channel = ctx.channel()
        val hostAddress = (channel.remoteAddress() as InetSocketAddress).address.hostAddress
        val nodeInfo = NodeInfo(msg.uid, NetworkAddress(hostAddress, msg.externalPort))

        if (nodeKeyHolder.getUid() == msg.uid) {
            explorerAddressesHolder.me = nodeInfo
            ctx.channel().close()
            return
        }

        if (channelHolder.getNodesInfo().any { it.uid == nodeInfo.uid }) {
            ctx.close()
            return
        }

        channelHolder.broadcast(NewClient(nodeInfo))
        ctx.writeAndFlush(GreetingResponseMessage(nodeKeyHolder.getUid(), hostAddress, explorerAddressesHolder.getNodesInfo()))

        channelHolder.addChannel(channel, nodeInfo)
        explorerAddressesHolder.addNodeInfo(nodeInfo)
    }

}