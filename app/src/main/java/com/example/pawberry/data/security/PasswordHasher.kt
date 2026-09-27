package com.example.pawberry.data.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    private const val ITERATIONS = 12_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16
    private const val ALGORITHM = "PBKDF2WithHmacSHA1"

    fun hash(password: String): String {
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        val hash = pbkdf2(password, salt)
        return encode(salt) + ":" + encode(hash)
    }

    fun verify(password: String, stored: String): Boolean {
        val parts = stored.split(":")
        if (parts.size != 2) return false
        val salt = decode(parts[0]) ?: return false
        val expected = decode(parts[1]) ?: return false
        val actual = pbkdf2(password, salt)
        if (actual.size != expected.size) return false
        var diff = 0
        for (i in actual.indices) {
            diff = diff or (actual[i].toInt() xor expected[i].toInt())
        }
        return diff == 0
    }

    private fun pbkdf2(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(spec).encoded
    }

    private fun encode(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.NO_WRAP)

    private fun decode(value: String): ByteArray? = try {
        Base64.decode(value, Base64.NO_WRAP)
    } catch (_: IllegalArgumentException) {
        null
    }
}
