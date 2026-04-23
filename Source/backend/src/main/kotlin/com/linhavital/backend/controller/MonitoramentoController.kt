package com.linhavital.backend.controller

import com.linhavital.backend.model.Monitoramento
import com.linhavital.backend.service.MonitoramentoService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/monitoramentos")
class MonitoramentoController(private val service: MonitoramentoService) {

    @GetMapping
    fun listar() = service.listar()

    @PostMapping
    fun criar(@RequestBody monitoramento: Monitoramento) =
        service.salvar(monitoramento)
}