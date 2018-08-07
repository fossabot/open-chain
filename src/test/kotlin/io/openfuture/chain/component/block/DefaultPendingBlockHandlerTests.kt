package io.openfuture.chain.component.block

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.config.any
import io.openfuture.chain.consensus.component.block.BlockApprovalStage
import io.openfuture.chain.consensus.component.block.DefaultPendingBlockHandler
import io.openfuture.chain.consensus.model.entity.transaction.TransferTransaction
import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.Transaction
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.core.util.TransactionUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.domain.NetworkBlockApproval
import io.openfuture.chain.network.domain.NetworkMainBlock
import io.openfuture.chain.network.service.NetworkService
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify


class DefaultPendingBlockHandlerTests : ServiceTests() {

    @Mock private lateinit var epochService: EpochService
    @Mock private lateinit var mainBlockService: MainBlockService
    @Mock private lateinit var keyHolder: NodeKeyHolder
    @Mock private lateinit var networkService: NetworkService

    private lateinit var defaultPendingBlockHandler: DefaultPendingBlockHandler


    @Before
    fun setUp() {
        defaultPendingBlockHandler = DefaultPendingBlockHandler(
            epochService,
            mainBlockService,
            keyHolder,
            networkService
        )
    }

    @Test
    fun addBlockShouldAddMainBlockAndBroadcast() {
        val delegate = Delegate("publicKey", "address", 1)
        val transactions: MutableSet<Transaction> = mutableSetOf(
            TransferTransaction(
                1L,
                2L,
                3L,
                "recipientAddress",
                "senderAddress",
                "senderPublicKey",
                "senderSignature",
                "hash",
                null
            )
        )
        val block = MainBlock(
            1L,
            "previousHash",
            2L,
            3L,
            "publicKey",
            TransactionUtils.calculateMerkleRoot(transactions),
            transactions
        )
        val privateKey = "529719453390370201f3f0efeeffe4c3a288f39b2e140a3f6074c8d3fc0021e6"
        block.sign(ByteUtils.fromHexString(privateKey))

        given(keyHolder.getPrivateKey()).willReturn(
            ByteUtils.fromHexString(privateKey))
        given(keyHolder.getPublicKey()).willReturn("037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4")
        given(epochService.getSlotNumber(block.timestamp)).willReturn(2L)
        given(epochService.getCurrentSlotOwner()).willReturn(delegate)
        given(epochService.getDelegates()).willReturn(
            setOf(Delegate("037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4", "address", 1)))
        given(mainBlockService.isValid(block)).willReturn(true)

        defaultPendingBlockHandler.addBlock(block)

        verify(networkService, times(1)).broadcast(any(NetworkMainBlock::class.java))
        verify(networkService, times(1)).broadcast(any(NetworkBlockApproval::class.java))
    }

