package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.core.sync.SyncManager
import io.openfuture.chain.core.sync.SyncStatus.SYNCHRONIZED
import io.openfuture.chain.network.component.time.ClockSynchronizer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Component
class BlockProductionScheduler(
    private val keyHolder: NodeKeyHolder,
    private val epochService: EpochService,
    private val mainBlockService: MainBlockService,
    private val genesisBlockService: GenesisBlockService,
    private val pendingBlockHandler: PendingBlockHandler,
    private val consensusProperties: ConsensusProperties,
    private val syncManager: SyncManager,
    private val clockSynchronizer: ClockSynchronizer
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(BlockProductionScheduler::class.java)
    }

    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()


    @PostConstruct
    fun init() {
        executor.scheduleAtFixedRate({ proceedProductionLoop() }, epochService.timeToNextTimeSlot(),
            consensusProperties.getPeriod(), TimeUnit.MILLISECONDS)
    }

    private fun proceedProductionLoop() {
        try {
            if (SYNCHRONIZED != clockSynchronizer.getStatus() || SYNCHRONIZED != syncManager.getStatus()) {
                log.debug("----------------Clock is ${clockSynchronizer.getStatus()}----------------")
                log.debug("----------------Ledger is ${syncManager.getStatus()}----------------")
                return
            }

            val slotOwner = epochService.getCurrentSlotOwner()
            log.debug("CONSENSUS: Slot owner ${slotOwner.id}")
            if (genesisBlockService.isGenesisBlockRequired()) {
                val genesisBlock = genesisBlockService.create()
                genesisBlockService.add(genesisBlock)
                pendingBlockHandler.resetSlotNumber()
            } else if (keyHolder.getPublicKeyAsHexString() == slotOwner.publicKey) {
                val block = mainBlockService.create()
                pendingBlockHandler.addBlock(block)
            }
        } catch (ex: Exception) {
            log.error("Block creation failure inbound: ${ex.message}")
        }
    }

}