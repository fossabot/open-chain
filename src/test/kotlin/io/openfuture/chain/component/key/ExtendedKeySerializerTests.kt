package io.openfuture.chain.component.key

import io.openfuture.chain.config.ServiceTests
import io.openfuture.chain.domain.key.ECKey
import io.openfuture.chain.domain.key.ExtendedKey
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExtendedKeySerializerTests: ServiceTests() {

    private val serializer = ExtendedKeySerializer()


    @Test
    fun serializePublicTest() {
        val ecKey = ECKey("""xpub68GHhbqGdkJSJ4Ly8C1furjPtHAdDUzAzaVQzVJavjLt2DvvKYq
            t9UdcNrmk6JKU8h1rK2nWAV6yqPV6Hpvuf33dezACzKmFEbK3fWN4Za6""".toByteArray())
        val extendedKey = ExtendedKey(ecKey, "d354d8b75cdc7b03d3af916cf3e18f00aaca455526322bf1e0352d12de7e7155".toByteArray())

        val serializedExtendedKey = serializer.serializePublic(extendedKey)

        assertThat(serializedExtendedKey).isNotNull()
    }

    @Test
    fun serializePrivateTest() {
        val ecKey = ECKey(("""xpub68GHhbqGdzevrk1tp6iue1kXNHKqWnqevMYjBH9d3kmqfDgPyLVsMy
            |SVsbDZfF9Uq8wmb5uzBW2wAbTpTEjLWbCnvWeyMFkaMNqe9Z8j43v""".trimMargin().toByteArray()))
        val extendedKey = ExtendedKey(ecKey, "d354d8b75cdc7b03d3af916cf3e18f00aaca455526322bf1e0352d12de7e7155".toByteArray())

        val serializedExtendedKey = serializer.serializePrivate(extendedKey)

        assertThat(serializedExtendedKey).isNotNull()
    }

}