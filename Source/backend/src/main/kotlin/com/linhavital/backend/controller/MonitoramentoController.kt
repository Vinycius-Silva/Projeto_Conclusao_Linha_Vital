package com.linhavital.backend.controller

import com.linhavital.backend.dto.MonitoramentoConfiguracaoRequest
import com.linhavital.backend.dto.MonitoramentoStatusResponse
import com.linhavital.backend.service.AuthorizationService
import com.linhavital.backend.service.MonitoramentoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/monitoramento")
class MonitoramentoController(
    private val monitoramentoService: MonitoramentoService,
    private val authorizationService: AuthorizationService
) {

    @GetMapping("/status/{usuarioId}")
    fun status(
        @PathVariable usuarioId: Long
    ): MonitoramentoStatusResponse {

        authorizationService
            .exigirUsuario(
                usuarioId
            )

        return monitoramentoService
            .obterStatus(
                usuarioId
            )
    }

    @PutMapping(
        "/configuracao/{usuarioId}"
    )
    fun configurar(
        @PathVariable usuarioId: Long,
        @RequestBody request:
        MonitoramentoConfiguracaoRequest
    ): MonitoramentoStatusResponse {

        authorizationService
            .exigirUsuario(
                usuarioId
            )

        return monitoramentoService
            .atualizarConfiguracao(
                usuarioId,
                request
            )
    }

    @PostMapping(
        "/check-in/{usuarioId}"
    )
    fun checkIn(
        @PathVariable usuarioId: Long
    ): MonitoramentoStatusResponse {

        authorizationService
            .exigirUsuario(
                usuarioId
            )

        return monitoramentoService
            .registrarCheckIn(
                usuarioId
            )
    }

    @PostMapping(
        "/atividade/{usuarioId}"
    )
    fun registrarAtividade(
        @PathVariable usuarioId: Long
    ): ResponseEntity<String> {

        authorizationService
            .exigirUsuario(
                usuarioId
            )

        monitoramentoService
            .registrarAtividade(
                usuarioId
            )

        return ResponseEntity.ok(
            "Atividade registrada"
        )
    }
}