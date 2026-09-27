package com.linhavital.app.data.model

data class LocalizacaoRequest(
    val latitude: Double,
    val longitude: Double
)

data class LocalizacaoResponse(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val dataHora: String
)