package io.openfuture.chain.component

import io.openfuture.chain.domain.key.ECKey
import io.openfuture.chain.util.Base58
import io.openfuture.chain.util.HashUtils
import org.springframework.stereotype.Component
import java.util.*

/**
 * Component for exporting/importing private keys in WIF (Wallet Import Format) - a Base58 String representation
 * of private key
 */
@Component
class PrivateKeyManager {

    fun exportPrivateKey(key: ECKey): String = Base58.encode(getWIFBytes(key))

    fun importPrivateKey(serializedKey: String): ECKey = parseWIFBytes(Base58.decode(serializedKey))

    private fun getWIFBytes(key: ECKey): ByteArray {
        return key.private?.let {
            val keyBytes = key.getPrivate()

            val extendedKey = ByteArray(keyBytes.size + 2)
            extendedKey[0] = 0x80.toByte()
            System.arraycopy(keyBytes, 0, extendedKey, 1, keyBytes.size)
            extendedKey[keyBytes.size + 1] = 0x01

            val checkSum = HashUtils.genarateDoubleHashBytes(extendedKey)
            val result = ByteArray(extendedKey.size + 4)
            System.arraycopy(extendedKey, 0, result, 0, extendedKey.size)
            System.arraycopy(checkSum, 0, result, extendedKey.size, 4)
            result
        } ?: throw IllegalStateException("Unable to provide WIF if no private key is present")
    }

    private fun parseWIFBytes(keyBytes: ByteArray): ECKey {
        checkChecksum(keyBytes)
        if (keyBytes.size == 38) {
            val key = Arrays.copyOfRange(keyBytes, 1, keyBytes.size - 5)
            return ECKey(key, true)
        }
        throw IllegalArgumentException("Invalid key length")
    }

    private fun checkChecksum(bytes: ByteArray) {
        val keyBytes = Arrays.copyOfRange(bytes, 0, bytes.size - 4)
        val checksum = Arrays.copyOfRange(bytes, bytes.size - 4, bytes.size)
        val actualChecksum = Arrays.copyOfRange(HashUtils.genarateDoubleHashBytes(keyBytes), 0, 4)
        if (!Arrays.equals(checksum, actualChecksum))
            throw IllegalArgumentException("Invalid checksum")
    }

}