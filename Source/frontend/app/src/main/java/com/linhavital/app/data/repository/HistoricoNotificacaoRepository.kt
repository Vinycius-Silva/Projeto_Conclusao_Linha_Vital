package com.linhavital.app.data.repository

import android.util.Log
import com.linhavital.app.data.api.ApiClient
import com.linhavital.app.data.api.ApiService
import com.linhavital.app.data.model.TentativaContatoRequest

class HistoricoNotificacaoRepository {

    private val api =
        ApiClient.create<ApiService>()

    suspend fun registrarTentativa(
        alertaId: Long,
        contatoId: Long,
        status: String
    ): Result<Unit> {

        return try {

            val request =
                TentativaContatoRequest(
                    contatoId = contatoId,
                    status = status
                )

            Log.d(
                "HistoricoNotificacao",
                "Registrando evento: " +
                        "alerta=$alertaId, " +
                        "contato=$contatoId, " +
                        "status=$status"
            )

            api.registrarTentativaContato(
                alertaId = alertaId,
                request = request
            )

            Log.d(
                "HistoricoNotificacao",
                "Evento $status registrado com sucesso."
            )

            Result.success(Unit)

        } catch (e: Exception) {

            Log.e(
                "HistoricoNotificacao",
                "Erro ao registrar status $status: ${e.message}",
                e
            )

            Result.failure(e)
        }
    }
}