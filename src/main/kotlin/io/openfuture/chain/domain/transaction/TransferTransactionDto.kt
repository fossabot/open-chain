package io.openfuture.chain.domain.transaction

import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.entity.transaction.unconfirmed.UTransferTransaction

class TransferTransactionDto(
    data: TransferTransactionData,
    timestamp: Long,
    senderPublicKey: String,
    senderSignature: String,
    hash: String
) : BaseTransactionDto<UTransferTransaction, TransferTransactionData>(data, timestamp, senderPublicKey, senderSignature, hash) {

    constructor(tx: UTransferTransaction) : this(
        TransferTransactionData(tx.amount, tx.fee,tx.recipientAddress, tx.senderAddress),
        tx.timestamp,
        tx.senderPublicKey,
        tx.senderSignature,
        tx.hash
    )

    override fun toEntity(): UTransferTransaction = UTransferTransaction(
        timestamp,
        data.amount,
        data.fee,
        data.recipientAddress,
        data.senderAddress,
        senderPublicKey,
        senderSignature,
        hash
    )

}
