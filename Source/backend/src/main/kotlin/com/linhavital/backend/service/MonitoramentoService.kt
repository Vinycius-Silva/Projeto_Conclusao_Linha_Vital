package com.linhavital.backend.service

import com.linhavital.backend.dto.MonitoramentoConfiguracaoRequest
import com.linhavital.backend.dto.MonitoramentoStatusResponse
import com.linhavital.backend.model.ConfiguracaoMonitoramento
import com.linhavital.backend.model.Monitoramento
import com.linhavital.backend.repository.ConfiguracaoMonitoramentoRepository
import com.linhavital.backend.repository.MonitoramentoRepository
import com.linhavital.backend.repository.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

@Service
class MonitoramentoService(
    private val monitoramentoRepository: MonitoramentoRepository,
    private val configuracaoRepository: ConfiguracaoMonitoramentoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val alertaService: AlertaService,
    private val emailInatividadeService: EmailInatividadeService
) {

    private val logger =
        LoggerFactory.getLogger(MonitoramentoService::class.java)

    @Transactional
    fun obterStatus(
        usuarioId: Long
    ): MonitoramentoStatusResponse {

        val configuracao =
            obterOuCriar(usuarioId)

        return toStatus(configuracao)
    }

    @Transactional
    fun atualizarConfiguracao(
        usuarioId: Long,
        request: MonitoramentoConfiguracaoRequest
    ): MonitoramentoStatusResponse {

        require(
            request.intervaloMinutos in 1..240
        ) {
            "O intervalo deve estar entre 1 e 240 minutos"
        }

        val configuracao =
            obterOuCriar(usuarioId)

        configuracao.ativo =
            request.ativo

        configuracao.intervaloMinutos =
            request.intervaloMinutos

        configuracao.ultimaConfirmacao =
            LocalDateTime.now()

        configuracao.alertaInatividadeAberto =
            false

        configuracaoRepository.save(
            configuracao
        )

        alertaService
            .resolverAlertasInatividade(
                usuarioId
            )

        registrarEvento(
            usuarioId,
            if (request.ativo) {
                "ATIVO"
            } else {
                "PAUSADO"
            }
        )

        return toStatus(
            configuracao
        )
    }

    @Transactional
    fun registrarCheckIn(
        usuarioId: Long
    ): MonitoramentoStatusResponse {

        val configuracao =
            obterOuCriar(usuarioId)

        configuracao.ultimaConfirmacao =
            LocalDateTime.now()

        configuracao.alertaInatividadeAberto =
            false

        configuracaoRepository.save(
            configuracao
        )

        alertaService
            .resolverAlertasInatividade(
                usuarioId
            )

        registrarEvento(
            usuarioId,
            "CHECK_IN"
        )

        return toStatus(
            configuracao
        )
    }

    fun registrarAtividade(
        usuarioId: Long
    ): MonitoramentoStatusResponse =
        registrarCheckIn(usuarioId)

    @Scheduled(
        fixedDelay = 60_000
    )
    @Transactional
    fun verificarInatividade() {

        val agora =
            LocalDateTime.now()

        configuracaoRepository
            .findAll()
            .asSequence()
            .filter {
                it.ativo &&
                        !it.alertaInatividadeAberto
            }
            .filter {
                agora.isAfter(
                    it.ultimaConfirmacao
                        .plusMinutes(
                            it.intervaloMinutos.toLong()
                        )
                )
            }
            .forEach { configuracao ->

                val usuarioId =
                    configuracao.usuario.id

                logger.warn(
                    "Inatividade detectada para usuário {}. " +
                            "Última confirmação: {}",
                    usuarioId,
                    configuracao.ultimaConfirmacao
                )

                alertaService
                    .criarAlertaInatividade(
                        usuarioId
                    )

                configuracao.alertaInatividadeAberto =
                    true

                configuracaoRepository.save(
                    configuracao
                )

                registrarEvento(
                    usuarioId,
                    "CHECK_IN_PENDENTE"
                )

                runCatching {

                    emailInatividadeService
                        .enviarEmailsInatividade(
                            usuarioId =
                                usuarioId,

                            dataUltimoMonitoramento =
                                configuracao.ultimaConfirmacao
                        )

                }.onSuccess { resultado ->

                    logger.info(
                        "Processamento de e-mails concluído. " +
                                "usuarioId={}, contatos={}, enviados={}, " +
                                "ignorados={}, erros={}",
                        usuarioId,
                        resultado.totalContatos,
                        resultado.enviados,
                        resultado.ignorados,
                        resultado.erros
                    )

                }.onFailure { erro ->

                    logger.error(
                        "Falha inesperada durante envio dos " +
                                "e-mails de inatividade do usuário {}",
                        usuarioId,
                        erro
                    )
                }
            }
    }

    private fun obterOuCriar(
        usuarioId: Long
    ): ConfiguracaoMonitoramento {

        configuracaoRepository
            .findByUsuarioId(usuarioId)
            ?.let {
                return it
            }

        val usuario =
            usuarioRepository
                .findById(usuarioId)
                .orElseThrow {
                    RuntimeException(
                        "Usuário não encontrado"
                    )
                }

        return configuracaoRepository
            .save(
                ConfiguracaoMonitoramento(
                    usuario = usuario
                )
            )
    }

    private fun registrarEvento(
        usuarioId: Long,
        status: String
    ) {

        val usuario =
            usuarioRepository
                .findById(usuarioId)
                .orElseThrow {
                    RuntimeException(
                        "Usuário não encontrado"
                    )
                }

        monitoramentoRepository
            .save(
                Monitoramento(
                    status = status,
                    usuario = usuario
                )
            )
    }

    private fun toStatus(
        configuracao: ConfiguracaoMonitoramento
    ): MonitoramentoStatusResponse {

        val agora =
            LocalDateTime.now()

        val proximo =
            configuracao
                .ultimaConfirmacao
                .plusMinutes(
                    configuracao
                        .intervaloMinutos
                        .toLong()
                )

        val pendente =
            configuracao.ativo &&
                    agora.isAfter(
                        proximo
                    )

        val restantes =
            if (
                !configuracao.ativo ||
                pendente
            ) {

                0L

            } else {

                Duration
                    .between(
                        agora,
                        proximo
                    )
                    .toMinutes()
                    .coerceAtLeast(
                        0
                    )
            }

        return MonitoramentoStatusResponse(
            ativo =
                configuracao.ativo,

            intervaloMinutos =
                configuracao.intervaloMinutos,

            ultimaConfirmacao =
                configuracao.ultimaConfirmacao,

            proximoCheckIn =
                proximo,

            minutosRestantes =
                restantes,

            checkInPendente =
                pendente,

            alertaInatividadeAberto =
                configuracao.alertaInatividadeAberto
        )
    }
}