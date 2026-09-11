package com.linhavital.backend.dto

import com.linhavital.backend.model.Usuario

data class LoginRequest(
    val email: String,
    val senha: String
)

data class UsuarioResponse(
    val id: Long,
    val nome: String,
    val email: String,
    val telefone: String,
    val dataNascimento: String
)

fun Usuario.toResponse() = UsuarioResponse(
    id = id,
    nome = nome,
    email = email,
    telefone = telefone,
    dataNascimento = dataNascimento.toString()
)
