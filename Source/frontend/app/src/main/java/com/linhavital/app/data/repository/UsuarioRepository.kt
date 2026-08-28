package com.linhavital.app.data.repository

import com.linhavital.app.data.api.ApiClient
import com.linhavital.app.data.api.ApiService
import com.linhavital.app.data.model.LoginRequest
import com.linhavital.app.data.model.Usuario
import com.linhavital.app.data.model.UsuarioSessao
import retrofit2.HttpException

class UsuarioRepository {

    private val api = ApiClient.create<ApiService>()

    suspend fun cadastrar(usuario: Usuario): Result<UsuarioSessao> = runCatching {
        api.criarUsuario(usuario)
    }.mapFailure()

    suspend fun login(email: String, senha: String): Result<UsuarioSessao> = runCatching {
        api.login(LoginRequest(email.trim(), senha))
    }.mapFailure("E-mail ou senha incorretos")

    private fun <T> Result<T>.mapFailure(defaultMessage: String = "Não foi possível concluir a operação"): Result<T> {
        val exception = exceptionOrNull() ?: return this
        val message = if (exception is HttpException) {
            when (exception.code()) {
                400, 401, 404 -> defaultMessage
                else -> "Servidor indisponível (${exception.code()})"
            }
        } else {
            exception.message ?: defaultMessage
        }
        return Result.failure(IllegalStateException(message, exception))
    }
}
