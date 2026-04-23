package com.linhavital.backend.controller

import com.linhavital.backend.model.Dispositivo
import com.linhavital.backend.service.DispositivoService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/dispositivos")
class DispositivoController(private val service: DispositivoService) {

    @GetMapping
    fun listar() = service.listar()

    @PostMapping
    fun criar(@RequestBody dispositivo: Dispositivo) =
        service.salvar(dispositivo)
}