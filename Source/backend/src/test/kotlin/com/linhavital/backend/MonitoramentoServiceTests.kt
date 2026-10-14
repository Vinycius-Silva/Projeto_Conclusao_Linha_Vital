package com.linhavital.backend

import com.linhavital.backend.dto.MonitoramentoConfiguracaoRequest
import com.linhavital.backend.model.Alerta
import com.linhavital.backend.model.ConfiguracaoMonitoramento
import com.linhavital.backend.model.ContatoEmergencia
import com.linhavital.backend.model.Usuario
import com.linhavital.backend.repository.ConfiguracaoMonitoramentoRepository
import com.linhavital.backend.repository.MonitoramentoRepository
import com.linhavital.backend.repository.UsuarioContatoRepository
import com.linhavital.backend.repository.UsuarioRepository
import com.linhavital.backend.service.AlertaService
import com.linhavital.backend.service.EmailInatividadeService
import com.linhavital.backend.service.MonitoramentoService
import com.linhavital.backend.service.ResultadoEnvioEmail
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

class MonitoramentoServiceTests {

    private val monitoramentoRepository =
        Mockito.mock(
            MonitoramentoRepository::class.java
        )

    private val configuracaoRepository =
        Mockito.mock(
            ConfiguracaoMonitoramentoRepository::class.java
        )

    private val usuarioRepository =
        Mockito.mock(
            UsuarioRepository::class.java
        )

    private val usuarioContatoRepository =
        Mockito.mock(
            UsuarioContatoRepository::class.java
        )

    private val alertaService =
        Mockito.mock(
            AlertaService::class.java
        )

    private val emailInatividadeService =
        Mockito.mock(
            EmailInatividadeService::class.java
        )

    private val usuario =
        Usuario(
            id =
                1L,

            nome =
                "Usuário Teste",

            email =
                "usuario@teste.com",

            telefone =
                "11999999999",

            dataNascimento =
                LocalDate.of(
                    2000,
                    1,
                    1
                ),

            senha =
                "senha-hash"
        )

    /*
     * Para os testes abaixo usamos uma tolerância
     * de 5 minutos.
     */
    private fun criarService(
        toleranciaMinutos: Long = 5L
    ): MonitoramentoService =
        MonitoramentoService(
            monitoramentoRepository =
                monitoramentoRepository,

            configuracaoRepository =
                configuracaoRepository,

            usuarioRepository =
                usuarioRepository,

            usuarioContatoRepository =
                usuarioContatoRepository,

            alertaService =
                alertaService,

            emailInatividadeService =
                emailInatividadeService,

            toleranciaInatividadeMinutos =
                toleranciaMinutos
        )

    /*
     * ===================================================
     * ATIVAÇÃO SEM CONTATO
     * ===================================================
     */

    @Test
    fun `monitoramento nao deve ser ativado sem contato de emergencia`() {

        val configuracao =
            ConfiguracaoMonitoramento(
                id =
                    1L,

                ativo =
                    false,

                intervaloMinutos =
                    35,

                ultimaConfirmacao =
                    LocalDateTime.now(),

                alertaInatividadeAberto =
                    false,

                usuario =
                    usuario
            )

        Mockito
            .`when`(
                configuracaoRepository
                    .findByUsuarioId(
                        1L
                    )
            )
            .thenReturn(
                configuracao
            )

        Mockito
            .`when`(
                usuarioContatoRepository
                    .findContatosByUsuarioId(
                        1L
                    )
            )
            .thenReturn(
                emptyList()
            )

        val service =
            criarService()

        val erro =
            assertThrows(
                IllegalArgumentException::class.java
            ) {

                service
                    .atualizarConfiguracao(
                        usuarioId =
                            1L,

                        request =
                            MonitoramentoConfiguracaoRequest(
                                ativo =
                                    true,

                                intervaloMinutos =
                                    1
                            )
                    )
            }

        assertTrue(
            erro.message
                ?.contains(
                    "contato de emergência",
                    ignoreCase = true
                ) == true
        )

        /*
         * Não deve criar alerta nem enviar e-mail.
         */
        Mockito.verify(
            alertaService,
            Mockito.never()
        ).criarAlertaInatividade(
            1L
        )

        Mockito.verifyNoInteractions(
            emailInatividadeService
        )
    }

    /*
     * ===================================================
     * CHECK-IN PENDENTE
     * ===================================================
     */

    @Test
    fun `status deve indicar check in pendente quando prazo principal terminar`() {

        val configuracao =
            ConfiguracaoMonitoramento(
                id =
                    1L,

                ativo =
                    true,

                intervaloMinutos =
                    1,

                /*
                 * Já se passaram 2 minutos.
                 *
                 * O prazo de check-in era 1 minuto,
                 * então o usuário já está atrasado.
                 *
                 * Porém ainda estamos dentro da
                 * tolerância de 5 minutos.
                 */
                ultimaConfirmacao =
                    LocalDateTime
                        .now()
                        .minusMinutes(
                            2
                        ),

                alertaInatividadeAberto =
                    false,

                usuario =
                    usuario
            )

        Mockito
            .`when`(
                configuracaoRepository
                    .findByUsuarioId(
                        1L
                    )
            )
            .thenReturn(
                configuracao
            )

        val service =
            criarService(
                toleranciaMinutos =
                    5L
            )

        val status =
            service.obterStatus(
                1L
            )

        assertTrue(
            status.ativo
        )

        assertTrue(
            status.checkInPendente
        )

        /*
         * O alerta grave ainda não foi aberto,
         * pois a tolerância não terminou.
         */
        assertFalse(
            status.alertaInatividadeAberto
        )
    }

