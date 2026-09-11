package com.linhavital.backend.controller

import com.linhavital.backend.dto.UsuarioResponse
import com.linhavital.backend.model.Usuario
import com.linhavital.backend.service.UsuarioService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/usuarios")
class UsuarioController(private val service: UsuarioService) {

    @GetMapping("/{id}")
    fun buscar(@PathVariable id: Long): UsuarioResponse = service.buscarRespostaPorId(id)

    @PostMapping
    fun criar(@RequestBody usuario: Usuario): UsuarioResponse = service.salvar(usuario)

    @PutMapping("/{id}")
    fun atualizar(@PathVariable id: Long, @RequestBody usuario: Usuario): UsuarioResponse =
        service.atualizar(id, usuario)

    @DeleteMapping("/{id}")
    fun deletar(@PathVariable id: Long) = service.deletar(id)
}
