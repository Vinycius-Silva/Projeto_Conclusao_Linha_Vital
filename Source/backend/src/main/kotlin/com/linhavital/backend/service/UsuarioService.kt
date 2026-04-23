package com.linhavital.backend.service

import com.linhavital.backend.model.Usuario
import com.linhavital.backend.repository.UsuarioRepository
import org.springframework.stereotype.Service

@Service
class UsuarioService(private val repository: UsuarioRepository) {

    fun listar(): List<Usuario> = repository.findAll()

    fun buscarPorId(id: Long): Usuario =
        repository.findById(id).orElseThrow {
            RuntimeException("Usuário não encontrado")
        }

    fun salvar(usuario: Usuario): Usuario = repository.save(usuario)

    fun atualizar(id: Long, usuarioAtualizado: Usuario): Usuario {
        val usuario = buscarPorId(id)

        val novoUsuario = usuario.copy(
            nome = usuarioAtualizado.nome,
            email = usuarioAtualizado.email,
            telefone = usuarioAtualizado.telefone,
            senha = usuarioAtualizado.senha,
            dataNascimento = usuarioAtualizado.dataNascimento
        )

        return repository.save(novoUsuario)
    }

    fun deletar(id: Long) {
        val usuario = buscarPorId(id)
        repository.delete(usuario)
    }
}