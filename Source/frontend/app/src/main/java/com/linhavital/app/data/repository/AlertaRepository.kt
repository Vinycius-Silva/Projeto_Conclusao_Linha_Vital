package com.linhavital.app.data.repository

import android.util.Log
import com.linhavital.app.data.api.ApiClient
import com.linhavital.app.data.api.ApiService

class AlertaRepository {

    private val api = ApiClient.create<ApiService>()

    suspend fun registrarAlertaPanico(usuarioId: Long): Result<Unit> {
        return try {
            Log.d("AlertaRepository", "Registrando alerta de pânico para usuário ID: $usuarioId")
            api.criarAlertaPanico(usuarioId)
            Log.d("AlertaRepository", "Alerta de pânico registrado com sucesso")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AlertaRepository", "Erro ao registrar alerta: ${e.message}")
            Result.failure(e)
        }
    }
}