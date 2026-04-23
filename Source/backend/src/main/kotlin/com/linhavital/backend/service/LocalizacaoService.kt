package com.linhavital.backend.service

import com.linhavital.backend.model.Localizacao
import com.linhavital.backend.repository.LocalizacaoRepository
import org.springframework.stereotype.Service

@Service
class LocalizacaoService(private val repository: LocalizacaoRepository) {

    fun listar() = repository.findAll()

    fun salvar(localizacao: Localizacao) = repository.save(localizacao)
}