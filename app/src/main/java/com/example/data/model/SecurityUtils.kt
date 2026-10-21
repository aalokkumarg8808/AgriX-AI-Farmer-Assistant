package com.example.data.model

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Local password hashing and verification utilities.
 *
 * Security Note:
 * - New password hashes use PBKDF2WithHmacSHA256 (`v2$<iterations>$<base64Hash>`) with a 16-byte
 *   cryptographically random salt per account and constant-time comparison (`MessageDigest.isEqual`).
 * - Legacy v1 SHA-256 hashes (created in earlier versions without the `v2$` prefix) are verified
 *   in constant time and automatically upgraded to v2 PBKDF2 hashes upon successful login.
 * - Because AgriX AI uses a device-local SQLite/Room database, these credentials authenticate the
 *   user only on this local device and do not provide cloud identity or cross-device sync.
 */
object SecurityUtils {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val V2_PREFIX = "v2"
    const val DEFAULT_ITERATIONS = 65_536
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16

    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_BYTES)
        random.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    /**
     * Hashes a password using PBKDF2-HMAC-SHA256 and returns a versioned hash string:
     * `v2$<iterations>$<base64DerivedKey>`.
     */
    fun hashPassword(
        password: String,
        salt: String,
        iterations: Int = DEFAULT_ITERATIONS
    ): String {
        val saltBytes = decodeBase64Safe(salt)
        val derived = derivePbkdf2(password, saltBytes, iterations)
        val encodedHash = Base64.encodeToString(derived, Base64.NO_WRAP)
        return "$V2_PREFIX$$iterations$$encodedHash"
    }

    /**
     * Computes a legacy v1 single-pass SHA-256 hash solely for verifying and migrating
     * pre-existing local accounts.
     */
    internal fun hashPasswordLegacyV1(password: String, salt: String): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(decodeBase64Safe(salt))
        return md.digest(password.toByteArray(Charsets.UTF_8))
    }

    /**
     * Returns true if [storedHash] uses the legacy v1 SHA-256 format and should be upgraded
     * to v2 PBKDF2 upon successful login.
     */
    fun isLegacyHash(storedHash: String): Boolean {
        return !storedHash.startsWith("$V2_PREFIX$")
    }

    /**
     * Verifies [password] against [expectedHash] using constant-time byte comparison.
     * Supports both v2 PBKDF2 hashes (`v2$<iterations>$<base64>`) and legacy v1 SHA-256 hashes.
     */
    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        if (password.isEmpty() || salt.isBlank() || expectedHash.isBlank()) {
            return false
        }
        return try {
            if (expectedHash.startsWith("$V2_PREFIX$")) {
                val parts = expectedHash.split("$")
                if (parts.size != 3) return false
                val iterations = parts[1].toIntOrNull() ?: return false
                if (iterations < 1_000 || iterations > 5_000_000) return false
                val expectedBytes = Base64.decode(parts[2], Base64.NO_WRAP)
                if (expectedBytes.isEmpty()) return false
                val saltBytes = decodeBase64Safe(salt)
                val computedBytes = derivePbkdf2(password, saltBytes, iterations)
                MessageDigest.isEqual(computedBytes, expectedBytes)
            } else {
                val expectedBytes = Base64.decode(expectedHash, Base64.NO_WRAP)
                if (expectedBytes.isEmpty()) return false
                val computedBytes = hashPasswordLegacyV1(password, salt)
                MessageDigest.isEqual(computedBytes, expectedBytes)
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun derivePbkdf2(password: String, saltBytes: ByteArray, iterations: Int): ByteArray {
        val chars = password.toCharArray()
        val spec = PBEKeySpec(chars, saltBytes, iterations, KEY_LENGTH_BITS)
        return try {
            val skf = SecretKeyFactory.getInstance(ALGORITHM)
            skf.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
            chars.fill('\u0000')
        }
    }

    private fun decodeBase64Safe(input: String): ByteArray {
        return try {
            val decoded = Base64.decode(input, Base64.NO_WRAP)
            if (decoded.isNotEmpty()) decoded else input.toByteArray(Charsets.UTF_8)
        } catch (e: Exception) {
            input.toByteArray(Charsets.UTF_8)
        }
    }
}
