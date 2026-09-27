package com.linhavital.backend.controller

import com.linhavital.backend.model.Alerta
import com.linhavital.backend.service.AlertaService
import com.linhavital.backend.service.AuthorizationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/alerta")
class AlertaController(
    private val alertaService: AlertaService,
    private val authorizationService: AuthorizationService
) {

    @GetMapping
    fun listar(): List<Alerta> =
        alertaService.listarPorUsuario(
            authorizationService.usuarioAtualId()
        )

    @GetMapping("/usuario/{usuarioId}")
    fun listarPorUsuario(
        @PathVariable usuarioId: Long
    ): List<Alerta> {

        authorizationService
            .exigirUsuario(
                usuarioId
            )

        return alertaService
            .listarPorUsuario(
                usuarioId
            )
    }

    @PostMapping("/panico/{usuarioId}")
    fun alertaPanico(
        @PathVariable usuarioId: Long
    ): ResponseEntity<Map<String, Any>> {

        authorizationService
            .exigirUsuario(
                usuarioId
            )

        val alerta =
            alertaService
                .criarAlertaPanico(
                    usuarioId
                )

        return ResponseEntity.ok(
            mapOf(
                "mensagem" to
                        "Alerta de pânico registrado com sucesso",

                "idAlerta" to
                        alerta.id,

                "tipo" to
                        alerta.tipo,

                "status" to
                        alerta.status
            )
        )
    }
}