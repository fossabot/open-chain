package io.openfuture.chain.core.model.entity.transaction.payload

import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class RewardTransactionPayload(

    @Column(name = "reward", nullable = false)
    var reward: Long,

    @Column(name = "recipient_address", nullable = false)
    var recipientAddress: String

) : TransactionPayload {

    override fun getBytes(): ByteArray {
        val buffer = ByteBuffer.allocate(LONG_BYTES + recipientAddress.toByteArray(UTF_8).size)
        buffer.putLong(reward)
        buffer.put(recipientAddress.toByteArray(UTF_8))
        return buffer.array()
    }

}