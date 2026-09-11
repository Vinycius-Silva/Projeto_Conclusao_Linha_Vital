package com.linhavital.backend.controller

import com.linhavital.backend.model.Alerta
import com.linhavital.backend.repository.AlertaRepository
import com.linhavital.backend.service.AlertaService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/alerta")
class AlertaController(
    private val alertaService: AlertaService,
    private val alertaRepository: AlertaRepository
) {

    @GetMapping
    fun listar(): List<Alerta> = alertaRepository.findAll()

    @GetMapping("/usuario/{usuarioId}")
    fun listarPorUsuario(@PathVariable usuarioId: Long): List<Alerta> =
        alertaService.listarPorUsuario(usuarioId)

    @PostMapping("/panico/{usuarioId}")
    fun alertaPanico(@PathVariable usuarioId: Long): ResponseEntity<Map<String, Any>> {
        val alerta = alertaService.criarAlertaPanico(usuarioId)
        return ResponseEntity.ok(
            mapOf(
                "mensagem" to "Alerta de pânico registrado com sucesso",
                "idAlerta" to alerta.id,
                "tipo" to alerta.tipo,
                "status" to alerta.status
            )
        )
    }
}
