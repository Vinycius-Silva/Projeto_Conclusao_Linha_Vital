package com.linhavital.backend.controller

import com.linhavital.backend.dto.TentativaContatoRequest
import com.linhavital.backend.model.HistoricoNotificacao
import com.linhavital.backend.service.HistoricoNotificacaoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/notificacoes")
class HistoricoNotificacaoController(
    private val service: HistoricoNotificacaoService
) {

    @GetMapping
    fun listar(): List<HistoricoNotificacao> =
        service.listar()

    @PostMapping
    fun criar(
        @RequestBody notificacao: HistoricoNotificacao
    ): HistoricoNotificacao =
        service.salvar(notificacao)

    @PostMapping("/alerta/{alertaId}/tentativa")
    fun registrarTentativa(
        @PathVariable alertaId: Long,
        @RequestBody request: TentativaContatoRequest
    ): ResponseEntity<Map<String, Any>> {

        val historico = service.registrarTentativa(
            alertaId = alertaId,
            contatoId = request.contatoId,
            statusOriginal = request.status
        )

        return ResponseEntity.ok(
            mapOf(
                "mensagem" to "Tentativa de contato registrada com sucesso",
                "idNotificacao" to historico.id,
                "alertaId" to historico.alerta.id,
                "contatoId" to historico.contato.id,
                "contatoNome" to historico.contato.nome,
                "status" to historico.status,
                "dataHora" to historico.dataHora.toString()
            )
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun tratarArgumentoInvalido(
        exception: IllegalArgumentException
    ): ResponseEntity<Map<String, String>> {

        return ResponseEntity
            .badRequest()
            .body(
                mapOf(
                    "erro" to (exception.message ?: "Requisição inválida")
                )
            )
    }
}