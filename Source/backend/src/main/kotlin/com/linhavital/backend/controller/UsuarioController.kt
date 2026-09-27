package com.linhavital.backend.controller

import com.linhavital.backend.dto.UsuarioResponse
import com.linhavital.backend.model.Usuario
import com.linhavital.backend.service.AuthorizationService
import com.linhavital.backend.service.UsuarioService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/usuarios")
class UsuarioController(
    private val service: UsuarioService,
    private val authorizationService: AuthorizationService
) {

    @GetMapping("/{id}")
    fun buscar(
        @PathVariable id: Long
    ): UsuarioResponse {

        authorizationService
            .exigirUsuario(
                id
            )

        return service
            .buscarRespostaPorId(
                id
            )
    }

    @PostMapping
    fun criar(
        @RequestBody usuario: Usuario
    ): UsuarioResponse =
        service.salvar(
            usuario
        )

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @RequestBody usuario: Usuario
    ): UsuarioResponse {

        authorizationService
            .exigirUsuario(
                id
            )

        return service.atualizar(
            id,
            usuario
        )
    }

    @DeleteMapping("/{id}")
    fun deletar(
        @PathVariable id: Long
    ) {

        authorizationService
            .exigirUsuario(
                id
            )

        service.deletar(
            id
        )
    }
}