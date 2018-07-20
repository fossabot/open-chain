package io.openfuture.chain.network.config

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.openfuture.chain.network.client.handler.ClientChannelInitializer
import io.openfuture.chain.property.NodeProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class NioClientConfig(
    private val clientChannelInitializer: ClientChannelInitializer,
    private val properties: NodeProperty
) {

    @Bean
    fun clientBootstrap(): Bootstrap = Bootstrap()
        .group(clientGroup())
        .channel(NioSocketChannel::class.java)
        .handler(clientChannelInitializer)
        .option(ChannelOption.SO_KEEPALIVE, properties.keepAlive)

    @Bean(destroyMethod = "shutdownGracefully")
    fun clientGroup(): NioEventLoopGroup = NioEventLoopGroup()

}