    /*
     * ===================================================
     * DENTRO DA TOLERÂNCIA
     * ===================================================
     */

    @Test
    fun `nao deve criar alerta nem enviar email durante tolerancia`() {

        val configuracao =
            ConfiguracaoMonitoramento(
                id =
                    1L,

                ativo =
                    true,

                intervaloMinutos =
                    1,

                /*
                 * 2 minutos desde o último check-in:
                 *
                 * 1 minuto de prazo
                 * +
                 * 1 minuto de atraso.
                 *
                 * Como a tolerância é de 5 minutos,
                 * ainda não deve escalar.
                 */
                ultimaConfirmacao =
                    LocalDateTime
                        .now()
                        .minusMinutes(
                            2
                        ),

                alertaInatividadeAberto =
                    false,

                usuario =
                    usuario
            )

        Mockito
            .`when`(
                configuracaoRepository
                    .findAll()
            )
            .thenReturn(
                listOf(
                    configuracao
                )
            )

        val service =
            criarService(
                toleranciaMinutos =
                    5L
            )

        service.verificarInatividade()

        Mockito.verify(
            alertaService,
            Mockito.never()
        ).criarAlertaInatividade(
            1L
        )

        Mockito.verifyNoInteractions(
            emailInatividadeService
        )

        assertFalse(
            configuracao
                .alertaInatividadeAberto
        )
    }

    /*
     * ===================================================
     * APÓS A TOLERÂNCIA
     * ===================================================
     */

    @Test
    fun `deve criar alerta e enviar email depois da tolerancia`() {

        val configuracao =
            ConfiguracaoMonitoramento(
                id =
                    1L,

                ativo =
                    true,

                intervaloMinutos =
                    1,

                /*
                 * 7 minutos desde o último check-in.
                 *
                 * Prazo normal:
                 * 1 minuto.
                 *
                 * Tolerância:
                 * 5 minutos.
                 *
                 * Escalonamento:
                 * após 6 minutos.
                 *
                 * Portanto 7 minutos é suficiente.
                 */
                ultimaConfirmacao =
                    LocalDateTime
                        .now()
                        .minusMinutes(
                            7
                        ),

                alertaInatividadeAberto =
                    false,

                usuario =
                    usuario
            )

        val alerta =
            Alerta(
                id =
                    10L,

                tipo =
                    "INATIVIDADE",

                dataHora =
                    LocalDateTime.now(),

                status =
                    "ATIVO",

                usuario =
                    usuario
            )

        Mockito
            .`when`(
                configuracaoRepository
                    .findAll()
            )
            .thenReturn(
                listOf(
                    configuracao
                )
            )

        Mockito
            .`when`(
                usuarioRepository
                    .findById(
                        1L
                    )
            )
            .thenReturn(
                Optional.of(
                    usuario
                )
            )

        Mockito
            .`when`(
                alertaService
                    .criarAlertaInatividade(
                        1L
                    )
            )
            .thenReturn(
                alerta
            )

        Mockito
            .`when`(
                emailInatividadeService
                    .enviarEmailsInatividade(
                        usuarioId =
                            1L,

                        dataUltimoMonitoramento =
                            configuracao
                                .ultimaConfirmacao,

                        alerta =
                            alerta
                    )
            )
            .thenReturn(
                ResultadoEnvioEmail(
                    totalContatos =
                        1,

                    enviados =
                        1,

                    ignorados =
                        0,

                    erros =
                        0
                )
            )

        val service =
            criarService(
                toleranciaMinutos =
                    5L
            )

        service.verificarInatividade()

        Mockito.verify(
            alertaService,
            Mockito.times(
                1
            )
        ).criarAlertaInatividade(
            1L
        )

        Mockito.verify(
            emailInatividadeService,
            Mockito.times(
                1
            )
        ).enviarEmailsInatividade(
            usuarioId =
                1L,

            dataUltimoMonitoramento =
                configuracao
                    .ultimaConfirmacao,

            alerta =
                alerta
        )

        assertTrue(
            configuracao
                .alertaInatividadeAberto
        )
    }

    /*
     * ===================================================
     * CHECK-IN DURANTE A TOLERÂNCIA
     * ===================================================
     */

