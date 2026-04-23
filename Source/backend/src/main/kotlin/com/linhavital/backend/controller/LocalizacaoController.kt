package com.linhavital.backend.controller

import com.linhavital.backend.model.Localizacao
import com.linhavital.backend.service.LocalizacaoService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/localizacoes")
class LocalizacaoController(private val service: LocalizacaoService) {

    @GetMapping
    fun listar() = service.listar()

    @PostMapping
    fun criar(@RequestBody localizacao: Localizacao) =
        service.salvar(localizacao)
}