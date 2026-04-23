package com.linhavital.backend.service

import com.linhavital.backend.model.Monitoramento
import com.linhavital.backend.repository.MonitoramentoRepository
import org.springframework.stereotype.Service

@Service
class MonitoramentoService(private val repository: MonitoramentoRepository) {

    fun listar() = repository.findAll()

    fun salvar(monitoramento: Monitoramento) = repository.save(monitoramento)
}