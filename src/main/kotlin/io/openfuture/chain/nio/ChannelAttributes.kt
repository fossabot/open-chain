package io.openfuture.chain.nio

import io.netty.util.AttributeKey

object ChannelAttributes {
    val REMOTE_NETWORK_ID: AttributeKey<String> = AttributeKey.valueOf("REMOTE_NETWORK_ID")
}
