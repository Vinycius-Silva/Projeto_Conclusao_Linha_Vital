package com.linhavital.backend.service

import com.linhavital.backend.model.Alerta
import com.linhavital.backend.repository.AlertaRepository
import com.linhavital.backend.repository.UsuarioRepository
import org.springframework.stereotype.Service

@Service
class AlertaService(
    val alertaRepository: AlertaRepository,
    val usuarioRepository: UsuarioRepository,
    val notificacaoService: NotificacaoService
) {

    fun criarAlertaPanico(usuarioId: Long): Alerta {
        val usuario = usuarioRepository.findById(usuarioId).orElseThrow {
            RuntimeException("Usuário não encontrado")
        }

        val alerta = Alerta(
            tipo = "PANICO",
            status = "ATIVO",
            usuario = usuario
        )

        val alertaSalvo = alertaRepository.save(alerta)

        usuario.fcmToken?.let {
            notificacaoService.enviarNotificacao(
                it,
                "EMERGÊNCIA",
                "Botão de pânico acionado!"
            )
        }

        return alertaSalvo
    }

    fun criarAlertaInatividade(usuarioId: Long): Alerta {
        val usuario = usuarioRepository.findById(usuarioId).orElseThrow {
            RuntimeException("Usuário não encontrado")
        }

        val alerta = Alerta(
            tipo = "INATIVIDADE",
            status = "ATIVO",
            usuario = usuario
        )

        val alertaSalvo = alertaRepository.save(alerta)

        usuario.fcmToken?.let {
            notificacaoService.enviarNotificacao(
                it,
                "⚠️ Inatividade detectada",
                "Usuário sem atividade!"
            )
        }

        return alertaSalvo
    }
}