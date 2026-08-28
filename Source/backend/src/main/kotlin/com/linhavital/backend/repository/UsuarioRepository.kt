package com.linhavital.backend.repository

import com.linhavital.backend.model.Usuario
import org.springframework.data.jpa.repository.JpaRepository

interface UsuarioRepository : JpaRepository<Usuario, Long> {
    fun findByEmailIgnoreCase(email: String): Usuario?
    fun existsByEmailIgnoreCase(email: String): Boolean
}
