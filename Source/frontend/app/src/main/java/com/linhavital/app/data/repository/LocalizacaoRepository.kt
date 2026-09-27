package com.linhavital.app.data.repository

import com.linhavital.app.data.api.ApiClient
import com.linhavital.app.data.api.ApiService
import com.linhavital.app.data.model.LocalizacaoRequest
import com.linhavital.app.data.model.LocalizacaoResponse

class LocalizacaoRepository {

    private val api =
        ApiClient.create<ApiService>()

    suspend fun registrar(
        latitude: Double,
        longitude: Double
    ): Result<LocalizacaoResponse> =
        runCatching {

            api.registrarLocalizacao(
                LocalizacaoRequest(
                    latitude = latitude,
                    longitude = longitude
                )
            )
        }

    suspend fun obterUltima():
            Result<LocalizacaoResponse> =
        runCatching {

            api.obterUltimaLocalizacao()
        }

    suspend fun listar():
            Result<List<LocalizacaoResponse>> =
        runCatching {

            api.listarLocalizacoes()
        }
}