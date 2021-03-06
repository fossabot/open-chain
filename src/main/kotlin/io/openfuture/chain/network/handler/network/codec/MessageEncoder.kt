package io.openfuture.chain.network.handler.network.codec

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import io.openfuture.chain.network.component.time.Clock
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.message.base.MessageType
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.serialization.Serializable
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Sharable
class MessageEncoder(
    private val nodeProperties: NodeProperties,
    private val clock: Clock
) : MessageToByteEncoder<Serializable>() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(MessageEncoder::class.java)
    }


    override fun encode(ctx: ChannelHandlerContext, message: Serializable, buf: ByteBuf) {
        log.trace("Encoding ${ToStringBuilder.reflectionToString(message, SHORT_PREFIX_STYLE)} " +
            "to ${ctx.channel().remoteAddress()}")

        buf.writeString(nodeProperties.protocolVersion!!)
        buf.writeLong(clock.currentTimeMillis())
        buf.writeByte(MessageType.get(message).id.toInt())
        message.write(buf)
    }

}