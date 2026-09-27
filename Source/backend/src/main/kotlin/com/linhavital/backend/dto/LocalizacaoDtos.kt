package com.linhavital.backend.dto

import java.time.LocalDateTime

data class LocalizacaoRequest(
    val latitude: Double,
    val longitude: Double
)

data class LocalizacaoResponse(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val dataHora: LocalDateTime
)