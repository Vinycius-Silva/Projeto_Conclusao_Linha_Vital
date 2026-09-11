package com.linhavital.backend.controller

import com.linhavital.backend.dto.MonitoramentoConfiguracaoRequest
import com.linhavital.backend.dto.MonitoramentoStatusResponse
import com.linhavital.backend.service.MonitoramentoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/monitoramento")
class MonitoramentoController(
    private val monitoramentoService: MonitoramentoService
) {

    @GetMapping("/status/{usuarioId}")
    fun status(@PathVariable usuarioId: Long): MonitoramentoStatusResponse =
        monitoramentoService.obterStatus(usuarioId)

    @PutMapping("/configuracao/{usuarioId}")
    fun configurar(
        @PathVariable usuarioId: Long,
        @RequestBody request: MonitoramentoConfiguracaoRequest
    ): MonitoramentoStatusResponse =
        monitoramentoService.atualizarConfiguracao(usuarioId, request)

    @PostMapping("/check-in/{usuarioId}")
    fun checkIn(@PathVariable usuarioId: Long): MonitoramentoStatusResponse =
        monitoramentoService.registrarCheckIn(usuarioId)

    @PostMapping("/atividade/{usuarioId}")
    fun registrarAtividade(@PathVariable usuarioId: Long): ResponseEntity<String> {
        monitoramentoService.registrarAtividade(usuarioId)
        return ResponseEntity.ok("Atividade registrada")
    }
}
