package com.linhavital.backend.service

import com.linhavital.backend.dto.MonitoramentoConfiguracaoRequest
import com.linhavital.backend.dto.MonitoramentoStatusResponse
import com.linhavital.backend.model.ConfiguracaoMonitoramento
import com.linhavital.backend.model.Monitoramento
import com.linhavital.backend.repository.ConfiguracaoMonitoramentoRepository
import com.linhavital.backend.repository.MonitoramentoRepository
import com.linhavital.backend.repository.UsuarioContatoRepository
import com.linhavital.backend.repository.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

@Service
class MonitoramentoService(

    private val monitoramentoRepository:
    MonitoramentoRepository,

    private val configuracaoRepository:
    ConfiguracaoMonitoramentoRepository,

    private val usuarioRepository:
    UsuarioRepository,

    private val usuarioContatoRepository:
    UsuarioContatoRepository,

    private val alertaService:
    AlertaService,

    private val emailInatividadeService:
    EmailInatividadeService,

    @Value(
        "\${INATIVIDADE_TOLERANCIA_MINUTOS:5}"
    )
    private val toleranciaInatividadeMinutos:
    Long
) {

    private val logger =
        LoggerFactory.getLogger(
            MonitoramentoService::class.java
        )

    /*
     * ===================================================
     * STATUS
     * ===================================================
     */

    @Transactional
    fun obterStatus(
        usuarioId: Long
    ): MonitoramentoStatusResponse {

        val configuracao =
            obterOuCriar(
                usuarioId
            )

        return toStatus(
            configuracao
        )
    }

    /*
     * ===================================================
     * CONFIGURAÇÃO
     * ===================================================
     */

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
            obterOuCriar(
                usuarioId
            )

        /*
         * O monitoramento só pode ser ativado
         * quando existir pelo menos um contato
         * de emergência.
         *
         * Isso corrige o cenário do CT03.
         */
        if (
            request.ativo
        ) {

            val contatos =
                usuarioContatoRepository
                    .findContatosByUsuarioId(
                        usuarioId
                    )

            require(
                contatos.isNotEmpty()
            ) {
                "Cadastre pelo menos um contato de emergência antes de ativar o monitoramento"
            }
        }

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

        /*
         * Ao alterar/reiniciar o ciclo,
         * alertas antigos de inatividade
         * deixam de representar o estado atual.
         */
        alertaService
            .resolverAlertasInatividade(
                usuarioId
            )

        registrarEvento(
            usuarioId =
                usuarioId,

            status =
                if (
                    request.ativo
                ) {

                    "ATIVO"

                } else {

                    "PAUSADO"
                }
        )

        return toStatus(
            configuracao
        )
    }

    /*
     * ===================================================
     * CHECK-IN
     * ===================================================
     */

    @Transactional
    fun registrarCheckIn(
        usuarioId: Long
    ): MonitoramentoStatusResponse {

        val configuracao =
            obterOuCriar(
                usuarioId
            )

        /*
         * O check-in reinicia completamente
         * o prazo.
         *
         * Se o usuário responder durante
         * a tolerância, o e-mail nunca será enviado.
         */
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
            usuarioId =
                usuarioId,

            status =
                "CHECK_IN"
        )

        return toStatus(
            configuracao
        )
    }

    /*
     * Atualmente atividade possui o mesmo
     * significado de confirmação de bem-estar.
     */
    fun registrarAtividade(
        usuarioId: Long
    ): MonitoramentoStatusResponse =
        registrarCheckIn(
            usuarioId
        )

    /*
     * ===================================================
     * VERIFICAÇÃO DE INATIVIDADE
     * ===================================================
     */

    @Scheduled(
        fixedDelayString =
            "\${MONITORAMENTO_SCHEDULER_MS:5000}"
    )
    @Transactional
    fun verificarInatividade() {

        val agora =
            LocalDateTime.now()

        val tolerancia =
            toleranciaInatividadeMinutos
                .coerceAtLeast(
                    0
                )

        configuracaoRepository
            .findAll()
            .asSequence()
            .filter {

                it.ativo &&
                        !it.alertaInatividadeAberto
            }
            .filter { configuracao ->

                /*
                 * Momento no qual o check-in
                 * passa a aparecer como pendente.
                 */
                val limiteCheckIn =
                    configuracao
                        .ultimaConfirmacao
                        .plusMinutes(
                            configuracao
                                .intervaloMinutos
                                .toLong()
                        )

                /*
                 * O contato só é acionado depois
                 * da tolerância.
                 *
                 * Exemplo:
                 *
                 * intervalo = 1 minuto
                 * tolerância = 1 minuto
                 *
                 * 12:01 -> check-in pendente
                 * 12:02 -> e-mail aos contatos
                 */
                val limiteEscalonamento =
                    limiteCheckIn
                        .plusMinutes(
                            tolerancia
                        )

                !agora.isBefore(
                    limiteEscalonamento
                )
            }
            .forEach { configuracao ->

                val usuarioId =
                    configuracao
                        .usuario
                        .id

                val limiteCheckIn =
                    configuracao
                        .ultimaConfirmacao
                        .plusMinutes(
                            configuracao
                                .intervaloMinutos
                                .toLong()
                        )

                val limiteEscalonamento =
                    limiteCheckIn
                        .plusMinutes(
                            tolerancia
                        )

                logger.warn(
                    "Inatividade confirmada após período de tolerância. " +
                            "usuarioId={}, ultimaConfirmacao={}, " +
                            "limiteCheckIn={}, limiteEscalonamento={}, " +
                            "toleranciaMinutos={}",
                    usuarioId,
                    configuracao.ultimaConfirmacao,
                    limiteCheckIn,
                    limiteEscalonamento,
                    tolerancia
                )

                /*
                 * Somente agora a ocorrência real
                 * de inatividade é aberta.
                 */
                val alerta =
                    alertaService
                        .criarAlertaInatividade(
                            usuarioId
                        )

                configuracao
                    .alertaInatividadeAberto =
                    true

                configuracaoRepository
                    .save(
                        configuracao
                    )

                registrarEvento(
                    usuarioId =
                        usuarioId,

                    status =
                        "CHECK_IN_PENDENTE"
                )

                /*
                 * O e-mail não bloqueia a criação
                 * do alerta caso o SMTP falhe.
                 */
                runCatching {

                    emailInatividadeService
                        .enviarEmailsInatividade(
                            usuarioId =
                                usuarioId,

                            dataUltimoMonitoramento =
                                configuracao
                                    .ultimaConfirmacao,

                            alerta =
                                alerta
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

    /*
     * ===================================================
     * CONFIGURAÇÃO DO USUÁRIO
     * ===================================================
     */

    private fun obterOuCriar(
        usuarioId: Long
    ): ConfiguracaoMonitoramento {

        configuracaoRepository
            .findByUsuarioId(
                usuarioId
            )
            ?.let {

                return it
            }

        val usuario =
            usuarioRepository
                .findById(
                    usuarioId
                )
                .orElseThrow {
                    RuntimeException(
                        "Usuário não encontrado"
                    )
                }

        return configuracaoRepository
            .save(
                ConfiguracaoMonitoramento(
                    usuario =
                        usuario
                )
            )
    }

    /*
     * ===================================================
     * HISTÓRICO DO MONITORAMENTO
     * ===================================================
     */

    private fun registrarEvento(
        usuarioId: Long,
        status: String
    ) {

        val usuario =
            usuarioRepository
                .findById(
                    usuarioId
                )
                .orElseThrow {
                    RuntimeException(
                        "Usuário não encontrado"
                    )
                }

        monitoramentoRepository
            .save(
                Monitoramento(
                    status =
                        status,

                    usuario =
                        usuario
                )
            )
    }

    /*
     * ===================================================
     * CONVERSÃO PARA STATUS
     * ===================================================
     */

    private fun toStatus(
        configuracao: ConfiguracaoMonitoramento
    ): MonitoramentoStatusResponse {

        val agora =
            LocalDateTime.now()

        /*
         * Esse prazo é propositalmente o prazo
         * do usuário, SEM a tolerância.
         *
         * Assim o aplicativo pode informar que
         * o check-in está pendente enquanto o
         * backend ainda aguarda antes de contatar
         * terceiros.
         */
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
                    !agora.isBefore(
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
                configuracao
                    .alertaInatividadeAberto
        )
    }
}