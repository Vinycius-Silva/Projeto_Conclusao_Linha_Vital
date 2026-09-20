package com.linhavital.backend.service

import com.linhavital.backend.model.Localizacao
import com.linhavital.backend.repository.LocalizacaoRepository
import com.linhavital.backend.repository.UsuarioContatoRepository
import com.linhavital.backend.repository.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class EmailInatividadeService(
    private val javaMailSender: JavaMailSender,
    private val usuarioRepository: UsuarioRepository,
    private val usuarioContatoRepository: UsuarioContatoRepository,
    private val localizacaoRepository: LocalizacaoRepository,

    @Value("\${LINHA_VITAL_EMAIL_REMETENTE}")
    private val emailRemetente: String
) {

    private val logger =
        LoggerFactory.getLogger(EmailInatividadeService::class.java)

    private val formatoData =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

    fun enviarEmailTeste(
        destinatario: String
    ) {

        require(destinatario.isNotBlank()) {
            "O e-mail de destino não pode estar vazio."
        }

        val mensagem =
            javaMailSender.createMimeMessage()

        val helper =
            MimeMessageHelper(
                mensagem,
                true,
                "UTF-8"
            )

        helper.setFrom(
            emailRemetente,
            "Linha Vital"
        )

        helper.setTo(destinatario)

        helper.setSubject(
            "Linha Vital - Teste de envio de e-mail"
        )

        helper.setText(
            """
            Olá!

            Este é um e-mail de teste enviado pelo backend do Linha Vital.

            Se você recebeu esta mensagem, a integração SMTP está funcionando corretamente.

            Linha Vital
            """.trimIndent(),
            """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
            </head>
            <body>
                <h2>Linha Vital</h2>

                <p>
                    Este é um <strong>e-mail de teste</strong>
                    enviado pelo backend do Linha Vital.
                </p>

                <p>
                    Se você recebeu esta mensagem,
                    a integração SMTP está funcionando corretamente.
                </p>

                <hr>

                <small>
                    Linha Vital
                </small>
            </body>
            </html>
            """.trimIndent()
        )

        javaMailSender.send(mensagem)

        logger.info(
            "E-mail de teste enviado com sucesso para {}",
            destinatario
        )
    }

    fun enviarEmailsInatividade(
        usuarioId: Long,
        dataUltimoMonitoramento: LocalDateTime
    ): ResultadoEnvioEmail {

        val usuario =
            usuarioRepository
                .findById(usuarioId)
                .orElseThrow {
                    IllegalArgumentException(
                        "Usuário com ID $usuarioId não encontrado."
                    )
                }

        val contatos =
            usuarioContatoRepository
                .findContatosByUsuarioId(usuarioId)

        if (contatos.isEmpty()) {

            logger.warn(
                "Usuário {} não possui contatos cadastrados.",
                usuarioId
            )

            return ResultadoEnvioEmail(
                totalContatos = 0,
                enviados = 0,
                ignorados = 0,
                erros = 0
            )
        }

        val ultimaLocalizacao =
            localizacaoRepository
                .findFirstByUsuarioIdOrderByDataHoraDesc(
                    usuarioId
                )

        var enviados = 0
        var ignorados = 0
        var erros = 0

        contatos.forEach { contato ->

            val email =
                contato.email.trim()

            if (email.isBlank()) {

                ignorados++

                logger.warn(
                    "Contato {} do usuário {} não possui e-mail. Ignorando.",
                    contato.id,
                    usuarioId
                )

                return@forEach
            }

            try {

                enviarEmailInatividade(
                    destinatario = email,
                    nomeContato = contato.nome,
                    nomeUsuario = usuario.nome,
                    dataUltimoMonitoramento = dataUltimoMonitoramento,
                    ultimaLocalizacao = ultimaLocalizacao
                )

                enviados++

                logger.info(
                    "E-mail de inatividade enviado com sucesso. " +
                            "usuarioId={}, contatoId={}, email={}",
                    usuarioId,
                    contato.id,
                    email
                )

            } catch (exception: Exception) {

                erros++

                logger.error(
                    "Erro ao enviar e-mail de inatividade. " +
                            "usuarioId={}, contatoId={}, email={}",
                    usuarioId,
                    contato.id,
                    email,
                    exception
                )
            }
        }

        return ResultadoEnvioEmail(
            totalContatos = contatos.size,
            enviados = enviados,
            ignorados = ignorados,
            erros = erros
        )
    }

    private fun enviarEmailInatividade(
        destinatario: String,
        nomeContato: String,
        nomeUsuario: String,
        dataUltimoMonitoramento: LocalDateTime,
        ultimaLocalizacao: Localizacao?
    ) {

        val mensagem =
            javaMailSender.createMimeMessage()

        val helper =
            MimeMessageHelper(
                mensagem,
                true,
                "UTF-8"
            )

        helper.setFrom(
            emailRemetente,
            "Linha Vital"
        )

        helper.setTo(destinatario)

        helper.setSubject(
            "Linha Vital - Alerta de inatividade"
        )

        helper.setText(
            criarCorpoTexto(
                nomeContato = nomeContato,
                nomeUsuario = nomeUsuario,
                dataUltimoMonitoramento = dataUltimoMonitoramento,
                ultimaLocalizacao = ultimaLocalizacao
            ),
            criarCorpoHtml(
                nomeContato = nomeContato,
                nomeUsuario = nomeUsuario,
                dataUltimoMonitoramento = dataUltimoMonitoramento,
                ultimaLocalizacao = ultimaLocalizacao
            )
        )

        javaMailSender.send(mensagem)
    }

    private fun criarCorpoTexto(
        nomeContato: String,
        nomeUsuario: String,
        dataUltimoMonitoramento: LocalDateTime,
        ultimaLocalizacao: Localizacao?
    ): String {

        val localizacao =
            if (ultimaLocalizacao != null) {

                """
                Latitude: ${ultimaLocalizacao.latitude}
                Longitude: ${ultimaLocalizacao.longitude}
                Registrada em: ${ultimaLocalizacao.dataHora.format(formatoData)}
                Google Maps: https://www.google.com/maps?q=${ultimaLocalizacao.latitude},${ultimaLocalizacao.longitude}
                """.trimIndent()

            } else {

                "Nenhuma localização conhecida foi registrada."
            }

        return """
            Olá, $nomeContato.

            O Linha Vital identificou uma possível situação de inatividade.

            Usuário monitorado:
            $nomeUsuario

            Data do último monitoramento:
            ${dataUltimoMonitoramento.format(formatoData)}

            Última localização conhecida:
            $localizacao

            Por favor, tente entrar em contato com o usuário para verificar se está tudo bem.

            Esta é uma mensagem automática do sistema Linha Vital.
        """.trimIndent()
    }

    private fun criarCorpoHtml(
        nomeContato: String,
        nomeUsuario: String,
        dataUltimoMonitoramento: LocalDateTime,
        ultimaLocalizacao: Localizacao?
    ): String {

        val localizacao =
            if (ultimaLocalizacao != null) {

                val latitude =
                    ultimaLocalizacao.latitude

                val longitude =
                    ultimaLocalizacao.longitude

                """
                <p>
                    Latitude: $latitude<br>
                    Longitude: $longitude<br>
                    Registrada em:
                    ${ultimaLocalizacao.dataHora.format(formatoData)}
                </p>

                <p>
                    <a href="https://www.google.com/maps?q=$latitude,$longitude">
                        Abrir localização no Google Maps
                    </a>
                </p>
                """.trimIndent()

            } else {

                """
                <p>
                    Nenhuma localização conhecida foi registrada.
                </p>
                """.trimIndent()
            }

        return """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
            </head>
            <body>

                <h2>
                    Linha Vital - Alerta de inatividade
                </h2>

                <p>
                    Olá,
                    <strong>${escapeHtml(nomeContato)}</strong>.
                </p>

                <p>
                    O Linha Vital identificou uma possível
                    <strong>situação de inatividade</strong>
                    do usuário monitorado.
                </p>

                <p>
                    <strong>Usuário monitorado:</strong><br>
                    ${escapeHtml(nomeUsuario)}
                </p>

                <p>
                    <strong>Data do último monitoramento:</strong><br>
                    ${dataUltimoMonitoramento.format(formatoData)}
                </p>

                <p>
                    <strong>Última localização conhecida:</strong>
                </p>

                $localizacao

                <p>
                    Por favor, tente entrar em contato com o usuário
                    para verificar se está tudo bem.
                </p>

                <hr>

                <p>
                    <small>
                        Esta é uma mensagem automática do sistema Linha Vital.
                    </small>
                </p>

            </body>
            </html>
        """.trimIndent()
    }

    private fun escapeHtml(
        texto: String
    ): String {

        return texto
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}

data class ResultadoEnvioEmail(
    val totalContatos: Int,
    val enviados: Int,
    val ignorados: Int,
    val erros: Int
)