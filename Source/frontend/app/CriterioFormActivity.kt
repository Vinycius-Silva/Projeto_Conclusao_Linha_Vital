package com.linhavital.app.ui.home
import com.linhavital.app.ui.common.showFormError
import com.linhavital.app.R
import androidx.core.content.ContextCompat

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.linhavital.app.data.repository.MonitoramentoRepository
import com.linhavital.app.databinding.ActivityCriterioFormBinding
import com.linhavital.app.monitoring.CheckInScheduler
import com.linhavital.app.utils.SessionManager
import com.linhavital.app.utils.applySystemBarsPadding
import kotlinx.coroutines.launch

class CriterioFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCriterioFormBinding
    private val repository = MonitoramentoRepository()
    private var usuarioId: Long? = null
    private val intervalos = listOf(1, 15, 30, 35, 60, 120)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCriterioFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding.headerCriterioForm.applySystemBarsPadding(top = true)
        binding.rootCriterioForm.applySystemBarsPadding(bottom = true, left = true, right = true, ime = true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.lv_background)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        val labels = intervalos.map {
            if (it == 1) "1 minuto (demonstração)" else "$it minutos"
        }
        binding.spinnerIntervalo.adapter = ArrayAdapter(
            this,
            R.layout.item_spinner,
            labels
        ).apply { setDropDownViewResource(R.layout.item_spinner_dropdown) }

        binding.btnVoltar.setOnClickListener { finish() }
        binding.btnSalvar.setOnClickListener { salvar() }

        lifecycleScope.launch {
            usuarioId = SessionManager(this@CriterioFormActivity).getUserId()
            val id = usuarioId
            if (id == null) {
                Toast.makeText(this@CriterioFormActivity, "Sessão inválida.", Toast.LENGTH_LONG).show()
                finish()
                return@launch
            }
            setLoading(true)
            repository.obterStatus(id)
                .onSuccess { status ->
                    binding.switchMonitoramento.isChecked = status.ativo
                    val index = intervalos.indexOf(status.intervaloMinutos)
                    binding.spinnerIntervalo.setSelection(if (index >= 0) index else 3)
                }
                .onFailure {
                    binding.tvFormError.showFormError("Não foi possível carregar a configuração.")
                }
            setLoading(false)
        }
    }

    private fun salvar() {
        val id = usuarioId ?: return
        val intervalo = intervalos[binding.spinnerIntervalo.selectedItemPosition]
        val ativo = binding.switchMonitoramento.isChecked
        setLoading(true, R.string.lv_saving)

        lifecycleScope.launch {
            repository.configurar(id, ativo, intervalo)
                .onSuccess { status ->
                    if (status.ativo) {
                        CheckInScheduler.schedule(this@CriterioFormActivity, status.intervaloMinutos.toLong())
                    } else {
                        CheckInScheduler.cancel(this@CriterioFormActivity)
                    }
                    Toast.makeText(
                        this@CriterioFormActivity,
                        if (ativo) "Monitoramento configurado." else "Monitoramento pausado.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                .onFailure {
                    setLoading(false)
                    binding.tvFormError.showFormError("Não foi possível salvar a configuração.")
                }
        }
    }

    private fun setLoading(loading: Boolean, loadingLabel: Int = R.string.lv_loading) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSalvar.isEnabled = !loading
        binding.btnSalvar.setText(if (loading) loadingLabel else R.string.lv_save_configuration)
        if (loading) binding.tvFormError.showFormError(null)
        binding.switchMonitoramento.isEnabled = !loading
        binding.spinnerIntervalo.isEnabled = !loading
    }
}
