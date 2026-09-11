package com.linhavital.backend

import com.linhavital.backend.service.PasswordService
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PasswordServiceTests {
    private val service = PasswordService()

    @Test
    fun `hash nao armazena a senha em texto puro e valida a senha correta`() {
        val raw = "SenhaSegura123"
        val stored = service.hash(raw)

        assertNotEquals(raw, stored)
        assertTrue(stored.startsWith("pbkdf2_sha256$"))
        assertTrue(service.matches(raw, stored))
        assertFalse(service.matches("senha-incorreta", stored))
    }

    @Test
    fun `senha legada em texto puro ainda pode ser validada para migracao`() {
        assertTrue(service.matches("legada123", "legada123"))
        assertFalse(service.matches("outra", "legada123"))
    }
}
