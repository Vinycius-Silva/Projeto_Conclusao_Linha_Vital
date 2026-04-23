package com.linhavital.backend.controller

import com.linhavital.backend.model.Usuario
import com.linhavital.backend.service.UsuarioService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/usuarios")
class UsuarioController(private val service: UsuarioService) {

    @GetMapping
    fun listar(): List<Usuario> = service.listar()

    @GetMapping("/{id}")
    fun buscar(@PathVariable id: Long): Usuario =
        service.buscarPorId(id)

    @PostMapping
    fun criar(@RequestBody usuario: Usuario): Usuario =
        service.salvar(usuario)

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @RequestBody usuario: Usuario
    ): Usuario = service.atualizar(id, usuario)

    @DeleteMapping("/{id}")
    fun deletar(@PathVariable id: Long) =
        service.deletar(id)
}