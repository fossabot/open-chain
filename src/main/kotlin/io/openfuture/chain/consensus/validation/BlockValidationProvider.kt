package io.openfuture.chain.consensus.validation

import io.openfuture.chain.consensus.component.block.TimeSlot
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.BaseBlock
import io.openfuture.chain.core.service.CommonBlockService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.component.node.NodeClock
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Component

@Component
class BlockValidationProvider(
    private val commonBlockService: CommonBlockService,
    private val mainBlockService: MainBlockService,
    private val genesisBlockService: GenesisBlockService,
    private val timeSlot: TimeSlot,
    private val clock: NodeClock
) {

    fun isValid(block: BaseBlock): Boolean {
        val currentTime = clock.networkTime()

        val blockIsValid: Boolean = when (block) {
            is MainBlock -> mainBlockService.isValid(block)
            is GenesisBlock -> genesisBlockService.isValid(block)
            else -> throw IllegalArgumentException("Unknown block type")
        }

        val lastBlock = commonBlockService.getLast()
        return blockIsValid
                && timeSlot.verifyTimeSlot(currentTime, block)
                && verifyBlockSignature(block)
                && verifyPreviousHash(block, lastBlock)
                && verifyHeight(block, lastBlock)
                && verifyTimestamp(block, lastBlock)
    }

    private fun verifyBlockSignature(block: BaseBlock): Boolean {
        if (block is MainBlock) {
            return SignatureUtils.verify(
                (block.previousHash + block.merkleHash + block.timestamp + block.height).toByteArray(),
                block.signature!!,
                ByteUtils.fromHexString(block.publicKey))
        }
        return SignatureUtils.verify(
            (block.previousHash + block.timestamp + block.height).toByteArray(),
            block.signature!!,
            ByteUtils.fromHexString(block.publicKey))
    }

    private fun verifyPreviousHash(block: BaseBlock, lastBlock: BaseBlock): Boolean = (block.previousHash == lastBlock.hash)

    private fun verifyTimestamp(block: BaseBlock, lastBlock: BaseBlock): Boolean = (block.timestamp > lastBlock.timestamp)

    private fun verifyHeight(block: BaseBlock, lastBlock: BaseBlock): Boolean = (block.height == lastBlock.height + 1)

}