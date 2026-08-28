package com.linhavital.backend.service

import com.linhavital.backend.dto.UsuarioResponse
import com.linhavital.backend.dto.toResponse
import com.linhavital.backend.model.Usuario
import com.linhavital.backend.repository.UsuarioRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UsuarioService(
    private val repository: UsuarioRepository,
    private val passwordService: PasswordService
) {

    fun listar(): List<UsuarioResponse> = repository.findAll().map { it.toResponse() }

    fun buscarPorId(id: Long): Usuario =
        repository.findById(id).orElseThrow { RuntimeException("Usuário não encontrado") }

    fun buscarRespostaPorId(id: Long): UsuarioResponse = buscarPorId(id).toResponse()

    @Transactional
    fun salvar(usuario: Usuario): UsuarioResponse {
        val emailNormalizado = usuario.email.trim().lowercase()
        if (repository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw IllegalArgumentException("Já existe uma conta com este e-mail")
        }
        require(usuario.nome.isNotBlank()) { "Nome é obrigatório" }
        require(usuario.senha.length >= 6) { "Senha deve ter no mínimo 6 caracteres" }

        usuario.email = emailNormalizado
        usuario.senha = passwordService.hash(usuario.senha)
        return repository.save(usuario).toResponse()
    }

    @Transactional
    fun atualizar(id: Long, usuarioAtualizado: Usuario): UsuarioResponse {
        val usuario = buscarPorId(id)
        val novoEmail = usuarioAtualizado.email.trim().lowercase()
        val outroUsuario = repository.findByEmailIgnoreCase(novoEmail)
        if (outroUsuario != null && outroUsuario.id != id) {
            throw IllegalArgumentException("Já existe uma conta com este e-mail")
        }

        usuario.nome = usuarioAtualizado.nome
        usuario.email = novoEmail
        usuario.telefone = usuarioAtualizado.telefone
        usuario.dataNascimento = usuarioAtualizado.dataNascimento
        if (usuarioAtualizado.senha.isNotBlank()) {
            usuario.senha = passwordService.hash(usuarioAtualizado.senha)
        }

        return repository.save(usuario).toResponse()
    }

    fun deletar(id: Long) {
        repository.delete(buscarPorId(id))
    }
}
