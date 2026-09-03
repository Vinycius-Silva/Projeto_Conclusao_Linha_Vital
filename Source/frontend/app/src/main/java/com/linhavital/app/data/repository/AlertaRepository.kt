package com.linhavital.app.data.repository

import android.util.Log
import com.linhavital.app.data.api.ApiClient
import com.linhavital.app.data.api.ApiService

class AlertaRepository {

    private val api =
        ApiClient.create<ApiService>()

    /*
     * Registra o alerta de pânico e devolve
     * o ID criado pelo backend.
     *
     * Esse ID será usado para relacionar
     * todas as tentativas da cascata.
     */
    suspend fun registrarAlertaPanico(
        usuarioId: Long
    ): Result<Long> {

        return try {

            Log.d(
                "AlertaRepository",
                "Registrando alerta de pânico para usuário ID: $usuarioId"
            )

            val resposta =
                api.criarAlertaPanico(usuarioId)

            val idAlerta =
                (resposta["idAlerta"] as? Number)
                    ?.toLong()
                    ?: throw IllegalStateException(
                        "Backend não retornou o idAlerta."
                    )

            Log.d(
                "AlertaRepository",
                "Alerta de pânico registrado com sucesso. ID: $idAlerta"
            )

            Result.success(idAlerta)

        } catch (e: Exception) {

            Log.e(
                "AlertaRepository",
                "Erro ao registrar alerta de pânico: ${e.message}",
                e
            )

            Result.failure(e)
        }
    }
}