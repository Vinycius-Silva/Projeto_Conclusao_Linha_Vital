package com.linhavital.app.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.linhavital.app.R
import com.linhavital.app.data.model.MonitoramentoStatus
import com.linhavital.app.data.repository.AlertaRepository
import com.linhavital.app.data.repository.ContatoRepository
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
    private val monitoramentoRepository = MonitoramentoRepository()
    private val contatoRepository = ContatoRepository()
    private val alertaRepository = AlertaRepository()

    private var usuarioId: Long? = null
    private var sosClickCount = 0
    private var lastSosClickTime = 0L
    private var numeroPendenteLigacao: String? = null
    private var notificationPermissionAsked = false

    companion object {
        private const val REQUEST_CALL_PHONE = 101
        private const val REQUEST_NOTIFICATIONS = 103
        const val EXTRA_OPEN_CHECK_IN = "open_check_in"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding.headerHome.applySystemBarsPadding(top = true)
        binding.bottomNavigation.bottomNavigationContainer.applySystemBarsPadding(bottom = true)
        window.statusBarColor = android.graphics.Color.parseColor("#FFF5F5")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        sessionManager = SessionManager(this)
        CheckInScheduler.ensureChannel(this)

        binding.btnSOS.setOnClickListener { registrarCliqueEmergencia("Toque") }
        binding.btnCheckIn.setOnClickListener { confirmarCheckIn() }
        binding.btnConfigurarMonitoramento.setOnClickListener {
            startActivity(Intent(this, CriterioFormActivity::class.java))
        }
        binding.btnVerContatos.setOnClickListener {
            startActivity(Intent(this, ContatosActivity::class.java))
        }

        configurarBottomBar()
        binding.btnLogout.setOnClickListener { logout() }

        lifecycleScope.launch {
            usuarioId = sessionManager.getUserId()
            if (usuarioId == null || usuarioId == 0L) {
                irParaLogin()
                return@launch
            }
            binding.tvBemVindo.text = "Olá, ${sessionManager.getUserName() ?: "Usuário"}!"
            carregarDashboard()
            if (intent.getBooleanExtra(EXTRA_OPEN_CHECK_IN, false)) {
                Toast.makeText(
                    this@HomeActivity,
                    "Seu check-in está aguardando confirmação.",
                    Toast.LENGTH_LONG
                ).show()
                intent.removeExtra(EXTRA_OPEN_CHECK_IN)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (usuarioId != null) carregarDashboard()
    }

    private fun configurarBottomBar() {
        binding.bottomNavigation.btnNavHome.setBackgroundResource(R.drawable.nav_item_active)
        binding.bottomNavigation.iconNavHome.setColorFilter(android.graphics.Color.parseColor("#BB0013"))
        binding.bottomNavigation.labelNavHome.setTextColor(android.graphics.Color.parseColor("#BB0013"))
        binding.bottomNavigation.btnNavHome.setOnClickListener { }
        binding.bottomNavigation.btnNavCriterios.setOnClickListener {
            startActivity(Intent(this, CriteriosActivity::class.java))
        }
        binding.bottomNavigation.btnNavContatos.setOnClickListener {
            startActivity(Intent(this, ContatosActivity::class.java))
        }
    }

    private fun carregarDashboard() {
        val id = usuarioId ?: return
        lifecycleScope.launch {
            monitoramentoRepository.obterStatus(id)
                .onSuccess(::renderMonitoramento)
                .onFailure {
                    binding.tvMonitoramentoStatus.text = "Backend indisponível"
                    binding.tvProximoCheckIn.text = "Não foi possível consultar o monitoramento."
                    binding.btnCheckIn.isEnabled = false
                }

            contatoRepository.listarContatos(id)
                .onSuccess { contatos ->
                    binding.tvContatoPrioritario.text = when {
                        contatos.isEmpty() -> "Nenhum contato cadastrado"
                        else -> "Prioritário: ${contatos.first().nome} • ${contatos.first().tipoContato}"
                    }
                }
                .onFailure {
                    binding.tvContatoPrioritario.text = "Não foi possível carregar seus contatos"
                }
        }
    }

    private fun renderMonitoramento(status: MonitoramentoStatus) {
        binding.btnCheckIn.isEnabled = status.ativo
        binding.tvMonitoramentoStatus.text = when {
            !status.ativo -> "Monitoramento pausado"
            status.checkInPendente || status.alertaInatividadeAberto -> "Check-in pendente"
            else -> "Monitoramento ativo"
        }

        binding.tvProximoCheckIn.text = when {
            !status.ativo -> "Ative os check-ins em Critérios para iniciar o ciclo preventivo."
            status.checkInPendente || status.alertaInatividadeAberto ->
                "O prazo terminou. Confirme agora que está tudo bem para encerrar a ocorrência."
            else -> "Próximo check-in em aproximadamente ${status.minutosRestantes.coerceAtLeast(1)} min."
        }

        if (status.ativo) {
            solicitarPermissaoNotificacaoSeNecessario()
            if (status.checkInPendente || status.alertaInatividadeAberto) {
                CheckInScheduler.cancel(this)
            } else if (!CheckInScheduler.isScheduled(this)) {
                CheckInScheduler.schedule(this, status.minutosRestantes.coerceAtLeast(1))
            }
        } else {
            CheckInScheduler.cancel(this)
        }
    }

    private fun confirmarCheckIn() {
        val id = usuarioId ?: return
        binding.btnCheckIn.isEnabled = false
        lifecycleScope.launch {
            monitoramentoRepository.checkIn(id)
                .onSuccess { status ->
                    renderMonitoramento(status)
                    CheckInScheduler.schedule(this@HomeActivity, status.intervaloMinutos.toLong())
                    Toast.makeText(
                        this@HomeActivity,
                        "Check-in confirmado. Que bom saber que está tudo bem.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .onFailure {
                    binding.btnCheckIn.isEnabled = true
                    Toast.makeText(
                        this@HomeActivity,
                        "Não foi possível confirmar o check-in.",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0 &&
            event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
        ) {
            registrarCliqueEmergencia("Pressione")
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    private fun registrarCliqueEmergencia(tipoAcao: String) {
        val agora = System.currentTimeMillis()
        if (agora - lastSosClickTime > 2_000) sosClickCount = 0
        sosClickCount++
        lastSosClickTime = agora

        when (sosClickCount) {
            1 -> Toast.makeText(this, "$tipoAcao mais 2x para acionar o SOS", Toast.LENGTH_SHORT).show()
            2 -> Toast.makeText(this, "$tipoAcao mais 1x para acionar o SOS", Toast.LENGTH_SHORT).show()
            3 -> {
                sosClickCount = 0
                acionarEmergencia()
            }
        }
    }

    private fun acionarEmergencia() {
        val id = usuarioId ?: return
        Toast.makeText(this, "SOS acionado. Buscando seu contato prioritário...", Toast.LENGTH_LONG).show()

        lifecycleScope.launch {
            val alerta = alertaRepository.registrarAlertaPanico(id)
            if (alerta.isFailure) {
                Toast.makeText(
                    this@HomeActivity,
                    "O SOS continuará, mas não foi possível registrar o alerta no backend.",
                    Toast.LENGTH_LONG
                ).show()
            }

            contatoRepository.listarContatos(id)
                .onSuccess { contatos ->
                    val contato = contatos.firstOrNull()
                    if (contato == null) {
                        Toast.makeText(
                            this@HomeActivity,
                            "Cadastre um contato para completar o fluxo de SOS.",
                            Toast.LENGTH_LONG
                        ).show()
                        startActivity(Intent(this@HomeActivity, ContatosActivity::class.java))
                    } else {
                        prepararLigacao(contato.telefone)
                    }
                }
                .onFailure {
                    Toast.makeText(
                        this@HomeActivity,
                        "Não foi possível consultar o contato prioritário.",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun prepararLigacao(numeroOriginal: String) {
        val numero = formatarNumero(numeroOriginal)
        if (numero.isBlank()) {
            Toast.makeText(this, "O telefone do contato prioritário é inválido.", Toast.LENGTH_LONG).show()
            return
        }
        numeroPendenteLigacao = numero

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            iniciarLigacao(numero)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                REQUEST_CALL_PHONE
            )
        }
    }

    private fun formatarNumero(numeroOriginal: String): String {
        val numero = numeroOriginal.filter(Char::isDigit).trimStart('0')
        return when {
            numero.startsWith("55") && numero.length >= 12 -> "+$numero"
            numero.length in 10..11 -> "+55$numero"
            else -> ""
        }
    }

    private fun iniciarLigacao(numero: String) {
        runCatching { startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$numero"))) }
            .onFailure {
                Toast.makeText(this, "Não foi possível iniciar a ligação.", Toast.LENGTH_LONG).show()
            }
    }

    private fun solicitarPermissaoNotificacaoSeNecessario() {
        if (Build.VERSION.SDK_INT < 33 || notificationPermissionAsked) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) return

        notificationPermissionAsked = true
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            REQUEST_NOTIFICATIONS
        )
    }

    private fun logout() {
        lifecycleScope.launch {
            usuarioId?.let { id ->
                monitoramentoRepository.obterStatus(id).getOrNull()?.let { status ->
                    monitoramentoRepository.configurar(id, false, status.intervaloMinutos)
                }
            }
            CheckInScheduler.cancel(this@HomeActivity)
            sessionManager.logout()
            irParaLogin()
        }
    }

    private fun irParaLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                numeroPendenteLigacao?.let(::iniciarLigacao)
            } else {
                Toast.makeText(
                    this,
                    "Sem permissão de ligação, o alerta é registrado, mas a chamada automática não pode ser iniciada.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
