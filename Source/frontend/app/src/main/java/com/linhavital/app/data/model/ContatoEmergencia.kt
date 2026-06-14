package com.linhavital.app.data.model

import com.google.gson.annotations.SerializedName

data class ContatoEmergencia(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("nome") val nome: String,
    @SerializedName("telefone") val telefone: String,
    @SerializedName("email") val email: String = "",
    @SerializedName("tipoContato") val tipoContato: String = "Familiar"
)