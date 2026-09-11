package com.linhavital.backend.service

import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

@Service
class PasswordService {
    companion object {
        private const val PREFIX = "pbkdf2_sha256"
        private const val ITERATIONS = 600_000
        private const val KEY_LENGTH = 256
        private const val SALT_BYTES = 16
    }

    private val random = SecureRandom()

    fun hash(rawPassword: String): String {
        require(rawPassword.isNotBlank()) { "Senha não pode ser vazia" }
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val hash = derive(rawPassword, salt, ITERATIONS)
        return listOf(
            PREFIX,
            ITERATIONS.toString(),
            Base64.getEncoder().encodeToString(salt),
            Base64.getEncoder().encodeToString(hash)
        ).joinToString("$")
    }

    fun matches(rawPassword: String, storedPassword: String): Boolean {
        if (!storedPassword.startsWith("$PREFIX$")) {
            return MessageDigest.isEqual(
                rawPassword.toByteArray(Charsets.UTF_8),
                storedPassword.toByteArray(Charsets.UTF_8)
            )
        }

        val parts = storedPassword.split('$')
        if (parts.size != 4) return false

        return runCatching {
            val iterations = parts[1].toInt()
            val salt = Base64.getDecoder().decode(parts[2])
            val expected = Base64.getDecoder().decode(parts[3])
            val actual = derive(rawPassword, salt, iterations)
            MessageDigest.isEqual(actual, expected)
        }.getOrDefault(false)
    }

    fun isEncoded(storedPassword: String): Boolean = storedPassword.startsWith("$PREFIX$")

    private fun derive(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}
