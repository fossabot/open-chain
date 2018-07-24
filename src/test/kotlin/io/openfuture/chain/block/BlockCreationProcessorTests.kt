package io.openfuture.chain.block

import io.openfuture.chain.block.validation.BlockValidationProvider
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.crypto.key.NodeKeyHolder
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.block.BlockCreationEvent
import io.openfuture.chain.domain.block.PendingBlock
import io.openfuture.chain.domain.block.Signature
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Delegate
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.property.NodeProperties
import io.openfuture.chain.service.BlockService
import io.openfuture.chain.service.ConsensusService
import io.openfuture.chain.service.DelegateService
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock

class BlockCreationProcessorTests: ServiceTests() {

    @Mock private lateinit var blockService: BlockService
    @Mock private lateinit var signatureCollector: SignatureCollector
    @Mock private lateinit var keyHolder: NodeKeyHolder
    @Mock private lateinit var blockValidationService: BlockValidationProvider
    @Mock private lateinit var consensusService: ConsensusService
    @Mock private lateinit var clock: NodeClock
    @Mock private lateinit var delegateService: DelegateService
    @Mock private lateinit var properties: NodeProperties

    private lateinit var processor: BlockCreationProcessor

    @Before
    fun init() {
        val block = createMainBlock()
        given(blockService.getLastMain()).willReturn(block)
        processor = BlockCreationProcessor(blockService, signatureCollector, keyHolder, blockValidationService,
            consensusService, clock, delegateService, properties)
    }

    @Test(expected = IllegalArgumentException::class)
    fun approveBlockFailsIfBlockIsInvalid() {
        val block = createMainBlock()
        val pendingBlock = createPendingBlock(block)
        given(blockValidationService.isValid(block)).willReturn(false)

        processor.approveBlock(pendingBlock)
    }

    @Test(expected = IllegalArgumentException::class)
    fun approveBlockFailsIfSignatureVerificationFailed() {
        val block = createMainBlock()
        val pendingBlock = createPendingBlock(block)

        given(blockValidationService.isValid(block)).willReturn(true)

        processor.approveBlock(pendingBlock)
    }

    @Test(expected = IllegalArgumentException::class)
    fun approveBlockFailsIfBlockSignatureIsAlreadyExists() {
        val block = createMainBlock()
        val pendingBlock = createPendingBlock(block)

        given(blockValidationService.isValid(block)).willReturn(true)

        processor.approveBlock(pendingBlock)
    }

    @Test
    fun fireBlockCreationShouldCreateMainBlock() {
        val transactions = createTransactions()
        val genesisBlock = createGenesisBlock()

        given(keyHolder.getPublicKey()).willReturn(HashUtils.fromHexString("public_key"))
        given(keyHolder.getPrivateKey()).willReturn(HashUtils.fromHexString("private_key"))

        val delegate = Delegate(HashUtils.toHexString(keyHolder.getPublicKey()), "address", 1)
        genesisBlock.activeDelegates = setOf(delegate)
        val event = BlockCreationEvent(transactions)

        given(blockService.getLastGenesis()).willReturn(genesisBlock)

        processor.fireBlockCreation(event)
    }

    private fun createPendingBlock(block: Block): PendingBlock {
        return PendingBlock(
            block,
            Signature("sign", "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719")
        )
    }

    private fun createMainBlock() = MainBlock(
        ByteArray(1),
        123,
        "prev_block_hash",
        "b7f6eb8b900a585a840bf7b44dea4b47f12e7be66e4c10f2305a0bf67ae91719",
        1512345678L,
        createTransactions()
    )

    private fun createGenesisBlock() = GenesisBlock(
        ByteArray(1),
        123,
        "prev_block_hash",
        1512345678L,
        1,
        setOf(
            Delegate("public_key1", "host1", 1),
            Delegate("public_key2", "host2", 2),
            Delegate("public_key3", "host3", 3)
        )
    )

    private fun createTransactions(): MutableList<BaseTransaction> = mutableListOf(
        VoteTransaction(
            1500000000L,
            1000.0,
            "recipient_address",
            "sender_key",
            "sender_address",
            "sender_signature",
            "hash",
            1,
            "delegate_key"
        ),
        VoteTransaction(
            1500000001L,
            1002.0,
            "recipient_address2",
            "sender_key2",
            "sender_address2",
            "sender_signature2",
            "hash2",
            2,
            "delegate_key2"
        )
    )
}