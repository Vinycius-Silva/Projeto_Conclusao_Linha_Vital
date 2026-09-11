package com.linhavital.app.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.linhavital.app.data.model.ContatoEmergencia
import com.linhavital.app.databinding.ActivityContatoFormBinding
import com.linhavital.app.utils.SessionManager
import com.linhavital.app.utils.applySystemBarsPadding
import kotlinx.coroutines.launch

class ContatoFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContatoFormBinding
    private val viewModel: ContatoViewModel by viewModels()
    private var usuarioId: Long? = null
    private var contatoId: Long? = null
    private val tipos = listOf("Emergência", "Confiança", "Familiar", "Médico", "Amigo", "Cuidador", "Outro")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContatoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding.headerContatoForm.applySystemBarsPadding(top = true)
        binding.rootContatoForm.applySystemBarsPadding(bottom = true)
        window.statusBarColor = android.graphics.Color.parseColor("#FFF5F5")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        contatoId = intent.getLongExtra(EXTRA_ID, 0L).takeIf { it > 0 }
        binding.tvTitle.text = if (contatoId == null) "Adicionar contato" else "Editar contato"
        binding.etNome.setText(intent.getStringExtra(EXTRA_NOME).orEmpty())
        binding.etTelefone.setText(intent.getStringExtra(EXTRA_TELEFONE).orEmpty())
        binding.etEmail.setText(intent.getStringExtra(EXTRA_EMAIL).orEmpty())
        binding.spinnerTipo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tipos)
        intent.getStringExtra(EXTRA_TIPO)?.let { tipoAtual ->
            tipos.indexOf(tipoAtual).takeIf { it >= 0 }?.let(binding.spinnerTipo::setSelection)
        }

        binding.btnVoltar.setOnClickListener { finish() }
        binding.btnSalvar.setOnClickListener { salvar() }

        lifecycleScope.launch {
            usuarioId = SessionManager(this@ContatoFormActivity).getUserId()
            if (usuarioId == null) {
                Toast.makeText(this@ContatoFormActivity, "Sessão inválida.", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        viewModel.estado.observe(this) { estado ->
            when (estado) {
                ContatoEstado.Loading -> setLoading(true)
                ContatoEstado.Sucesso -> {
                    Toast.makeText(this, "Contato adicionado.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                ContatoEstado.Atualizado -> {
                    Toast.makeText(this, "Contato atualizado.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is ContatoEstado.Erro -> {
                    setLoading(false)
                    Toast.makeText(this, estado.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun salvar() {
        val idUsuario = usuarioId ?: return
        val nome = binding.etNome.text?.toString().orEmpty().trim()
        val telefone = binding.etTelefone.text?.toString().orEmpty().trim()
        val email = binding.etEmail.text?.toString().orEmpty().trim()
        val tipo = binding.spinnerTipo.selectedItem?.toString().orEmpty()

        val id = contatoId
        if (id == null) {
            viewModel.cadastrarContato(idUsuario, nome, telefone, email, tipo)
        } else {
            viewModel.atualizarContato(idUsuario, id, nome, telefone, email, tipo)
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSalvar.isEnabled = !loading
    }

    companion object {
        private const val EXTRA_ID = "contato_id"
        private const val EXTRA_NOME = "contato_nome"
        private const val EXTRA_TELEFONE = "contato_telefone"
        private const val EXTRA_EMAIL = "contato_email"
        private const val EXTRA_TIPO = "contato_tipo"

        fun intent(context: Context, contato: ContatoEmergencia?): Intent =
            Intent(context, ContatoFormActivity::class.java).apply {
                contato?.let {
                    putExtra(EXTRA_ID, it.id ?: 0L)
                    putExtra(EXTRA_NOME, it.nome)
                    putExtra(EXTRA_TELEFONE, it.telefone)
                    putExtra(EXTRA_EMAIL, it.email)
                    putExtra(EXTRA_TIPO, it.tipoContato)
                }
            }
    }
}
