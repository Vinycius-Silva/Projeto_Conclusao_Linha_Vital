package com.linhavital.backend.controller

import com.linhavital.backend.dto.LocalizacaoRequest
import com.linhavital.backend.dto.LocalizacaoResponse
import com.linhavital.backend.service.LocalizacaoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/localizacoes")
class LocalizacaoController(
    private val service: LocalizacaoService
) {

    @GetMapping
    fun listar():
            List<LocalizacaoResponse> =
        service.listarDoUsuarioAtual()

    @GetMapping("/ultima")
    fun obterUltima():
            ResponseEntity<LocalizacaoResponse> {

        val localizacao =
            service.obterUltimaDoUsuarioAtual()

        return if (
            localizacao != null
        ) {
            ResponseEntity.ok(
                localizacao
            )
        } else {
            ResponseEntity
                .noContent()
                .build()
        }
    }

    @PostMapping
    fun registrar(
        @RequestBody request: LocalizacaoRequest
    ): LocalizacaoResponse =
        service.registrarLocalizacao(
            request
        )
}