    @Test
    fun `check in durante tolerancia deve impedir escalonamento`() {

        val configuracao =
            ConfiguracaoMonitoramento(
                id =
                    1L,

                ativo =
                    true,

                intervaloMinutos =
                    1,

                /*
                 * Já está atrasado, porém ainda
                 * dentro da tolerância.
                 */
                ultimaConfirmacao =
                    LocalDateTime
                        .now()
                        .minusMinutes(
                            2
                        ),

                alertaInatividadeAberto =
                    false,

                usuario =
                    usuario
            )

        Mockito
            .`when`(
                configuracaoRepository
                    .findByUsuarioId(
                        1L
                    )
            )
            .thenReturn(
                configuracao
            )

        Mockito
            .`when`(
                usuarioRepository
                    .findById(
                        1L
                    )
            )
            .thenReturn(
                Optional.of(
                    usuario
                )
            )

        val service =
            criarService(
                toleranciaMinutos =
                    5L
            )

        /*
         * Usuário confirma que está bem
         * antes da tolerância acabar.
         */
        service.registrarCheckIn(
            1L
        )

        /*
         * O mesmo objeto agora possui uma
         * ultimaConfirmacao atualizada.
         */
        Mockito
            .`when`(
                configuracaoRepository
                    .findAll()
            )
            .thenReturn(
                listOf(
                    configuracao
                )
            )

        service.verificarInatividade()

        Mockito.verify(
            alertaService,
            Mockito.never()
        ).criarAlertaInatividade(
            1L
        )

        Mockito.verifyNoInteractions(
            emailInatividadeService
        )

        assertFalse(
            configuracao
                .alertaInatividadeAberto
        )
    }

    /*
     * ===================================================
     * MONITORAMENTO PAUSADO
     * ===================================================
     */

    @Test
    fun `monitoramento pausado nunca deve gerar alerta de inatividade`() {

        val configuracao =
            ConfiguracaoMonitoramento(
                id =
                    1L,

                ativo =
                    false,

                intervaloMinutos =
                    1,

                ultimaConfirmacao =
                    LocalDateTime
                        .now()
                        .minusHours(
                            10
                        ),

                alertaInatividadeAberto =
                    false,

                usuario =
                    usuario
            )

        Mockito
            .`when`(
                configuracaoRepository
                    .findAll()
            )
            .thenReturn(
                listOf(
                    configuracao
                )
            )

        val service =
            criarService(
                toleranciaMinutos =
                    0L
            )

        service.verificarInatividade()

        Mockito.verify(
            alertaService,
            Mockito.never()
        ).criarAlertaInatividade(
            1L
        )

        Mockito.verifyNoInteractions(
            emailInatividadeService
        )
    }

    /*
     * ===================================================
     * ALERTA JÁ ABERTO
     * ===================================================
     */

    @Test
    fun `nao deve gerar segundo alerta quando ja existe alerta aberto`() {

        val configuracao =
            ConfiguracaoMonitoramento(
                id =
                    1L,

                ativo =
                    true,

                intervaloMinutos =
                    1,

                ultimaConfirmacao =
                    LocalDateTime
                        .now()
                        .minusHours(
                            10
                        ),

                alertaInatividadeAberto =
                    true,

                usuario =
                    usuario
            )

        Mockito
            .`when`(
                configuracaoRepository
                    .findAll()
            )
            .thenReturn(
                listOf(
                    configuracao
                )
            )

        val service =
            criarService(
                toleranciaMinutos =
                    0L
            )

        service.verificarInatividade()

        Mockito.verify(
            alertaService,
            Mockito.never()
        ).criarAlertaInatividade(
            1L
        )

        Mockito.verifyNoInteractions(
            emailInatividadeService
        )
    }

    /*
     * ===================================================
     * USUÁRIO COM CONTATO
     * ===================================================
     */

    @Test
    fun `monitoramento deve poder ser ativado quando usuario possui contato`() {

        val configuracao =
            ConfiguracaoMonitoramento(
                id =
                    1L,

                ativo =
                    false,

                intervaloMinutos =
                    35,

                ultimaConfirmacao =
                    LocalDateTime.now(),

                alertaInatividadeAberto =
                    false,

                usuario =
                    usuario
            )

        val contato =
            ContatoEmergencia(
                id =
                    1L,

                nome =
                    "Contato Teste",

                telefone =
                    "11988888888",

                email =
                    "contato@teste.com",

                tipoContato =
                    "FAMILIAR"
            )

        Mockito
            .`when`(
                configuracaoRepository
                    .findByUsuarioId(
                        1L
                    )
            )
            .thenReturn(
                configuracao
            )

        Mockito
            .`when`(
                usuarioContatoRepository
                    .findContatosByUsuarioId(
                        1L
                    )
            )
            .thenReturn(
                listOf(
                    contato
                )
            )

        Mockito
            .`when`(
                usuarioRepository
                    .findById(
                        1L
                    )
            )
            .thenReturn(
                Optional.of(
                    usuario
                )
            )

        val service =
            criarService()

        val status =
            service
                .atualizarConfiguracao(
                    usuarioId =
                        1L,

                    request =
                        MonitoramentoConfiguracaoRequest(
                            ativo =
                                true,

                            intervaloMinutos =
                                1
                        )
                )

        assertTrue(
            status.ativo
        )

        assertFalse(
            status.checkInPendente
        )
    }
}