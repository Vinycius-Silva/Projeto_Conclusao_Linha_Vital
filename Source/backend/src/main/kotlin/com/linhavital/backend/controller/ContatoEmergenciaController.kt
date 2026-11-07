package com.linhavital.backend.controller

import com.linhavital.backend.dto.ReordenarContatosRequest
import com.linhavital.backend.model.ContatoEmergencia
import com.linhavital.backend.service.AuthorizationService
import com.linhavital.backend.service.ContatoEmergenciaService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/contatos")
class ContatoEmergenciaController(
    private val service:
    ContatoEmergenciaService,

    private val authorizationService:
    AuthorizationService
) {

    @GetMapping
    fun listar() =
        service.listarPorUsuario(
            authorizationService.usuarioAtualId()
        )

    @GetMapping("/{id}")
    fun buscar(
        @PathVariable
        id: Long
    ): ContatoEmergencia {

        authorizationService
            .exigirContato(
                id
            )

        return service.buscarPorId(
            id
        )
    }

    @PostMapping
    fun criar(
        @RequestBody
        contato: ContatoEmergencia
    ): ContatoEmergencia =
        service.salvarParaUsuario(
            authorizationService.usuarioAtualId(),
            contato
        )

    @DeleteMapping("/{id}")
    fun deletar(
        @PathVariable
        id: Long
    ) {

        authorizationService
            .exigirContato(
                id
            )

        service.deletarDoUsuario(
            authorizationService.usuarioAtualId(),
            id
        )
    }

    @GetMapping(
        "/usuario/{usuarioId}"
    )
    fun listarPorUsuario(
        @PathVariable
        usuarioId: Long
    ): List<ContatoEmergencia> {

        authorizationService
            .exigirUsuario(
                usuarioId
            )

        return service.listarPorUsuario(
            usuarioId
        )
    }

    @PostMapping(
        "/usuario/{usuarioId}"
    )
    fun criarParaUsuario(
        @PathVariable
        usuarioId: Long,

        @RequestBody
        contato: ContatoEmergencia
    ): ContatoEmergencia {

        authorizationService
            .exigirUsuario(
                usuarioId
            )

        return service.salvarParaUsuario(
            usuarioId,
            contato
        )
    }

    /*
     * ===================================================
     * REORDENAÇÃO
     * ===================================================
     *
     * Exemplo:
     *
     * PUT /contatos/usuario/1/ordem
     *
     * {
     *   "contatoIds": [8, 3, 5]
     * }
     *
     * Resultado:
     *
     * contato 8 -> prioridade 1
     * contato 3 -> prioridade 2
     * contato 5 -> prioridade 3
     */
    @PutMapping(
        "/usuario/{usuarioId}/ordem"
    )
    fun reordenarContatos(
        @PathVariable
        usuarioId: Long,

        @RequestBody
        request: ReordenarContatosRequest
    ): List<ContatoEmergencia> {

        authorizationService
            .exigirUsuario(
                usuarioId
            )

        return service.reordenarContatos(
            usuarioId =
                usuarioId,

            contatoIds =
                request.contatoIds
        )
    }

    @PutMapping(
        "/usuario/{usuarioId}/{contatoId}"
    )
    fun atualizarDoUsuario(
        @PathVariable
        usuarioId: Long,

        @PathVariable
        contatoId: Long,

        @RequestBody
        contato: ContatoEmergencia
    ): ContatoEmergencia {

        authorizationService
            .exigirUsuario(
                usuarioId
            )

        authorizationService
            .exigirContato(
                contatoId
            )

        return service.atualizarDoUsuario(
            usuarioId,
            contatoId,
            contato
        )
    }

    @DeleteMapping(
        "/usuario/{usuarioId}/{contatoId}"
    )
    fun deletarDoUsuario(
        @PathVariable
        usuarioId: Long,

        @PathVariable
        contatoId: Long
    ) {

        authorizationService
            .exigirUsuario(
                usuarioId
            )

        authorizationService
            .exigirContato(
                contatoId
            )

        service.deletarDoUsuario(
            usuarioId,
            contatoId
        )
    }
}