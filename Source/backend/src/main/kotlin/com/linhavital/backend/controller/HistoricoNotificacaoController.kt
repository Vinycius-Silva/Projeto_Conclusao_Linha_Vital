package com.linhavital.backend.controller

import com.linhavital.backend.model.HistoricoNotificacao
import com.linhavital.backend.service.HistoricoNotificacaoService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/notificacoes")
class HistoricoNotificacaoController(private val service: HistoricoNotificacaoService) {

    @GetMapping
    fun listar() = service.listar()

    @PostMapping
    fun criar(@RequestBody notificacao: HistoricoNotificacao) =
        service.salvar(notificacao)
}