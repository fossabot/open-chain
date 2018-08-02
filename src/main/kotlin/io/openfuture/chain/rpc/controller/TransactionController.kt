package io.openfuture.chain.rpc.controller

import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.core.service.TransactionService
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.core.service.VoteTransactionService
import io.openfuture.chain.rpc.domain.transaction.DelegateTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.TransferTransactionRequest
import io.openfuture.chain.rpc.domain.transaction.VoteTransactionRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/rpc/transactions")
class TransactionController(
    private val transactionService: TransactionService
) {

    @PostMapping("/votes")
    fun addVote(@Valid @RequestBody request: VoteTransactionRequest) {
        transactionService.add(request)
    }

    @PostMapping("/transfers")
    fun addTransfer(@Valid @RequestBody request: TransferTransactionRequest) {
        transactionService.add(request)
    }

    @PostMapping("/delegates")
    fun addDelegates(@Valid @RequestBody request: DelegateTransactionRequest) {
        transactionService.add(request)
    }

}
