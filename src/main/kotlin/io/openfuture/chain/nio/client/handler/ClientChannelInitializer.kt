package io.openfuture.chain.nio.client.handler

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.protobuf.ProtobufDecoder
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import io.openfuture.chain.response.GetTimeResponseProto
import org.springframework.stereotype.Component

/**
 * @author Evgeni Krylov
 */
@Component
class ClientChannelInitializer(
    private val clientHandler: ClientHandler
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        ch.pipeline()
                .addLast(ProtobufDecoder(GetTimeResponseProto.GetTimeResponse.getDefaultInstance()))
                .addLast(StringEncoder())
                .addLast(clientHandler)
    }

}