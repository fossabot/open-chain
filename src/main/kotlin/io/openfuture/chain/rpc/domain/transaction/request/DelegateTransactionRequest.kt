package io.openfuture.chain.rpc.domain.transaction.request

import io.openfuture.chain.crypto.annotation.AddressChecksum
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class DelegateTransactionRequest(
    @field:NotNull var timestamp: Long? = null,
    @field:NotNull var fee: Long? = null,
    @field:NotBlank @field:AddressChecksum var senderAddress: String? = null,
    @field:NotNull var amount: Long? = null,
    @field:NotBlank var nodeId: String? = null,
    @field:NotBlank var nodeKey: String? = null,
    @field:NotBlank var nodeHost: String? = null,
    @field:NotNull var nodePort: Int? = null,
    @field:NotBlank var hash: String? = null,
    @field:NotBlank var senderSignature: String? = null,
    @field:NotBlank var senderPublicKey: String? = null
)