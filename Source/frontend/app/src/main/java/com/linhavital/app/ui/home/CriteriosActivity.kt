package com.linhavital.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.linhavital.app.R
import com.linhavital.app.data.model.MonitoramentoStatus
import com.linhavital.app.data.repository.MonitoramentoRepository
import com.linhavital.app.databinding.ActivityCriteriosBinding
import com.linhavital.app.utils.SessionManager
import com.linhavital.app.utils.applySystemBarsPadding
import kotlinx.coroutines.launch

class CriteriosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCriteriosBinding
    private val repository = MonitoramentoRepository()
    private lateinit var sessionManager: SessionManager
    private var usuarioId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCriteriosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding.headerCriterios.applySystemBarsPadding(top = true)
        binding.bottomNavigation.bottomNavigationContainer.applySystemBarsPadding(bottom = true)
        window.statusBarColor = android.graphics.Color.parseColor("#FFF5F5")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        sessionManager = SessionManager(this)
        binding.btnAdicionarCriterio.setOnClickListener { abrirConfiguracao() }
        binding.cardInatividade.setOnClickListener { abrirConfiguracao() }

        binding.bottomNavigation.btnNavCriterios.setBackgroundResource(R.drawable.nav_item_active)
        binding.bottomNavigation.iconNavCriterios.setColorFilter(android.graphics.Color.parseColor("#BB0013"))
        binding.bottomNavigation.labelNavCriterios.setTextColor(android.graphics.Color.parseColor("#BB0013"))

        binding.bottomNavigation.btnNavHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
            finish()
        }
        binding.bottomNavigation.btnNavContatos.setOnClickListener {
            startActivity(Intent(this, ContatosActivity::class.java))
            finish()
        }
        binding.bottomNavigation.btnNavCriterios.setOnClickListener { }

        lifecycleScope.launch {
            usuarioId = sessionManager.getUserId()
            if (usuarioId == null) {
                Toast.makeText(this@CriteriosActivity, "Sessão inválida.", Toast.LENGTH_LONG).show()
                finish()
            } else {
                carregarStatus()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (usuarioId != null) carregarStatus()
    }

    private fun abrirConfiguracao() {
        startActivity(Intent(this, CriterioFormActivity::class.java))
    }

    private fun carregarStatus() {
        val id = usuarioId ?: return
        lifecycleScope.launch {
            val result = repository.obterStatus(id)
            result.onSuccess(::renderStatus)
                .onFailure {
                    binding.tvStatusInatividade.text = "Não foi possível carregar a configuração"
                }
        }
    }

    private fun renderStatus(status: MonitoramentoStatus) {
        binding.tvStatusInatividade.text = if (status.ativo) {
            "Ativo • check-in a cada ${status.intervaloMinutos} min"
        } else {
            "Pausado"
        }
        binding.tvDescricaoInatividade.text = if (status.ativo) {
            "O app lembra você de confirmar que está bem. Sem resposta, o backend registra uma ocorrência de inatividade."
        } else {
            "Ative quando quiser usar os check-ins preventivos."
        }
    }
}