    @Test
    fun handleApproveMessageShouldPrepareApproveMessage() {
        val privateKey = "237a1f68f5e6ee331c2d1fac4107807f7eaf7eed2265490d9a9a330c3549a43d"
        val publicKey = "020bf4f11983fca4a99b0d7b18fbffa02462c36126757e598e9beaa33a275f0948"
        val delegate = Delegate(publicKey, "address", 1)
        val transactions: MutableSet<Transaction> = mutableSetOf(
            TransferTransaction(
                1L,
                2L,
                3L,
                "recipientAddress",
                "senderAddress",
                "senderPublicKey",
                "senderSignature",
                "hash",
                null
            )
        )
        val mainBlock = MainBlock(
            1L,
            "previousHash",
            2L,
            3L,
            publicKey,
            TransactionUtils.calculateMerkleRoot(transactions),
            transactions
        )
        val message = NetworkBlockApproval(
            BlockApprovalStage.PREPARE.value,
            "22c626c74fdc7aa6b2809d88a60068e6017a3d7015113ebd0af18cdf9f3809c6",
            publicKey,
            null
        )

        message.signature = SignatureUtils.sign(message.getBytes(), ByteUtils.fromHexString(privateKey))
        mainBlock.sign(ByteUtils.fromHexString(privateKey))

        given(keyHolder.getPrivateKey()).willReturn(
            ByteUtils.fromHexString(privateKey))
        given(keyHolder.getPublicKey()).willReturn(publicKey)
        given(epochService.getSlotNumber(mainBlock.timestamp)).willReturn(2L)
        given(epochService.getCurrentSlotOwner()).willReturn(delegate)
        given(mainBlockService.isValid(mainBlock)).willReturn(true)
        given(epochService.getDelegates()).willReturn(
            setOf(Delegate("020bf4f11983fca4a99b0d7b18fbffa02462c36126757e598e9beaa33a275f0948", "address", 1)))
        defaultPendingBlockHandler.addBlock(mainBlock)

        given(epochService.getDelegates()).willReturn(setOf(delegate))

        defaultPendingBlockHandler.handleApproveMessage(message)

        verify(networkService, times(3)).broadcast(any(NetworkBlockApproval::class.java))
    }

    @Test
    fun handleApproveMessageShouldCommitApproveMessage() {
        val publicKey = "037aa4d9495e30b6b30b94a30f5a573a0f2b365c25eda2d425093b6cf7b826fbd4"
        val delegate = Delegate(publicKey, "address", 1)
        val message = NetworkBlockApproval(
            BlockApprovalStage.COMMIT.value,
            "2a897fecaaaddcd924a9f562be1cdacf0c7cf3370d1d13c3209f0d05be6bd26f",
            publicKey,
            "MEYCIQCjcs54dZxldCIqIwpwKxsUAYIeMGNdlaBudCF7Ps7SYwIhAJiKsUUoOuTiVHZNePFDjPWF5sarUFguNqV+lMDnsieX"
        )

        given(epochService.getDelegates()).willReturn(setOf(delegate))

        defaultPendingBlockHandler.handleApproveMessage(message)
    }

    @Test
    fun handleApproveMessageShouldCommitApproveTwoMessages() {
        addBlockShouldAddMainBlockAndBroadcast()
        val blockHash = "2b9fa527078f6c5d8b48cbee453e138f6a3a54f9ef1da57b7b464bb17a4d7a72"
        val privateKey = "237a1f68f5e6ee331c2d1fac4107807f7eaf7eed2265490d9a9a330c3549a43d"
        val privateKey2 = "a3ddaee50c7986bb95816b0d389eadbff39146af546fa795a6433c67af97c6c9"
        val publicKey = "020bf4f11983fca4a99b0d7b18fbffa02462c36126757e598e9beaa33a275f0948"
        val publicKey2 = "02fb3085f5f8bd7a095198211fb1d4781fd7f0643dec52328151b6cb46e46931fd"
        val delegate = Delegate(publicKey, "address", 1)
        val delegate2 = Delegate(publicKey2, "address2", 2)
        val message = NetworkBlockApproval(
            BlockApprovalStage.COMMIT.value,
            blockHash,
            publicKey,
            null
        )
        message.signature = SignatureUtils.sign(message.getBytes(), ByteUtils.fromHexString(privateKey))
        val message2 = NetworkBlockApproval(
            BlockApprovalStage.COMMIT.value,
            blockHash,
            publicKey2,
            null
        )
        message2.signature = SignatureUtils.sign(message2.getBytes(), ByteUtils.fromHexString(privateKey2))

        given(epochService.getDelegates()).willReturn(setOf(delegate, delegate2))

        defaultPendingBlockHandler.handleApproveMessage(message)
        defaultPendingBlockHandler.handleApproveMessage(message2)

        verify(networkService, times(2)).broadcast(any(NetworkBlockApproval::class.java))
        verify(mainBlockService, times(1)).save(any(MainBlock::class.java))
    }

}