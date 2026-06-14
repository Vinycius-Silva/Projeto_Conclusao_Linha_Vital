package com.linhavital.app.data.model

import com.google.gson.annotations.SerializedName

data class Usuario(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("nome") val nomeUsuario: String,
    @SerializedName("email") val emailUsuario: String,
    @SerializedName("senha") val senhaUsuario: String,
    @SerializedName("telefone") val telefoneUsuario: String? = "",
    @SerializedName("dataNascimento") val dataNascimento: String? = null
)