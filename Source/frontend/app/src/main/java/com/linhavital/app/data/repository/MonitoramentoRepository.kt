package com.linhavital.app.data.repository

import com.linhavital.app.data.api.ApiClient
import com.linhavital.app.data.api.ApiService
import com.linhavital.app.data.model.MonitoramentoConfiguracaoRequest
import com.linhavital.app.data.model.MonitoramentoStatus

class MonitoramentoRepository {
    private val api = ApiClient.create<ApiService>()

    suspend fun obterStatus(usuarioId: Long): Result<MonitoramentoStatus> =
        runCatching { api.getMonitoramentoStatus(usuarioId) }

    suspend fun configurar(
        usuarioId: Long,
        ativo: Boolean,
        intervaloMinutos: Int
    ): Result<MonitoramentoStatus> = runCatching {
        api.configurarMonitoramento(
            usuarioId,
            MonitoramentoConfiguracaoRequest(ativo, intervaloMinutos)
        )
    }

    suspend fun checkIn(usuarioId: Long): Result<MonitoramentoStatus> =
        runCatching { api.registrarCheckIn(usuarioId) }
}
