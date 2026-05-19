package com.linhavital.backend.service

import com.linhavital.backend.model.Monitoramento
import com.linhavital.backend.repository.MonitoramentoRepository
import com.linhavital.backend.repository.UsuarioRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime

@Service
class MonitoramentoService(
    val monitoramentoRepository: MonitoramentoRepository,
    val usuarioRepository: UsuarioRepository,
    val alertaService: AlertaService
) {

    fun registrarAtividade(usuarioId: Long) {

        val usuario = usuarioRepository.findById(usuarioId).orElseThrow()

        val monitoramento = Monitoramento(
            status = "ATIVO",
            usuario = usuario
        )

        monitoramentoRepository.save(monitoramento)
    }

    @Scheduled(fixedRate = 60000) // a cada 1 minuto
    fun verificarInatividade() {

        val usuarios = usuarioRepository.findAll()
        val agora = LocalDateTime.now()

        usuarios.forEach { usuario ->

            val ultimo = monitoramentoRepository
                .findTopByUsuarioIdOrderByDataHoraDesc(usuario.id!!)

            if (ultimo != null) {

                val minutos = Duration.between(ultimo.dataHora, agora).toMinutes()

                if (minutos > 5) {
                    alertaService.criarAlertaInatividade(usuario.id!!)
                }
            }
        }
    }
}