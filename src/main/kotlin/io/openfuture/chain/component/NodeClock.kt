package io.openfuture.chain.component

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

@Component
class NodeClock {

    companion object {
        private val log = LoggerFactory.getLogger(NodeClock::class.java)

        private const val MINIMUM_OFFSETS_SIZE_TO_DO_SYNC = 5
    }

    @Volatile
    private var adjustment : Long = 0

    private val networkTimeOffsets: ConcurrentHashMap<String, Long> = ConcurrentHashMap()

    private val lock: ReadWriteLock = ReentrantReadWriteLock()

    fun nodeTime() : Long = System.currentTimeMillis()

    fun networkTime() : Long {
        lock.readLock().lock()
        try{
            return nodeTime() + adjustment
        } finally {
            lock.readLock().unlock()
        }
    }

    fun addTimeOffset(remoteAddress: String, offset: Long) {
        lock.writeLock().lock()
        try{
            networkTimeOffsets[remoteAddress] = offset
            recalculateAdjustment()
        } finally {
            lock.writeLock().unlock()
        }
    }

    fun removeTimeOffset(remoteAddress: String) {
        lock.writeLock().lock()
        try{
            networkTimeOffsets.remove(remoteAddress)
            recalculateAdjustment()
        } finally {
            lock.writeLock().unlock()
        }
    }

    private fun recalculateAdjustment() {
        if (networkTimeOffsets.size % 2 == 1 &&
                networkTimeOffsets.size >= MINIMUM_OFFSETS_SIZE_TO_DO_SYNC) {
            val offsetList = ArrayList(networkTimeOffsets.values)
            offsetList.sort()
            adjustment = offsetList[networkTimeOffsets.size / 2]
            log.info("Time adjustment was changed: $adjustment")
        }
    }
}