package com.linhavital.backend.service

import com.linhavital.backend.dto.UsuarioResponse
import com.linhavital.backend.dto.toResponse
import com.linhavital.backend.repository.UsuarioRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val usuarioRepository: UsuarioRepository,
    private val passwordService: PasswordService
) {
    @Transactional
    fun login(email: String, senha: String): UsuarioResponse {
        val usuario = usuarioRepository.findByEmailIgnoreCase(email.trim())
            ?: throw IllegalArgumentException("E-mail ou senha incorretos")

        if (!passwordService.matches(senha, usuario.senha)) {
            throw IllegalArgumentException("E-mail ou senha incorretos")
        }

        // Migra senhas legadas em texto puro no primeiro login bem-sucedido.
        if (!passwordService.isEncoded(usuario.senha)) {
            usuario.senha = passwordService.hash(senha)
            usuarioRepository.save(usuario)
        }

        return usuario.toResponse()
    }
}
