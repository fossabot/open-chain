package io.openfuture.chain.core.aspect

import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.exception.SynchronizationException
import io.openfuture.chain.core.sync.SyncManager
import io.openfuture.chain.core.sync.SyncStatus.SYNCHRONIZED
import io.openfuture.chain.network.component.time.ClockSynchronizer
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.stereotype.Component


@Aspect
@Component
class NodeSyncAspect(
    private val syncManager: SyncManager,
    private val clockSynchronizer: ClockSynchronizer
) {

    @Before("@annotation(annotation)")
    fun annotated(annotation: BlockchainSynchronized) {
        if (SYNCHRONIZED != syncManager.getStatus() || SYNCHRONIZED != clockSynchronizer.getStatus()) {
            throw SynchronizationException("Application is not synchronized!")
        }
    }

}