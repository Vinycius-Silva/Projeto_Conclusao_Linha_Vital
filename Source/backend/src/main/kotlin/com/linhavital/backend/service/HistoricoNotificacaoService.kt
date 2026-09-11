package com.linhavital.backend.service

import com.linhavital.backend.model.HistoricoNotificacao
import com.linhavital.backend.repository.AlertaRepository
import com.linhavital.backend.repository.ContatoEmergenciaRepository
import com.linhavital.backend.repository.HistoricoNotificacaoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class HistoricoNotificacaoService(
    private val repository: HistoricoNotificacaoRepository,
    private val alertaRepository: AlertaRepository,
    private val contatoRepository: ContatoEmergenciaRepository
) {

    companion object {
        private val STATUS_PERMITIDOS = setOf(
            "TENTATIVA",
            "NAO_ATENDIDO",
            "ATENDIDO"
        )
    }

    fun listar(): List<HistoricoNotificacao> =
        repository.findAll()

    fun salvar(notificacao: HistoricoNotificacao): HistoricoNotificacao =
        repository.save(notificacao)

    @Transactional
    fun registrarTentativa(
        alertaId: Long,
        contatoId: Long,
        statusOriginal: String
    ): HistoricoNotificacao {

        val status = statusOriginal
            .trim()
            .uppercase()

        if (status !in STATUS_PERMITIDOS) {
            throw IllegalArgumentException(
                "Status inválido. Utilize TENTATIVA, NAO_ATENDIDO ou ATENDIDO."
            )
        }

        val alerta = alertaRepository
            .findById(alertaId)
            .orElseThrow {
                IllegalArgumentException(
                    "Alerta com ID $alertaId não encontrado."
                )
            }

        val contato = contatoRepository
            .findById(contatoId)
            .orElseThrow {
                IllegalArgumentException(
                    "Contato com ID $contatoId não encontrado."
                )
            }

        val historico = HistoricoNotificacao(
            status = status,
            dataHora = LocalDateTime.now(),
            contato = contato,
            alerta = alerta
        )

        return repository.save(historico)
    }
}