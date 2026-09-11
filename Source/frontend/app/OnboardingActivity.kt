package com.linhavital.app.ui.onboarding
import android.view.View
import com.linhavital.app.ui.common.revealPage
import com.linhavital.app.R
import androidx.core.content.ContextCompat

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.linhavital.app.databinding.ActivityOnboardingBinding
import com.linhavital.app.ui.home.HomeActivity
import com.linhavital.app.utils.SessionManager
import com.linhavital.app.utils.applySystemBarsPadding
import kotlinx.coroutines.launch

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var sessionManager: SessionManager
    private var page = 0

    private val pages = listOf(
        Page(
            eyebrow = "MONITORAMENTO PREVENTIVO",
            title = "Sua segurança é nossa prioridade",
            body = "O MVP trabalha com check-ins periódicos. Você escolhe o intervalo e confirma que está tudo bem sem compartilhar dados continuamente.",
            detail = "Configure o período entre check-ins na tela Critérios."
        ),
        Page(
            eyebrow = "CHECK-IN PROGRAMADO",
            title = "Receba um lembrete e confirme",
            body = "Quando o período configurado terminar, o Linha Vital mostra um lembrete para você abrir o app e tocar em ‘Estou bem’.",
            detail = "Se a confirmação não ocorrer, o backend registra uma ocorrência de inatividade sem duplicar alertas."
        ),
        Page(
            eyebrow = "REDE DE PROTEÇÃO",
            title = "Defina seu contato prioritário",
            body = "Cadastre contatos de confiança. No SOS manual, o primeiro contato da lista é usado para a ligação de emergência.",
            detail = "A detecção automática de queda e o envio externo de SMS/FCM ficam fora deste MVP."
        ),
        Page(
            eyebrow = "SOS MANUAL",
            title = "Você continua no controle",
            body = "Na tela inicial, toque três vezes no botão SOS ou pressione três vezes o volume para baixo enquanto o app estiver em primeiro plano.",
            detail = "O alerta é registrado no backend e o app tenta ligar para seu contato prioritário."
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding.rootOnboarding.applySystemBarsPadding(top = true, bottom = true, left = true, right = true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.lv_background)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        sessionManager = SessionManager(this)
        page = savedInstanceState?.getInt("ui_onboarding_page", 0)?.coerceIn(0, pages.lastIndex) ?: 0
        binding.btnSkip.setOnClickListener { concluir() }
        binding.btnNext.setOnClickListener {
            if (page == pages.lastIndex) concluir() else {
                page++
                render()
                binding.onboardingScroll.scrollTo(0, 0)
                binding.pageContent.revealPage()
            }
        }
        render()
    }

    private fun render() {
        val current = pages[page]
        binding.tvEyebrow.text = current.eyebrow
        binding.tvTitle.text = current.title
        binding.tvBody.text = current.body
        binding.tvDetail.text = current.detail
        binding.tvProgress.text = "${page + 1} DE ${pages.size}"
        binding.btnNext.text = if (page == pages.lastIndex) "Iniciar" else "Próximo"
        binding.btnSkip.visibility = if (page == pages.lastIndex) View.INVISIBLE else View.VISIBLE
        binding.btnSkip.isEnabled = page != pages.lastIndex
        binding.ivIllustration.setImageResource(when (page) {
            0 -> R.drawable.figma_welcome_person
            1 -> R.drawable.ic_monitoramento_vital
            2 -> R.drawable.ic_nav_contacts
            else -> R.drawable.ic_ajuda_imediata
        })
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3, binding.dot4)
        dots.forEachIndexed { index, dot ->
            dot.isSelected = index == page
            dot.layoutParams = dot.layoutParams.apply {
                width = ((if (index == page) 24 else 8) * resources.displayMetrics.density).toInt()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("ui_onboarding_page", page)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        binding.pageContent.animate().cancel()
        super.onDestroy()
    }

    private fun concluir() {
        lifecycleScope.launch {
            sessionManager.completeOnboarding()
            startActivity(Intent(this@OnboardingActivity, HomeActivity::class.java))
            finish()
        }
    }

    private data class Page(
        val eyebrow: String,
        val title: String,
        val body: String,
        val detail: String
    )
}
