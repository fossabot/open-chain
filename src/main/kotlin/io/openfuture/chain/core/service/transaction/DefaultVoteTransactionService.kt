package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.component.TransactionCapacityChecker
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.exception.model.ExceptionType.INCORRECT_DELEGATE_KEY
import io.openfuture.chain.core.exception.model.ExceptionType.INCORRECT_VOTES_COUNT
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.dictionary.VoteType
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedVoteTransaction
import io.openfuture.chain.core.repository.UVoteTransactionRepository
import io.openfuture.chain.core.repository.VoteTransactionRepository
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.VoteTransactionService
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import io.openfuture.chain.rpc.domain.transaction.request.VoteTransactionRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class DefaultVoteTransactionService(
    repository: VoteTransactionRepository,
    uRepository: UVoteTransactionRepository,
    capacityChecker: TransactionCapacityChecker,
    private val delegateService: DelegateService,
    private val consensusProperties: ConsensusProperties
) : ExternalTransactionService<VoteTransaction, UnconfirmedVoteTransaction>(repository, uRepository, capacityChecker), VoteTransactionService {

    companion object {
        val log = LoggerFactory.getLogger(DefaultVoteTransactionService::class.java)
    }


    @Transactional(readOnly = true)
    override fun getByHash(hash: String): VoteTransaction = repository.findOneByFooterHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional(readOnly = true)
    override fun getAllUnconfirmed(): MutableList<UnconfirmedVoteTransaction> = unconfirmedRepository.findAllByOrderByHeaderFeeDesc()

    @Transactional(readOnly = true)
    override fun getUnconfirmedByHash(hash: String): UnconfirmedVoteTransaction = unconfirmedRepository.findOneByFooterHash(hash)
        ?: throw NotFoundException("Transaction with hash $hash not found")

    @Transactional
    override fun add(message: VoteTransactionMessage): UnconfirmedVoteTransaction {
        return super.add(UnconfirmedVoteTransaction.of(message))
    }

    @BlockchainSynchronized
    @Transactional
    override fun add(request: VoteTransactionRequest): UnconfirmedVoteTransaction {
        return super.add(UnconfirmedVoteTransaction.of(request))
    }

    @Transactional
    override fun toBlock(message: VoteTransactionMessage, block: MainBlock): VoteTransaction {
        val tx = repository.findOneByFooterHash(message.hash)
        if (null != tx) {
            return tx
        }

        walletService.decreaseBalance(message.senderAddress, message.fee)

        val utx = unconfirmedRepository.findOneByFooterHash(message.hash)

        if (null != utx) {
            return confirm(utx, VoteTransaction.of(utx, block))
        }

        return this.save(VoteTransaction.of(message, block))
    }

    @Transactional
    override fun verify(message: VoteTransactionMessage): Boolean {
        return try {
            validate(UnconfirmedVoteTransaction.of(message))
            true
        } catch (e: ValidationException) {
            log.warn(e.message)
            false
        }
    }

    @Transactional
    override fun save(tx: VoteTransaction): VoteTransaction {
        val type = tx.payload.getVoteType()
        updateWalletVotes(tx.header.senderAddress, tx.payload.nodeId, type)
        return super.save(tx)
    }

    @Transactional
    override fun validate(utx: UnconfirmedVoteTransaction) {
        if (!isExistsDelegate(utx.payload.nodeId)) {
            throw ValidationException("Incorrect delegate key", INCORRECT_DELEGATE_KEY)
        }

        if (!isValidVoteCount(utx.header.senderAddress)) {
            throw ValidationException("Incorrect votes count", INCORRECT_VOTES_COUNT)
        }

        if (isAlreadyVote(utx.header.senderAddress, utx.payload.nodeId)) {
            throw ValidationException("Address: ${utx.header.senderAddress} already vote for delegate with key: ${utx.payload.nodeId}")
        }

        if (!isExistsVoteType(utx.payload.voteTypeId)) {
            throw ValidationException("Vote type with id: ${utx.payload.voteTypeId} is not exists")
        }

        super.validateExternal(utx.header, utx.payload, utx.footer, utx.header.fee)
    }

    private fun updateWalletVotes(senderAddress: String, nodeId: String, type: VoteType) {
        val delegate = delegateService.getByNodeId(nodeId)
        val wallet = walletService.getByAddress(senderAddress)

        when (type) {
            VoteType.FOR -> {
                wallet.votes.add(delegate)
            }
            VoteType.AGAINST -> {
                wallet.votes.remove(delegate)
            }
        }
        walletService.save(wallet)
    }

    private fun isExistsDelegate(nodeId: String): Boolean = delegateService.isExistsByNodeId(nodeId)

    private fun isValidVoteCount(senderAddress: String): Boolean {
        val confirmedVotes = walletService.getVotesByAddress(senderAddress).count()
        val unconfirmedForVotes = unconfirmedRepository.findAll()
            .filter { it.header.senderAddress == senderAddress && it.payload.getVoteType() == VoteType.FOR }
            .count()

        return consensusProperties.delegatesCount!! > confirmedVotes + unconfirmedForVotes
    }

    private fun isAlreadyVote(senderAddress: String, nodeId: String): Boolean {
        val delegates = walletService.getVotesByAddress(senderAddress)
        return delegates.any { it.nodeId == nodeId }
    }

    private fun isExistsVoteType(typeId: Int): Boolean {
        return VoteType.values().any { it.getId() == typeId }
    }

}