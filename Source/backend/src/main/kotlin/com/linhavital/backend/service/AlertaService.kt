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

    fun criarAlertaPanico(usuarioId: Long) {

        val usuario = usuarioRepository.findById(usuarioId).orElseThrow()

        val alerta = Alerta(
            tipo = "PANICO",
            usuario = usuario
        )

        alertaRepository.save(alerta)

        usuario.fcmToken?.let {
            notificacaoService.enviarNotificacao(
                it,
                "EMERGÊNCIA",
                "Botão de pânico acionado!"
            )
        }
    }

    fun criarAlertaInatividade(usuarioId: Long) {

        val usuario = usuarioRepository.findById(usuarioId).orElseThrow()

        val alerta = Alerta(
            tipo = "INATIVIDADE",
            usuario = usuario
        )

        alertaRepository.save(alerta)

        usuario.fcmToken?.let {
            notificacaoService.enviarNotificacao(
                it,
                "⚠️ Inatividade detectada",
                "Usuário sem atividade!"
            )
        }
    }
}