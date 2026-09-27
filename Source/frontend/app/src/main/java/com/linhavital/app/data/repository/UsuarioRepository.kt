package com.linhavital.app.data.repository

import com.google.gson.Gson
import com.linhavital.app.data.api.ApiClient
import com.linhavital.app.data.api.ApiService
import com.linhavital.app.data.model.AuthResponse
import com.linhavital.app.data.model.GoogleCadastroRequest
import com.linhavital.app.data.model.GoogleLoginRequest
import com.linhavital.app.data.model.GoogleLoginResponse
import com.linhavital.app.data.model.LoginRequest
import com.linhavital.app.data.model.Usuario
import com.linhavital.app.data.model.UsuarioSessao
import retrofit2.HttpException

class UsuarioRepository {

    private val api =
        ApiClient.create<ApiService>()

    private val gson =
        Gson()

    suspend fun cadastrar(
        usuario: Usuario
    ): Result<UsuarioSessao> =
        runCatching {

            api.criarUsuario(
                usuario
            )

        }.mapFailure()

    suspend fun login(
        email: String,
        senha: String
    ): Result<AuthResponse> =
        runCatching {

            api.login(
                LoginRequest(
                    email = email.trim(),
                    senha = senha
                )
            )

        }.mapFailure(
            "E-mail ou senha incorretos"
        )

    suspend fun loginGoogle(
        idToken: String
    ): Result<GoogleLoginResponse> =
        runCatching {

            api.loginGoogle(
                GoogleLoginRequest(
                    idToken = idToken
                )
            )

        }.mapFailure(
            "Não foi possível entrar com o Google"
        )

    suspend fun cadastrarGoogle(
        idToken: String,
        telefone: String,
        dataNascimento: String
    ): Result<AuthResponse> =
        runCatching {

            api.cadastrarGoogle(
                GoogleCadastroRequest(
                    idToken = idToken,
                    telefone = telefone,
                    dataNascimento = dataNascimento
                )
            )

        }.mapFailure(
            "Não foi possível concluir o cadastro com Google"
        )

    suspend fun usuarioAtual():
            Result<UsuarioSessao> =
        runCatching {

            api.usuarioAtual()

        }.mapFailure(
            "Não foi possível recuperar sua sessão"
        )

    suspend fun logout():
            Result<Unit> =
        runCatching {

            api.logout()

            Unit

        }.mapFailure(
            "Não foi possível encerrar a sessão"
        )

    private fun <T> Result<T>.mapFailure(
        defaultMessage: String =
            "Não foi possível concluir a operação"
    ): Result<T> {

        val exception =
            exceptionOrNull()
                ?: return this

        val message =
            if (exception is HttpException) {

                extrairMensagemBackend(
                    exception
                )
                    ?: when (
                        exception.code()
                    ) {

                        400 ->
                            defaultMessage

                        401 ->
                            "Não autenticado"

                        403 ->
                            "Você não tem permissão para realizar essa operação"

                        404 ->
                            defaultMessage

                        409 ->
                            "Já existe um cadastro com essas informações"

                        500 ->
                            "Erro interno do servidor"

                        else ->
                            "Servidor indisponível (${exception.code()})"
                    }

            } else {

                exception.message
                    ?: defaultMessage
            }

        return Result.failure(
            IllegalStateException(
                message,
                exception
            )
        )
    }

    private fun extrairMensagemBackend(
        exception: HttpException
    ): String? {

        return runCatching {

            val json =
                exception
                    .response()
                    ?.errorBody()
                    ?.string()
                    ?: return@runCatching null

            val erro =
                gson.fromJson(
                    json,
                    ErroBackend::class.java
                )

            erro.erro

        }.getOrNull()
    }

    private data class ErroBackend(
        val erro: String?
    )
}