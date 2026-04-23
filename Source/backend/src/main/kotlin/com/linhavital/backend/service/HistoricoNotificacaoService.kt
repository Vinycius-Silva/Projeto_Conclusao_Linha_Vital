package com.linhavital.backend.service

import com.linhavital.backend.model.HistoricoNotificacao
import com.linhavital.backend.repository.HistoricoNotificacaoRepository
import org.springframework.stereotype.Service

@Service
class HistoricoNotificacaoService(private val repository: HistoricoNotificacaoRepository) {

    fun listar() = repository.findAll()

    fun salvar(notificacao: HistoricoNotificacao) =
        repository.save(notificacao)
}