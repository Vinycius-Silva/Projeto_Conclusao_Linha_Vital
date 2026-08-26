package com.linhavital.app.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.linhavital.app.utils.applySystemBarsPadding
import androidx.lifecycle.lifecycleScope
import com.linhavital.app.data.repository.ContatoRepository
import com.linhavital.app.databinding.ActivityHomeBinding
import com.linhavital.app.ui.auth.LoginActivity
import com.linhavital.app.utils.SessionManager
import kotlinx.coroutines.launch
import com.linhavital.app.data.repository.AlertaRepository

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var sessionManager: SessionManager

    private var sosClickCount = 0
    private var lastSosClickTime = 0L

    private var numeroPendenteLigacao: String? = null

    companion object {
        private const val REQUEST_CALL_PHONE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding.headerHome.applySystemBarsPadding(top = true)
        binding.bottomNavigationContainer.applySystemBarsPadding(bottom = true)

        window.statusBarColor = android.graphics.Color.parseColor("#FFF5F5")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        sessionManager = SessionManager(this)

        pedirPermissaoLigacaoAoAbrir()

        lifecycleScope.launch {
            binding.tvBemVindo.text = "Olá, ${sessionManager.getUserName() ?: "Usuário"}!"
        }

        binding.btnSOS.setOnClickListener {
            registrarCliqueEmergencia("Clique")
        }

        binding.btnNavHome.setBackgroundResource(com.linhavital.app.R.drawable.nav_item_active)
        binding.iconNavHome.setColorFilter(android.graphics.Color.parseColor("#BB0013"))
        binding.btnNavHome.setOnClickListener { }
        binding.btnNavCriterios.setOnClickListener {
            startActivity(Intent(this, CriteriosActivity::class.java))
        }

        binding.btnNavContatos.setOnClickListener {
            startActivity(Intent(this, ContatosActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            lifecycleScope.launch {
                sessionManager.logout()
                startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun pedirPermissaoLigacaoAoAbrir() {
        val permissaoLigacao = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CALL_PHONE
        )

        if (permissaoLigacao != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                REQUEST_CALL_PHONE
            )
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
            if (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                registrarCliqueEmergencia("Aperte")
                return true
            }
        }

        return super.dispatchKeyEvent(event)
    }

    private fun registrarCliqueEmergencia(tipoAcao: String) {
        val agora = System.currentTimeMillis()

        if (agora - lastSosClickTime > 2000) {
            sosClickCount = 0
        }

        sosClickCount++
        lastSosClickTime = agora

        when (sosClickCount) {
            1 -> {
                Toast.makeText(
                    this,
                    "$tipoAcao mais 2x para acionar emergência",
                    Toast.LENGTH_SHORT
                ).show()
            }

            2 -> {
                Toast.makeText(
                    this,
                    "$tipoAcao mais 1x para acionar emergência",
                    Toast.LENGTH_SHORT
                ).show()
            }

            3 -> {
                sosClickCount = 0
                acionarEmergencia()
            }
        }
    }

    private fun acionarEmergencia() {
        Toast.makeText(this, "🚨 EMERGÊNCIA ACIONADA!", Toast.LENGTH_LONG).show()

        lifecycleScope.launch {
            val usuarioId = sessionManager.getUserId()

            if (usuarioId == null || usuarioId == 0L) {
                Toast.makeText(
                    this@HomeActivity,
                    "Usuário não encontrado na sessão. Faça login novamente.",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            val alertaRepository = AlertaRepository()
            val resultadoAlerta = alertaRepository.registrarAlertaPanico(usuarioId)

            if (resultadoAlerta.isFailure) {
                Toast.makeText(
                    this@HomeActivity,
                    "Emergência acionada, mas o alerta não foi salvo no banco",
                    Toast.LENGTH_LONG
                ).show()
            }

            ligarParaContato()
        }
    }

    private fun ligarParaContato() {
        lifecycleScope.launch {
            try {
                val usuarioId = sessionManager.getUserId()

                if (usuarioId == null || usuarioId == 0L) {
                    Toast.makeText(
                        this@HomeActivity,
                        "Usuário não encontrado na sessão. Faça login novamente.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                val repository = ContatoRepository()
                val result = repository.listarContatos(usuarioId)

                if (result.isSuccess) {
                    val contatos = result.getOrNull()

                    if (!contatos.isNullOrEmpty()) {
                        val contato = contatos[0]
                        val numeroFormatado = formatarNumeroParaLigacao(contato.telefone)

                        if (numeroFormatado.isBlank()) {
                            Toast.makeText(
                                this@HomeActivity,
                                "Número do contato inválido!",
                                Toast.LENGTH_LONG
                            ).show()
                            return@launch
                        }

                        numeroPendenteLigacao = numeroFormatado

                        if (ContextCompat.checkSelfPermission(
                                this@HomeActivity,
                                Manifest.permission.CALL_PHONE
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            iniciarLigacao(numeroFormatado)
                        } else {
                            ActivityCompat.requestPermissions(
                                this@HomeActivity,
                                arrayOf(Manifest.permission.CALL_PHONE),
                                REQUEST_CALL_PHONE
                            )
                        }
                    } else {
                        Toast.makeText(
                            this@HomeActivity,
                            "Nenhum contato de emergência cadastrado!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@HomeActivity,
                        "Erro ao buscar contatos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@HomeActivity,
                    "Erro ao buscar contatos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun iniciarLigacao(numero: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$numero")
            }

            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Não foi possível iniciar a ligação",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun formatarNumeroParaLigacao(numeroOriginal: String): String {
        var numero = numeroOriginal.filter { it.isDigit() }

        while (numero.startsWith("0")) {
            numero = numero.drop(1)
        }

        if (numero.startsWith("55") && numero.length >= 12) {
            return "+$numero"
        }

        if (numero.length == 11) {
            return "+55$numero"
        }

        if (numero.length == 10) {
            return "+55$numero"
        }

        return numero
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                numeroPendenteLigacao?.let { numero ->
                    iniciarLigacao(numero)
                }
            } else {
                Toast.makeText(
                    this,
                    "Permissão de ligação negada. O SOS não conseguirá ligar automaticamente.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}