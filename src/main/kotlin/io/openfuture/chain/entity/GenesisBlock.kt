package io.openfuture.chain.entity

import io.openfuture.chain.crypto.util.HashUtils
import org.apache.commons.lang3.StringUtils
import java.security.PublicKey
import javax.persistence.*

@Entity
@Table(name = "genesis_blocks")
class GenesisBlock(privateKey: ByteArray, height: Long,
        previousHash: String, timestamp: Long, publicKey: ByteArray,

    @Column(name = "epoch_index", nullable = false)
    var epochIndex: Long,

    @ManyToMany
    @JoinTable(name = "delegate2genesis",
        joinColumns = [JoinColumn(name = "genesis_id")],
        inverseJoinColumns = [(JoinColumn(name = "delegate_id"))])
    var activeDelegates: Set<Delegate>

) : Block(privateKey, height, previousHash, StringUtils.EMPTY, timestamp, BlockType.GENESIS.id,
    HashUtils.toHexString(publicKey))