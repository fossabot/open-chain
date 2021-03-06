package io.openfuture.chain.network.component.time

import io.openfuture.chain.core.sync.SyncStatus
import io.openfuture.chain.core.sync.SyncStatus.*
import io.openfuture.chain.network.component.ExplorerAddressesHolder
import io.openfuture.chain.network.message.network.ResponseTimeMessage
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.service.ConnectionService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

@Component
class ClockSynchronizer(
    private val clock: Clock,
    private val properties: NodeProperties,
    private val connectionService: ConnectionService,
    private val addressHolder: ExplorerAddressesHolder
) {

    companion object {
        private val log = LoggerFactory.getLogger(Clock::class.java)
    }

    private val offsets: MutableList<Long> = Collections.synchronizedList(mutableListOf())
    private val selectionSize: Int = properties.getRootAddresses().size
    private val lock: ReadWriteLock = ReentrantReadWriteLock()
    private val syncRound: AtomicInteger = AtomicInteger()
    private val deviation: AtomicLong = AtomicLong()
    private val nodesTime: ConcurrentHashMap<ResponseTimeMessage, Long> = ConcurrentHashMap()

    @Volatile
    private var status: SyncStatus = NOT_SYNCHRONIZED

    @Scheduled(fixedDelayString = "\${node.time-sync-interval}")
    fun sync() {
        lock.writeLock().lock()
        val addresses = addressHolder.getRandomList(selectionSize).asSequence().map { it.address }.toSet()
        try {
            if (SYNCHRONIZED != status) {
                status = PROCESSING
            }
            offsets.clear()
            nodesTime.clear()

            connectionService.sendTimeSyncRequest(addresses)
        } finally {
            lock.writeLock().unlock()

            Thread.sleep(properties.expiry!!)
            mitigate()
        }
    }

    fun add(msg: ResponseTimeMessage, destinationTime: Long) {
        lock.writeLock().lock()
        try {
            if (isExpired(msg, destinationTime)) {
                return
            }

            val offset = getRemoteOffset(msg, destinationTime)

            if (msg.status == SYNCHRONIZED.priority) {
                nodesTime[msg] = offset
            }

            if (0 == syncRound.get()) {
                deviation.set(500)
                if (!isOutOfBound(offset)) {
                    offsets.add(offset)
                }
                return
            }

        } finally {
            lock.writeLock().unlock()
        }
    }

    fun getStatus(): SyncStatus {
        lock.readLock().lock()
        try {
            return status
        } finally {
            lock.readLock().unlock()
        }
    }

    private fun mitigate() {
        lock.writeLock().lock()
        log.debug("CLOCK: Offsets size ${offsets.size} nodesTime size ${nodesTime.size}")
        try {

            val offset = if (syncRound.get() != 0) {
                if ((selectionSize * 2 / 3) > nodesTime.size) {
                    status = NOT_SYNCHRONIZED
                    return
                }
                nodesTime.values.average().toLong()
            } else {
                if ((selectionSize * 2 / 3) > offsets.size) {
                    status = NOT_SYNCHRONIZED
                    return
                }
                getEffectiveOffset()
            }

            clock.adjust(offset)
            syncRound.getAndIncrement()
            log.info("CLOCK: Effective offset $offset")

            if (SYNCHRONIZED != status) {
                status = SYNCHRONIZED
            }

        } finally {
            lock.writeLock().unlock()
        }
    }

    private fun getEffectiveOffset(): Long = Math.round(offsets.average())
    private fun getRemoteOffset(msg: ResponseTimeMessage, destinationTime: Long): Long =
        ((destinationTime - msg.originalTime) + (msg.transmitTime - msg.receiveTime)) / 2

    private fun getScale(): Double = 1.div(Math.log(Math.E.plus(syncRound.get())))

    private fun isExpired(msg: ResponseTimeMessage, destinationTime: Long): Boolean =
        properties.expiry!! < Math.abs(destinationTime.minus(msg.originalTime))

    private fun isOutOfBound(offset: Long): Boolean = deviation.get() < offset

}