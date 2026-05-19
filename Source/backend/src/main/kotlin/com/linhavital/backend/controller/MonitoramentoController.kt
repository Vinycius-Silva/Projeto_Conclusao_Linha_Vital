package com.linhavital.backend.controller

import com.linhavital.backend.service.MonitoramentoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/monitoramento")
class MonitoramentoController(
    val monitoramentoService: MonitoramentoService
) {

    @PostMapping("/atividade/{usuarioId}")
    fun registrarAtividade(@PathVariable usuarioId: Long): ResponseEntity<String> {
        monitoramentoService.registrarAtividade(usuarioId)
        return ResponseEntity.ok("Atividade registrada")
    }
}