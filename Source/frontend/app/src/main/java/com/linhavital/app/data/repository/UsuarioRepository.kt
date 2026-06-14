package com.linhavital.app.data.repository

import com.linhavital.app.data.api.ApiClient
import com.linhavital.app.data.api.ApiService
import com.linhavital.app.data.model.Usuario

class UsuarioRepository {

    private val api = ApiClient.create<ApiService>()

    suspend fun cadastrar(usuario: Usuario): Result<Usuario> {
        return try {
            val response = api.criarUsuario(usuario)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, senha: String): Result<Usuario> {
        return try {
            val usuarios = api.getUsuarios()
            val usuario = usuarios.find {
                it.emailUsuario == email && it.senhaUsuario == senha
            }
            if (usuario != null) {
                Result.success(usuario)
            } else {
                Result.failure(Exception("E-mail ou senha incorretos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}