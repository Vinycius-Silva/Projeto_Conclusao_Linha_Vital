package com.linhavital.backend.security

data class AuthenticatedUser(
    val usuarioId: Long,
    val email: String
)