package com.linhavital.backend.dto

import com.linhavital.backend.model.Usuario

data class LoginRequest(
    val email: String,
    val senha: String
)

data class GoogleLoginRequest(
    val idToken: String
)

data class GoogleCadastroRequest(
    val idToken: String,
    val telefone: String,
    val dataNascimento: String
)

data class UsuarioResponse(
    val id: Long,
    val nome: String,
    val email: String,
    val telefone: String,
    val dataNascimento: String
)

data class AuthResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresAt: String,
    val usuario: UsuarioResponse
)

data class GoogleLoginResponse(
    val cadastroNecessario: Boolean,
    val autenticacao: AuthResponse? = null,
    val nomeGoogle: String? = null,
    val emailGoogle: String? = null
)

fun Usuario.toResponse() = UsuarioResponse(
    id = id,
    nome = nome,
    email = email,
    telefone = telefone,
    dataNascimento = dataNascimento.toString()
)