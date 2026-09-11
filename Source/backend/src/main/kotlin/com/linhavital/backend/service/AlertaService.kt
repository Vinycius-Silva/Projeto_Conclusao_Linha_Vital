package com.linhavital.backend.service

import com.linhavital.backend.model.Alerta
import com.linhavital.backend.repository.AlertaRepository
import com.linhavital.backend.repository.UsuarioRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AlertaService(
    private val alertaRepository: AlertaRepository,
    private val usuarioRepository: UsuarioRepository
) {

    fun criarAlertaPanico(usuarioId: Long): Alerta = criar(usuarioId, "PANICO")

    fun criarAlertaInatividade(usuarioId: Long): Alerta = criar(usuarioId, "INATIVIDADE")

    fun listarPorUsuario(usuarioId: Long): List<Alerta> =
        alertaRepository.findByUsuarioIdOrderByDataHoraDesc(usuarioId)

    @Transactional
    fun resolverAlertasInatividade(usuarioId: Long) {
        val ativos = alertaRepository.findByUsuarioIdAndTipoAndStatus(
            usuarioId,
            "INATIVIDADE",
            "ATIVO"
        )
        ativos.forEach { it.status = "RESOLVIDO" }
        alertaRepository.saveAll(ativos)
    }

    private fun criar(usuarioId: Long, tipo: String): Alerta {
        val usuario = usuarioRepository.findById(usuarioId).orElseThrow {
            RuntimeException("Usuário não encontrado")
        }
        return alertaRepository.save(
            Alerta(
                tipo = tipo,
                status = "ATIVO",
                usuario = usuario
            )
        )
    }
}
