package com.linhavital.backend.controller

import com.linhavital.backend.model.ContatoEmergencia
import com.linhavital.backend.service.ContatoEmergenciaService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/contatos")
class ContatoEmergenciaController(private val service: ContatoEmergenciaService) {

    @GetMapping
    fun listar() = service.listar()

    @GetMapping("/{id}")
    fun buscar(@PathVariable id: Long) = service.buscarPorId(id)

    @PostMapping
    fun criar(@RequestBody contato: ContatoEmergencia) = service.salvar(contato)

    @DeleteMapping("/{id}")
    fun deletar(@PathVariable id: Long) = service.deletar(id)
}