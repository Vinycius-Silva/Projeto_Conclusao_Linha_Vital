package com.linhavital.app.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.linhavital.app.R
import com.linhavital.app.data.model.ContatoEmergencia
import com.linhavital.app.data.model.MonitoramentoStatus
import com.linhavital.app.data.repository.AlertaRepository
import com.linhavital.app.data.repository.ContatoRepository
import com.linhavital.app.data.repository.HistoricoNotificacaoRepository
import com.linhavital.app.data.repository.MonitoramentoRepository
import com.linhavital.app.databinding.ActivityHomeBinding
import com.linhavital.app.monitoring.CheckInScheduler
import com.linhavital.app.ui.auth.LoginActivity
import com.linhavital.app.utils.SessionManager
import com.linhavital.app.utils.applySystemBarsPadding
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var sessionManager: SessionManager

    private val monitoramentoRepository =
        MonitoramentoRepository()

    private val contatoRepository =
        ContatoRepository()

    private val alertaRepository =
        AlertaRepository()

    private val historicoNotificacaoRepository =
        HistoricoNotificacaoRepository()

    private var usuarioId: Long? = null

    private var sosClickCount = 0
    private var lastSosClickTime = 0L

    private var numeroPendenteLigacao: String? = null

    private var notificationPermissionAsked = false

    /*
     * ===================================================
     * CONTROLE DA CASCATA DE EMERGÊNCIA
     * ===================================================
     */

    private var contatosCascata: List<ContatoEmergencia> =
        emptyList()

    private var indiceContatoAtual = 0

    private var cascataEmAndamento = false

    private var aguardandoRetornoLigacao = false

    /*
     * ID do alerta PANICO criado pelo backend.
     *
     * Todos os registros de TENTATIVA,
     * NAO_ATENDIDO e ATENDIDO usarão esse ID.
     */
    private var alertaIdCascata: Long? = null

    companion object {

        private const val REQUEST_CALL_PHONE = 101

        private const val REQUEST_NOTIFICATIONS = 103

        const val EXTRA_OPEN_CHECK_IN = "open_check_in"
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        binding =
            ActivityHomeBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        binding.headerHome
            .applySystemBarsPadding(
                top = true
            )

        binding.bottomNavigation
            .bottomNavigationContainer
            .applySystemBarsPadding(
                bottom = true
            )

        window.statusBarColor =
            android.graphics.Color.parseColor(
                "#FFF5F5"
            )

        WindowCompat
            .getInsetsController(
                window,
                window.decorView
            )
            .isAppearanceLightStatusBars =
            true

        sessionManager =
            SessionManager(this)

        CheckInScheduler.ensureChannel(
            this
        )

        /*
         * SOS
         */

        binding.btnSOS
            .setOnClickListener {

                registrarCliqueEmergencia(
                    "Toque"
                )
            }

        /*
         * CHECK-IN
         */

        binding.btnCheckIn
            .setOnClickListener {

                confirmarCheckIn()
            }

        binding.btnConfigurarMonitoramento
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        CriterioFormActivity::class.java
                    )
                )
            }

        /*
         * CONTATOS
         */

        binding.btnVerContatos
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        ContatosActivity::class.java
                    )
                )
            }

        configurarBottomBar()

        binding.btnLogout
            .setOnClickListener {

                logout()
            }

        lifecycleScope.launch {

            usuarioId =
                sessionManager.getUserId()

            if (
                usuarioId == null ||
                usuarioId == 0L
            ) {

                irParaLogin()

                return@launch
            }

            binding.tvBemVindo.text =
                "Olá, ${
                    sessionManager.getUserName()
                        ?: "Usuário"
                }!"

            carregarDashboard()

            if (
                intent.getBooleanExtra(
                    EXTRA_OPEN_CHECK_IN,
                    false
                )
            ) {

                Toast.makeText(
                    this@HomeActivity,
                    "Seu check-in está aguardando confirmação.",
                    Toast.LENGTH_LONG
                ).show()

                intent.removeExtra(
                    EXTRA_OPEN_CHECK_IN
                )
            }
        }
    }

    /*
     * ===================================================
     * RETORNO DA TELA DE LIGAÇÃO
     * ===================================================
     *
     * O Android não permite detectar de forma confiável
     * se uma chamada convencional foi atendida.
     *
     * Quando o usuário retorna para o Linha Vital,
     * perguntamos manualmente se o contato atendeu.
     */

    override fun onResume() {

        super.onResume()

        if (usuarioId != null) {

            carregarDashboard()
        }

        if (
            cascataEmAndamento &&
            aguardandoRetornoLigacao
        ) {

            aguardandoRetornoLigacao =
                false

            binding.root.postDelayed({

                if (cascataEmAndamento) {

                    mostrarConfirmacaoAtendimento()
                }

            }, 500)
        }
    }

    /*
     * ===================================================
     * BOTTOM NAVIGATION
     * ===================================================
     */

    private fun configurarBottomBar() {

        binding.bottomNavigation
            .btnNavHome
            .setBackgroundResource(
                R.drawable.nav_item_active
            )

        binding.bottomNavigation
            .iconNavHome
            .setColorFilter(
                android.graphics.Color.parseColor(
                    "#BB0013"
                )
            )

        binding.bottomNavigation
            .labelNavHome
            .setTextColor(
                android.graphics.Color.parseColor(
                    "#BB0013"
                )
            )

        binding.bottomNavigation
            .btnNavHome
            .setOnClickListener { }

        binding.bottomNavigation
            .btnNavCriterios
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        CriteriosActivity::class.java
                    )
                )
            }

        binding.bottomNavigation
            .btnNavContatos
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        ContatosActivity::class.java
                    )
                )
            }
    }

    /*
     * ===================================================
     * DASHBOARD
     * ===================================================
     */

    private fun carregarDashboard() {

        val id =
            usuarioId ?: return

        lifecycleScope.launch {

            monitoramentoRepository
                .obterStatus(id)
                .onSuccess(
                    ::renderMonitoramento
                )
                .onFailure {

                    binding.tvMonitoramentoStatus.text =
                        "Backend indisponível"

                    binding.tvProximoCheckIn.text =
                        "Não foi possível consultar o monitoramento."

                    binding.btnCheckIn.isEnabled =
                        false
                }

            contatoRepository
                .listarContatos(id)
                .onSuccess { contatos ->

                    binding.tvContatoPrioritario.text =
                        when {

                            contatos.isEmpty() -> {

                                "Nenhum contato cadastrado"
                            }

                            else -> {

                                "Prioritário: " +
                                        "${contatos.first().nome} • " +
                                        contatos.first().tipoContato
                            }
                        }
                }
                .onFailure {

                    binding.tvContatoPrioritario.text =
                        "Não foi possível carregar seus contatos"
                }
        }
    }

    /*
     * ===================================================
     * MONITORAMENTO
     * ===================================================
     */

    private fun renderMonitoramento(
        status: MonitoramentoStatus
    ) {

        binding.btnCheckIn.isEnabled =
            status.ativo

        binding.tvMonitoramentoStatus.text =
            when {

                !status.ativo ->

                    "Monitoramento pausado"

                status.checkInPendente ||
                        status.alertaInatividadeAberto ->

                    "Check-in pendente"

                else ->

                    "Monitoramento ativo"
            }

        binding.tvProximoCheckIn.text =
            when {

                !status.ativo ->

                    "Ative os check-ins em Critérios para iniciar o ciclo preventivo."

                status.checkInPendente ||
                        status.alertaInatividadeAberto ->

                    "O prazo terminou. Confirme agora que está tudo bem para encerrar a ocorrência."

                else ->

                    "Próximo check-in em aproximadamente " +
                            "${status.minutosRestantes.coerceAtLeast(1)} min."
            }

        if (status.ativo) {

            solicitarPermissaoNotificacaoSeNecessario()

            if (
                status.checkInPendente ||
                status.alertaInatividadeAberto
            ) {

                CheckInScheduler.cancel(
                    this
                )

            } else if (
                !CheckInScheduler.isScheduled(
                    this
                )
            ) {

                CheckInScheduler.schedule(
                    this,
                    status.minutosRestantes
                        .coerceAtLeast(1)
                )
            }

        } else {

            CheckInScheduler.cancel(
                this
            )
        }
    }

    private fun confirmarCheckIn() {

        val id =
            usuarioId ?: return

        binding.btnCheckIn.isEnabled =
            false

        lifecycleScope.launch {

            monitoramentoRepository
                .checkIn(id)
                .onSuccess { status ->

                    renderMonitoramento(
                        status
                    )

                    CheckInScheduler.schedule(
                        this@HomeActivity,
                        status.intervaloMinutos
                            .toLong()
                    )

                    Toast.makeText(
                        this@HomeActivity,
                        "Check-in confirmado. Que bom saber que está tudo bem.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .onFailure {

                    binding.btnCheckIn.isEnabled =
                        true

                    Toast.makeText(
                        this@HomeActivity,
                        "Não foi possível confirmar o check-in.",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    /*
     * ===================================================
     * SOS PELO BOTÃO DE VOLUME
     * ===================================================
     */

    override fun dispatchKeyEvent(
        event: KeyEvent
    ): Boolean {

        if (
            event.action ==
            KeyEvent.ACTION_DOWN &&
            event.repeatCount == 0 &&
            event.keyCode ==
            KeyEvent.KEYCODE_VOLUME_DOWN
        ) {

            registrarCliqueEmergencia(
                "Pressione"
            )

            return true
        }

        return super.dispatchKeyEvent(
            event
        )
    }

    /*
     * ===================================================
     * CONFIRMAÇÃO DE 3 INTERAÇÕES
     * ===================================================
     */

    private fun registrarCliqueEmergencia(
        tipoAcao: String
    ) {

        val agora =
            System.currentTimeMillis()

        if (
            agora - lastSosClickTime >
            2_000
        ) {

            sosClickCount = 0
        }

        sosClickCount++

        lastSosClickTime =
            agora

        when (sosClickCount) {

            1 -> {

                Toast.makeText(
                    this,
                    "$tipoAcao mais 2x para acionar o SOS",
                    Toast.LENGTH_SHORT
                ).show()
            }

            2 -> {

                Toast.makeText(
                    this,
                    "$tipoAcao mais 1x para acionar o SOS",
                    Toast.LENGTH_SHORT
                ).show()
            }

            3 -> {

                sosClickCount = 0

                acionarEmergencia()
            }
        }
    }

    /*
     * ===================================================
     * SOS
     * ===================================================
     */

    private fun acionarEmergencia() {

        /*
         * Impede que duas cascatas sejam iniciadas
         * ao mesmo tempo.
         */

        if (cascataEmAndamento) {

            Toast.makeText(
                this,
                "Uma cascata de emergência já está em andamento.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val id =
            usuarioId ?: return

        Toast.makeText(
            this,
            "SOS acionado. Buscando seus contatos de emergência...",
            Toast.LENGTH_LONG
        ).show()

        lifecycleScope.launch {

            /*
             * Cria o alerta PANICO.
             */

            val resultadoAlerta =
                alertaRepository
                    .registrarAlertaPanico(
                        id
                    )

            resultadoAlerta
                .onSuccess { idAlerta ->

                    /*
                     * Guarda o ID do alerta que será usado
                     * em todos os eventos da cascata.
                     */

                    alertaIdCascata =
                        idAlerta
                }
                .onFailure {

                    /*
                     * Se o backend falhar, ainda tentamos
                     * realizar as ligações.
                     */

                    alertaIdCascata =
                        null

                    Toast.makeText(
                        this@HomeActivity,
                        "O SOS continuará, mas não foi possível registrar o alerta no backend.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            /*
             * Busca os contatos do usuário.
             *
             * O backend já devolve a lista
             * respeitando a prioridade.
             */

            contatoRepository
                .listarContatos(id)
                .onSuccess { contatos ->

                    if (
                        contatos.isEmpty()
                    ) {

                        Toast.makeText(
                            this@HomeActivity,
                            "Cadastre um contato para completar o fluxo de SOS.",
                            Toast.LENGTH_LONG
                        ).show()

                        alertaIdCascata =
                            null

                        startActivity(
                            Intent(
                                this@HomeActivity,
                                ContatosActivity::class.java
                            )
                        )

                        return@onSuccess
                    }

                    iniciarCascata(
                        contatos
                    )
                }
                .onFailure {

                    alertaIdCascata =
                        null

                    Toast.makeText(
                        this@HomeActivity,
                        "Não foi possível consultar seus contatos de emergência.",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    /*
     * ===================================================
     * INÍCIO DA CASCATA
     * ===================================================
     */

    private fun iniciarCascata(
        contatos: List<ContatoEmergencia>
    ) {

        contatosCascata =
            contatos

        indiceContatoAtual =
            0

        cascataEmAndamento =
            true

        aguardandoRetornoLigacao =
            false

        ligarParaContatoAtual()
    }

    /*
     * ===================================================
     * CONTATO ATUAL DA CASCATA
     * ===================================================
     */

    private fun ligarParaContatoAtual() {

        if (
            !cascataEmAndamento
        ) {

            return
        }

        val contato =
            contatosCascata
                .getOrNull(
                    indiceContatoAtual
                )

        /*
         * Não existem mais contatos.
         */

        if (
            contato == null
        ) {

            finalizarCascataSemAtendimento()

            return
        }

        val numeroFormatado =
            formatarNumero(
                contato.telefone
            )

        /*
         * Telefone inválido:
         * pula para o próximo contato.
         */

        if (
            numeroFormatado.isBlank()
        ) {

            Toast.makeText(
                this,
                "O telefone de ${contato.nome} é inválido. Tentando o próximo contato.",
                Toast.LENGTH_LONG
            ).show()

            chamarProximoContato()

            return
        }

        Toast.makeText(
            this,
            "Ligando para ${contato.nome} " +
                    "(${indiceContatoAtual + 1} de ${contatosCascata.size})",
            Toast.LENGTH_SHORT
        ).show()

        /*
         * Registra TENTATIVA antes de realizar
         * a ligação.
         */

        lifecycleScope.launch {

            registrarStatusCascata(
                contato = contato,
                status = "TENTATIVA"
            )

            /*
             * Mesmo que o histórico não seja salvo,
             * a ligação continua.
             */

            prepararLigacao(
                contato.telefone
            )
        }
    }

    /*
     * ===================================================
     * CONFIRMAÇÃO DE ATENDIMENTO
     * ===================================================
     */

    private fun mostrarConfirmacaoAtendimento() {

        val contato =
            contatosCascata
                .getOrNull(
                    indiceContatoAtual
                )
                ?: return

        AlertDialog.Builder(
            this
        )
            .setTitle(
                "Contato de emergência"
            )
            .setMessage(
                "${contato.nome} atendeu a ligação?"
            )
            .setPositiveButton(
                "Sim"
            ) { _, _ ->

                lifecycleScope.launch {

                    /*
                     * O usuário confirmou que
                     * este contato atendeu.
                     */

                    registrarStatusCascata(
                        contato = contato,
                        status = "ATENDIDO"
                    )

                    finalizarCascataComAtendimento(
                        contato
                    )
                }
            }
            .setNegativeButton(
                "Não"
            ) { _, _ ->

                lifecycleScope.launch {

                    /*
                     * Este contato não atendeu.
                     */

                    registrarStatusCascata(
                        contato = contato,
                        status = "NAO_ATENDIDO"
                    )

                    /*
                     * Passa para o próximo contato.
                     */

                    chamarProximoContato()
                }
            }
            .setCancelable(
                false
            )
            .show()
    }

    /*
     * ===================================================
     * REGISTRO DO HISTÓRICO DA CASCATA
     * ===================================================
     */

    private suspend fun registrarStatusCascata(
        contato: ContatoEmergencia,
        status: String
    ) {

        /*
         * Sem ID de alerta não conseguimos relacionar
         * o evento no backend.
         *
         * A ligação continua normalmente.
         */

        val alertaId =
            alertaIdCascata
                ?: run {

                    android.util.Log.w(
                        "HomeActivity",
                        "Evento $status não registrado: alerta sem ID."
                    )

                    return
                }

        /*
         * No model ContatoEmergencia o campo id é Long?,
         * pois um contato ainda não salvo pode não ter ID.
         *
         * Para um contato vindo do backend, o ID deve
         * estar preenchido. Mesmo assim fazemos a
         * validação para evitar NullPointerException.
         */

        val contatoId =
            contato.id
                ?: run {

                    android.util.Log.e(
                        "HomeActivity",
                        "Contato ${contato.nome} não possui ID. " +
                                "O status $status não será registrado."
                    )

                    return
                }

        historicoNotificacaoRepository
            .registrarTentativa(
                alertaId = alertaId,
                contatoId = contatoId,
                status = status
            )
            .onFailure { erro ->

                /*
                 * Falhar ao registrar o histórico
                 * nunca deve interromper uma emergência.
                 */

                android.util.Log.e(
                    "HomeActivity",
                    "Não foi possível registrar $status " +
                            "para o contato $contatoId: ${erro.message}",
                    erro
                )
            }
    }

    /*
     * ===================================================
     * PRÓXIMO CONTATO
     * ===================================================
     */

    private fun chamarProximoContato() {

        indiceContatoAtual++

        if (
            indiceContatoAtual <
            contatosCascata.size
        ) {

            ligarParaContatoAtual()

        } else {

            finalizarCascataSemAtendimento()
        }
    }

    /*
     * ===================================================
     * CASCATA FINALIZADA COM SUCESSO
     * ===================================================
     */

    private fun finalizarCascataComAtendimento(
        contato: ContatoEmergencia
    ) {

        Toast.makeText(
            this,
            "${contato.nome} atendeu. Cascata de emergência encerrada.",
            Toast.LENGTH_LONG
        ).show()

        limparCascata()
    }

    /*
     * ===================================================
     * CASCATA FINALIZADA SEM ATENDIMENTO
     * ===================================================
     */

    private fun finalizarCascataSemAtendimento() {

        AlertDialog.Builder(
            this
        )
            .setTitle(
                "Nenhum contato disponível"
            )
            .setMessage(
                "Todos os seus contatos de emergência foram tentados " +
                        "e nenhum atendimento foi confirmado."
            )
            .setPositiveButton(
                "OK",
                null
            )
            .setCancelable(
                false
            )
            .show()

        limparCascata()
    }

    /*
     * ===================================================
     * LIMPEZA DO ESTADO DA CASCATA
     * ===================================================
     */

    private fun limparCascata() {

        contatosCascata =
            emptyList()

        indiceContatoAtual =
            0

        cascataEmAndamento =
            false

        aguardandoRetornoLigacao =
            false

        numeroPendenteLigacao =
            null

        alertaIdCascata =
            null
    }

    /*
     * ===================================================
     * PREPARAÇÃO DA LIGAÇÃO
     * ===================================================
     */

    private fun prepararLigacao(
        numeroOriginal: String
    ) {

        val numero =
            formatarNumero(
                numeroOriginal
            )

        if (
            numero.isBlank()
        ) {

            Toast.makeText(
                this,
                "O telefone do contato é inválido.",
                Toast.LENGTH_LONG
            ).show()

            if (
                cascataEmAndamento
            ) {

                chamarProximoContato()
            }

            return
        }

        numeroPendenteLigacao =
            numero

        if (
            ContextCompat
                .checkSelfPermission(
                    this,
                    Manifest.permission.CALL_PHONE
                ) ==
            PackageManager.PERMISSION_GRANTED
        ) {

            iniciarLigacao(
                numero
            )

        } else {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CALL_PHONE
                ),
                REQUEST_CALL_PHONE
            )
        }
    }

    /*
     * ===================================================
     * FORMATAÇÃO DO TELEFONE
     * ===================================================
     */

    private fun formatarNumero(
        numeroOriginal: String
    ): String {

        val numero =
            numeroOriginal
                .filter(
                    Char::isDigit
                )
                .trimStart(
                    '0'
                )

        return when {

            numero.startsWith(
                "55"
            ) &&
                    numero.length >= 12 -> {

                "+$numero"
            }

            numero.length in 10..11 -> {

                "+55$numero"
            }

            else -> {

                ""
            }
        }
    }

    /*
     * ===================================================
     * INÍCIO DA LIGAÇÃO
     * ===================================================
     */

    private fun iniciarLigacao(
        numero: String
    ) {

        /*
         * Marca que o aplicativo está indo para
         * a tela de chamada.
         *
         * Quando voltar, onResume() mostrará
         * a confirmação de atendimento.
         */

        aguardandoRetornoLigacao =
            true

        runCatching {

            startActivity(
                Intent(
                    Intent.ACTION_CALL,
                    Uri.parse(
                        "tel:$numero"
                    )
                )
            )

        }.onFailure {

            aguardandoRetornoLigacao =
                false

            Toast.makeText(
                this,
                "Não foi possível iniciar a ligação. Tentando o próximo contato.",
                Toast.LENGTH_LONG
            ).show()

            if (
                cascataEmAndamento
            ) {

                chamarProximoContato()
            }
        }
    }

    /*
     * ===================================================
     * PERMISSÃO DE NOTIFICAÇÕES
     * ===================================================
     */

    private fun solicitarPermissaoNotificacaoSeNecessario() {

        if (
            Build.VERSION.SDK_INT < 33 ||
            notificationPermissionAsked
        ) {

            return
        }

        if (
            ContextCompat
                .checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) ==
            PackageManager.PERMISSION_GRANTED
        ) {

            return
        }

        notificationPermissionAsked =
            true

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.POST_NOTIFICATIONS
            ),
            REQUEST_NOTIFICATIONS
        )
    }

    /*
     * ===================================================
     * LOGOUT
     * ===================================================
     */

    private fun logout() {

        lifecycleScope.launch {

            usuarioId
                ?.let { id ->

                    monitoramentoRepository
                        .obterStatus(id)
                        .getOrNull()
                        ?.let { status ->

                            monitoramentoRepository
                                .configurar(
                                    id,
                                    false,
                                    status.intervaloMinutos
                                )
                        }
                }

            CheckInScheduler.cancel(
                this@HomeActivity
            )

            sessionManager.logout()

            irParaLogin()
        }
    }

    private fun irParaLogin() {

        startActivity(
            Intent(
                this,
                LoginActivity::class.java
            ).apply {

                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )

        finish()
    }

    /*
     * ===================================================
     * RESULTADO DAS PERMISSÕES
     * ===================================================
     */

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        /*
         * PERMISSÃO DE LIGAÇÃO
         */

        if (
            requestCode ==
            REQUEST_CALL_PHONE
        ) {

            if (
                grantResults.firstOrNull() ==
                PackageManager.PERMISSION_GRANTED
            ) {

                numeroPendenteLigacao
                    ?.let(
                        ::iniciarLigacao
                    )

            } else {

                Toast.makeText(
                    this,
                    "A permissão para realizar ligações é necessária para executar a cascata de emergência.",
                    Toast.LENGTH_LONG
                ).show()

                limparCascata()
            }
        }
    }
}