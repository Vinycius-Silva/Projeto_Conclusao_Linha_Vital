package com.linhavital.app.data.model

import com.google.gson.annotations.SerializedName

data class MonitoramentoConfiguracaoRequest(
    @SerializedName("ativo") val ativo: Boolean,
    @SerializedName("intervaloMinutos") val intervaloMinutos: Int
)

data class MonitoramentoStatus(
    @SerializedName("ativo") val ativo: Boolean,
    @SerializedName("intervaloMinutos") val intervaloMinutos: Int,
    @SerializedName("ultimaConfirmacao") val ultimaConfirmacao: String,
    @SerializedName("proximoCheckIn") val proximoCheckIn: String,
    @SerializedName("minutosRestantes") val minutosRestantes: Long,
    @SerializedName("checkInPendente") val checkInPendente: Boolean,
    @SerializedName("alertaInatividadeAberto") val alertaInatividadeAberto: Boolean
)
