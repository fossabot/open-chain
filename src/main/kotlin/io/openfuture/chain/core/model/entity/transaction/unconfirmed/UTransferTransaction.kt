package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.dto.transaction.BaseTransactionDto
import io.openfuture.chain.core.model.dto.transaction.TransferTransactionDto
import io.openfuture.chain.core.model.dto.transaction.VoteTransactionDto
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.BaseTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.VoteTransactionPayload
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "u_transfer_transactions")
class UTransferTransaction(
    timestamp: Long,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,

    @Embedded
    private var payload: TransferTransactionPayload

) : UTransaction(timestamp, senderAddress, senderPublicKey, senderSignature, hash) {

    companion object {
        fun of(dto: TransferTransactionDto): UTransferTransaction = UTransferTransaction(
            dto.timestamp,
            dto.senderAddress,
            dto.senderPublicKey,
            dto.senderSignature,
            dto.hash,
            TransferTransactionPayload(dto.fee, dto.amount, dto.recipientAddress)
        )
    }

    override fun toMessage(): TransferTransactionDto = TransferTransactionDto(
        timestamp,
        payload.fee,
        senderAddress,
        senderPublicKey,
        senderSignature,
        hash,
        payload.amount,
        payload.recipientAddress
    )

    override fun toConfirmed(): TransferTransaction = TransferTransaction(
        timestamp,
        senderAddress,
        senderPublicKey,
        senderSignature,
        hash,
        payload
    )

    override fun getPayload(): TransferTransactionPayload {
        return payload
    }

}