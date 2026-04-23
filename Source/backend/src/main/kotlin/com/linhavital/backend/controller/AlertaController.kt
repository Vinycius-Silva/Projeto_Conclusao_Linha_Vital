package com.linhavital.backend.controller

import com.linhavital.backend.model.Alerta
import com.linhavital.backend.service.AlertaService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/alertas")
class AlertaController(private val service: AlertaService) {

    @GetMapping
    fun listar() = service.listar()

    @PostMapping
    fun criar(@RequestBody alerta: Alerta) = service.salvar(alerta)

    @DeleteMapping("/{id}")
    fun deletar(@PathVariable id: Long) = service.deletar(id)
}