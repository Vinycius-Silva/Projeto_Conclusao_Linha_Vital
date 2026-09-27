package com.linhavital.backend.service

import com.linhavital.backend.dto.LocalizacaoRequest
import com.linhavital.backend.dto.LocalizacaoResponse
import com.linhavital.backend.model.Localizacao
import com.linhavital.backend.repository.LocalizacaoRepository
import com.linhavital.backend.repository.UsuarioRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class LocalizacaoService(
    private val repository: LocalizacaoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val authorizationService: AuthorizationService
) {

    fun registrarLocalizacao(
        request: LocalizacaoRequest
    ): LocalizacaoResponse {

        validarCoordenadas(
            latitude = request.latitude,
            longitude = request.longitude
        )

        val usuarioId =
            authorizationService
                .usuarioAtualId()

        val usuario =
            usuarioRepository
                .findById(usuarioId)
                .orElseThrow {
                    IllegalArgumentException(
                        "Usuário não encontrado."
                    )
                }

        val localizacao =
            Localizacao(
                latitude = request.latitude,
                longitude = request.longitude,
                dataHora = LocalDateTime.now(),
                usuario = usuario
            )

        return repository
            .save(localizacao)
            .toResponse()
    }

    fun listarDoUsuarioAtual():
            List<LocalizacaoResponse> {

        val usuarioId =
            authorizationService
                .usuarioAtualId()

        return repository
            .findByUsuarioIdOrderByDataHoraDesc(
                usuarioId
            )
            .map {
                it.toResponse()
            }
    }

    fun obterUltimaDoUsuarioAtual():
            LocalizacaoResponse? {

        val usuarioId =
            authorizationService
                .usuarioAtualId()

        return repository
            .findFirstByUsuarioIdOrderByDataHoraDesc(
                usuarioId
            )
            ?.toResponse()
    }

    private fun validarCoordenadas(
        latitude: Double,
        longitude: Double
    ) {

        require(
            latitude in -90.0..90.0
        ) {
            "Latitude inválida."
        }

        require(
            longitude in -180.0..180.0
        ) {
            "Longitude inválida."
        }
    }

    private fun Localizacao.toResponse():
            LocalizacaoResponse {

        return LocalizacaoResponse(
            id = id,
            latitude = latitude,
            longitude = longitude,
            dataHora = dataHora
        )
    }
}