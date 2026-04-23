package com.linhavital.backend.service

import com.linhavital.backend.model.Alerta
import com.linhavital.backend.repository.AlertaRepository
import org.springframework.stereotype.Service

@Service
class AlertaService(private val repository: AlertaRepository) {

    fun listar() = repository.findAll()

    fun salvar(alerta: Alerta) = repository.save(alerta)

    fun deletar(id: Long) = repository.deleteById(id)
}