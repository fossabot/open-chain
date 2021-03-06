package io.openfuture.chain.network.message.base

import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import io.openfuture.chain.network.message.network.*
import io.openfuture.chain.network.message.sync.*
import io.openfuture.chain.network.serialization.Serializable
import kotlin.reflect.KClass

enum class MessageType(
    val id: Byte,
    val clazz: KClass<out Serializable>
) {

    // network
    HEART_BEAT(1, HeartBeatMessage::class),
    REQUEST_TIME(2, RequestTimeMessage::class),
    RESPONSE_TIME(3, ResponseTimeMessage::class),
    GREETING(4, GreetingMessage::class),
    GREETING_RESPONSE(5, GreetingResponseMessage::class),
    NEW_CLIENT(6, NewClient::class),
    // core
    TRANSFER_TRANSACTION(7, TransferTransactionMessage::class),
    DELEGATE_TRANSACTION(8, DelegateTransactionMessage::class),
    VOTE_TRANSACTION(9, VoteTransactionMessage::class),
    // consensus
    BLOCK_APPROVAL(10, BlockApprovalMessage::class),
    PENDING_BLOCK(11, PendingBlockMessage::class),
    // sync
    SYNC_REQUEST(12, SyncRequestMessage::class),
    SYNC_RESPONSE(13, SyncResponseMessage::class),
    SYNC_BLOCKS_REQUEST(14, SyncBlockRequestMessage::class),
    MAIN_BLOCK(15, MainBlockMessage::class),
    GENESIS_BLOCK(16, GenesisBlockMessage::class);


    companion object {

        fun get(id: Byte) = values().single { id == it.id }

        fun get(message: Serializable) = values().single { message::class == it.clazz }

    }

}
