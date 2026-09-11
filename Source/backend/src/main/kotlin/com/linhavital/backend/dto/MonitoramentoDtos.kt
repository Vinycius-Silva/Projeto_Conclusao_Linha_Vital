package com.linhavital.backend.dto

import java.time.LocalDateTime

data class MonitoramentoConfiguracaoRequest(
    val ativo: Boolean,
    val intervaloMinutos: Int
)

data class MonitoramentoStatusResponse(
    val ativo: Boolean,
    val intervaloMinutos: Int,
    val ultimaConfirmacao: LocalDateTime,
    val proximoCheckIn: LocalDateTime,
    val minutosRestantes: Long,
    val checkInPendente: Boolean,
    val alertaInatividadeAberto: Boolean
)
