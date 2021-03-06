package io.openfuture.chain.consensus.repository

import io.openfuture.chain.config.RepositoryTests
import io.openfuture.chain.core.model.entity.Wallet
import io.openfuture.chain.core.repository.WalletRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class WalletRepositoryTests : RepositoryTests() {

    @Autowired
    private lateinit var repository: WalletRepository


    @Test
    fun findOneByAddress() {
        val address = "address"
        val wallet = Wallet(address, 1)

        entityManager.persist(wallet)

        val actualWallet = repository.findOneByAddress(address)

        assertThat(actualWallet).isEqualTo(wallet)
    }

}
