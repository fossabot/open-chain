package io.openfuture.chain.core.service.block

import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.repository.BlockRepository
import io.openfuture.chain.core.service.BlockService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultBlockService(
    private val repository: BlockRepository<Block>
) : BlockService {

    @Transactional(readOnly = true)
    override fun getAfterCurrentHash(hash: String): List<Block> {
        val startBlock = repository.findOneByHash(hash) ?: return emptyList()
        return repository.findAllByHeightGreaterThan(startBlock.height)
    }

    override fun getAvgProductionTime(): Long {
        val lastBlock = getLast()
        val firstMainBlock = repository.findFirstByHeightGreaterThan(1) ?: return 0
        return (lastBlock.timestamp - firstMainBlock.timestamp) / lastBlock.height
    }

    @Transactional(readOnly = true)
    override fun getCount(): Long = repository.count()

    @Transactional(readOnly = true)
    override fun getLast(): Block =
        repository.findFirstByOrderByHeightDesc() ?: throw NotFoundException("Last block not found!")

    @Transactional(readOnly = true)
    override fun isExists(hash: String): Boolean = repository.findOneByHash(hash)?.let { true } ?: false

    @Transactional(readOnly = true)
    override fun isExists(hash: String, height: Long): Boolean =
        repository.findOneByHashAndHeight(hash, height)?.let { true } ?: false

    @Transactional(readOnly = true)
    override fun getCurrentHeight(): Long = repository.getCurrentHeight()

    @Transactional(readOnly = true)
    override fun getCarcassForBlockSync(hash: String): List<Block> {
        val startBlock = repository.findOneByHash(hash) ?: return emptyList()
        return repository.findTop30ByHeightGreaterThanOrderByHeightDesc(startBlock.height)
    }

}