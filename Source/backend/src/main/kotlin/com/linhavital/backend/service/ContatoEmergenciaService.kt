package com.linhavital.backend.service

import com.linhavital.backend.model.ContatoEmergencia
import com.linhavital.backend.repository.ContatoEmergenciaRepository
import org.springframework.stereotype.Service

@Service
class ContatoEmergenciaService(private val repository: ContatoEmergenciaRepository) {

    fun listar() = repository.findAll()

    fun buscarPorId(id: Long) =
        repository.findById(id).orElseThrow { RuntimeException("Contato não encontrado") }

    fun salvar(contato: ContatoEmergencia) = repository.save(contato)

    fun deletar(id: Long) = repository.deleteById(id)
}