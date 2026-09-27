package com.linhavital.backend.controller

import com.linhavital.backend.dto.TentativaContatoRequest
import com.linhavital.backend.model.HistoricoNotificacao
import com.linhavital.backend.service.AuthorizationService
import com.linhavital.backend.service.HistoricoNotificacaoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/notificacoes")
class HistoricoNotificacaoController(
    private val service: HistoricoNotificacaoService,
    private val authorizationService: AuthorizationService
) {

    @GetMapping
    fun listar(): List<HistoricoNotificacao> =
        service.listarPorUsuario(
            authorizationService.usuarioAtualId()
        )

    @PostMapping
    fun criar(
        @RequestBody notificacao:
        HistoricoNotificacao
    ): HistoricoNotificacao {

        authorizationService
            .exigirAlerta(
                notificacao.alerta.id
            )

        authorizationService
            .exigirContato(
                notificacao.contato.id
            )

        return service.salvar(
            notificacao
        )
    }

    @PostMapping(
        "/alerta/{alertaId}/tentativa"
    )
    fun registrarTentativa(
        @PathVariable alertaId: Long,
        @RequestBody request:
        TentativaContatoRequest
    ): ResponseEntity<Map<String, Any>> {

        authorizationService
            .exigirAlerta(
                alertaId
            )

        authorizationService
            .exigirContato(
                request.contatoId
            )

        val historico =
            service.registrarTentativa(
                alertaId = alertaId,
                contatoId = request.contatoId,
                statusOriginal = request.status
            )

        return ResponseEntity.ok(
            mapOf(
                "mensagem" to
                        "Tentativa de contato registrada com sucesso",

                "idNotificacao" to
                        historico.id,

                "alertaId" to
                        historico.alerta.id,

                "contatoId" to
                        historico.contato.id,

                "contatoNome" to
                        historico.contato.nome,

                "status" to
                        historico.status,

                "dataHora" to
                        historico.dataHora.toString()
            )
        )
    }
}