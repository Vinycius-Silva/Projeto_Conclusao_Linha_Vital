package com.linhavital.backend.service

import com.linhavital.backend.model.Dispositivo
import com.linhavital.backend.repository.DispositivoRepository
import org.springframework.stereotype.Service

@Service
class DispositivoService(private val repository: DispositivoRepository) {

    fun listar() = repository.findAll()

    fun salvar(dispositivo: Dispositivo) = repository.save(dispositivo)
}