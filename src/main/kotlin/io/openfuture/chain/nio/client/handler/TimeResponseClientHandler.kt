package io.openfuture.chain.nio.client.handler

import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.nio.base.BaseHandler
import io.openfuture.chain.protocol.CommunicationProtocol.*
import io.openfuture.chain.service.TimeSyncService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class TimeResponseClientHandler(
        private val timeSyncService: TimeSyncService
) : BaseHandler(Type.TIME_RESPONSE){

    companion object {
        private val log = LoggerFactory.getLogger(TimeResponseClientHandler::class.java)
    }

    override fun packetReceived(ctx: ChannelHandlerContext, message: Packet) {
        log.info("Time packet received from ${ctx.channel().remoteAddress()}")
        timeSyncService.addTimeOffset(message, ctx.channel().remoteAddress().toString())
    }

}