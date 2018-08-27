package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.rpc.domain.transaction.request.DelegateTransactionRequest
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "u_delegate_transactions")
class UnconfirmedDelegateTransaction(
    header: TransactionHeader,
    hash: String,
    senderSignature: String,
    senderPublicKey: String,

    @Embedded
    var payload: DelegateTransactionPayload

) : UnconfirmedTransaction(header, hash, senderSignature, senderPublicKey) {

    companion object {
        fun of(message: DelegateTransactionMessage): UnconfirmedDelegateTransaction = UnconfirmedDelegateTransaction(
            TransactionHeader(message.timestamp, message.fee, message.senderAddress),
            message.hash,
            message.senderSignature,
            message.senderPublicKey,
            DelegateTransactionPayload(message.delegateKey, message.delegateHost, message.delegatePort, message.amount)
        )

        fun of(request: DelegateTransactionRequest): UnconfirmedDelegateTransaction = UnconfirmedDelegateTransaction(
            TransactionHeader(request.timestamp!!, request.fee!!, request.senderAddress!!),
            request.hash!!,
            request.senderSignature!!,
            request.senderPublicKey!!,
            DelegateTransactionPayload(request.delegateKey!!, "localhost", 8080, request.amount!!)
        )
    }

    override fun getPayload(): TransactionPayload = payload

    override fun toMessage(): DelegateTransactionMessage = DelegateTransactionMessage (
        header.timestamp,
        header.fee,
        header.senderAddress,
        hash,
        senderSignature,
        senderPublicKey,
        payload.delegateKey,
        payload.delegateHost,
        payload.delegatePort,
        payload.amount
    )

}