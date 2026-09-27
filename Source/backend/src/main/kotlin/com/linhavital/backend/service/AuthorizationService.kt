package com.linhavital.backend.service

import com.linhavital.backend.exception.UnauthorizedException
import com.linhavital.backend.repository.AlertaRepository
import com.linhavital.backend.repository.UsuarioContatoRepository
import com.linhavital.backend.security.AuthenticatedUser
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class AuthorizationService(
    private val usuarioContatoRepository: UsuarioContatoRepository,
    private val alertaRepository: AlertaRepository
) {

    fun usuarioAtual(): AuthenticatedUser {

        val principal =
            SecurityContextHolder
                .getContext()
                .authentication
                ?.principal

        return principal as? AuthenticatedUser
            ?: throw UnauthorizedException(
                "Usuário não autenticado."
            )
    }

    fun usuarioAtualId(): Long =
        usuarioAtual().usuarioId

    fun exigirUsuario(
        usuarioId: Long
    ) {

        if (
            usuarioAtualId() != usuarioId
        ) {
            throw AccessDeniedException(
                "Você não possui acesso aos dados deste usuário."
            )
        }
    }

    fun exigirContato(
        contatoId: Long
    ) {

        val usuarioId =
            usuarioAtualId()

        if (
            usuarioContatoRepository
                .countByUsuarioIdAndContatoId(
                    usuarioId,
                    contatoId
                ) == 0L
        ) {
            throw AccessDeniedException(
                "Contato não pertence ao usuário autenticado."
            )
        }
    }

    fun exigirAlerta(
        alertaId: Long
    ) {

        val alerta =
            alertaRepository
                .findById(
                    alertaId
                )
                .orElseThrow {
                    RuntimeException(
                        "Alerta não encontrado."
                    )
                }

        if (
            alerta.usuario.id !=
            usuarioAtualId()
        ) {
            throw AccessDeniedException(
                "Alerta não pertence ao usuário autenticado."
            )
        }
    }
}