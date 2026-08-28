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

    @GetMapping("/usuario/{usuarioId}")
    fun listarPorUsuario(@PathVariable usuarioId: Long) = service.listarPorUsuario(usuarioId)

    @PostMapping("/usuario/{usuarioId}")
    fun criarParaUsuario(
        @PathVariable usuarioId: Long,
        @RequestBody contato: ContatoEmergencia
    ) = service.salvarParaUsuario(usuarioId, contato)

    @PutMapping("/usuario/{usuarioId}/{contatoId}")
    fun atualizarDoUsuario(
        @PathVariable usuarioId: Long,
        @PathVariable contatoId: Long,
        @RequestBody contato: ContatoEmergencia
    ) = service.atualizarDoUsuario(usuarioId, contatoId, contato)

    @DeleteMapping("/usuario/{usuarioId}/{contatoId}")
    fun deletarDoUsuario(
        @PathVariable usuarioId: Long,
        @PathVariable contatoId: Long
    ) = service.deletarDoUsuario(usuarioId, contatoId)
}
