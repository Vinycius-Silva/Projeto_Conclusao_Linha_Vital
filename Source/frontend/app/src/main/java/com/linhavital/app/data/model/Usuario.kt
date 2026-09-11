package com.linhavital.app.data.model

import com.google.gson.annotations.SerializedName

data class Usuario(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("nome") val nomeUsuario: String,
    @SerializedName("email") val emailUsuario: String,
    @SerializedName("senha") val senhaUsuario: String,
    @SerializedName("telefone") val telefoneUsuario: String,
    @SerializedName("dataNascimento") val dataNascimento: String
)

data class UsuarioSessao(
    @SerializedName("id") val id: Long,
    @SerializedName("nome") val nome: String,
    @SerializedName("email") val email: String,
    @SerializedName("telefone") val telefone: String,
    @SerializedName("dataNascimento") val dataNascimento: String
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("senha") val senha: String
)
