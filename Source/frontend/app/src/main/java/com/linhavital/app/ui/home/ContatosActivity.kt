package com.linhavital.app.ui.home

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.linhavital.app.R
import com.linhavital.app.databinding.ActivityContatosBinding
import com.linhavital.app.data.model.ContatoEmergencia
import com.linhavital.app.utils.SessionManager
import com.linhavital.app.utils.applySystemBarsPadding
import kotlinx.coroutines.launch

class ContatosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContatosBinding
    private val viewModel: ContatoViewModel by viewModels()
    private lateinit var adapter: ContatoAdapter
    private lateinit var sessionManager: SessionManager
    private var usuarioIdLogado: Long? = null
    private var numeroPendenteLigacao: String? = null

    companion object {
        private const val REQUEST_CALL_PHONE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityContatosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding.headerContatos.applySystemBarsPadding(top = true)
        binding.bottomNavigationContainer.applySystemBarsPadding(bottom = true)
        window.statusBarColor = android.graphics.Color.parseColor("#FFF5F5")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        sessionManager = SessionManager(this)

        binding.btnVoltar.setOnClickListener { finish() }
        binding.fabAdicionarContato.setOnClickListener { mostrarDialogContato(null) }

        adapter = ContatoAdapter(
            emptyList(),
            onLigar = ::ligarParaContato,
            onEditar = ::mostrarDialogContato,
            onDeletar = ::confirmarExclusao
        )

        binding.rvContatos.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.rvContatos.adapter = adapter

        viewModel.contatos.observe(this) { contatos ->
            binding.rvContatos.visibility = if (contatos.isEmpty()) View.GONE else View.VISIBLE
            binding.tvEmpty.visibility = if (contatos.isEmpty()) View.VISIBLE else View.GONE
            adapter.atualizar(contatos)
        }

        viewModel.estado.observe(this) { estado ->
            when (estado) {
                is ContatoEstado.Sucesso ->
                    Toast.makeText(this, "Contato cadastrado.", Toast.LENGTH_SHORT).show()
                is ContatoEstado.Atualizado ->
                    Toast.makeText(this, "Contato atualizado.", Toast.LENGTH_SHORT).show()
                is ContatoEstado.Erro ->
                    Toast.makeText(this, estado.message, Toast.LENGTH_LONG).show()
                else -> Unit
            }
        }

        configurarBottomBar()

        lifecycleScope.launch {
            val id = sessionManager.getUserId()
            if (id == null || id == 0L) {
                Toast.makeText(this@ContatosActivity, "Sessão inválida. Faça login novamente.", Toast.LENGTH_LONG).show()
                finish()
                return@launch
            }
            usuarioIdLogado = id
            viewModel.carregarContatos(id)
        }
    }

    private fun configurarBottomBar() {
        binding.btnNavContatos.setBackgroundResource(R.drawable.nav_item_active)
        binding.iconNavContatos.setColorFilter(android.graphics.Color.parseColor("#BB0013"))
        binding.labelNavContatos.setTextColor(android.graphics.Color.parseColor("#BB0013"))

        binding.btnNavHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
            finish()
        }
        binding.btnNavContatos.setOnClickListener { /* tela atual */ }
        binding.btnNavCriterios.setOnClickListener {
            startActivity(Intent(this, CriteriosActivity::class.java))
        }
    }

    private fun mostrarDialogContato(contato: ContatoEmergencia?) {
        val usuarioId = usuarioIdLogado ?: return

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 8, 48, 0)
        }

        val etNome = TextInputEditText(this).apply {
            hint = "Nome completo"
            setText(contato?.nome.orEmpty())
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        }
        val etTelefone = TextInputEditText(this).apply {
            hint = "Telefone"
            setText(contato?.telefone.orEmpty())
            inputType = InputType.TYPE_CLASS_PHONE
        }
        val etEmail = TextInputEditText(this).apply {
            hint = "E-mail (opcional)"
            setText(contato?.email.orEmpty())
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        val spinner = Spinner(this)
        val tipos = listOf("Emergência", "Confiança", "Familiar", "Médico", "Amigo", "Cuidador", "Outro")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tipos)
        contato?.tipoContato?.let {
            val index = tipos.indexOf(it)
            if (index >= 0) spinner.setSelection(index)
        }

        layout.addView(etNome)
        layout.addView(etTelefone)
        layout.addView(etEmail)
        layout.addView(spinner)

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (contato == null) "Adicionar contato" else "Editar contato")
            .setView(layout)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salvar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val nome = etNome.text?.toString()?.trim().orEmpty()
                val telefone = etTelefone.text?.toString()?.trim().orEmpty()
                val email = etEmail.text?.toString()?.trim().orEmpty()
                val tipo = spinner.selectedItem.toString()

                if (nome.isBlank() || telefone.isBlank()) {
                    Toast.makeText(this, "Nome e telefone são obrigatórios.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (contato?.id == null) {
                    viewModel.cadastrarContato(usuarioId, nome, telefone, email, tipo)
                } else {
                    viewModel.atualizarContato(usuarioId, contato.id, nome, telefone, email, tipo)
                }
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun confirmarExclusao(contatoId: Long) {
        AlertDialog.Builder(this)
            .setTitle("Excluir contato?")
            .setMessage("Esse contato deixará de fazer parte da sua rede de proteção.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Excluir") { _, _ ->
                usuarioIdLogado?.let { viewModel.deletarContato(it, contatoId) }
            }
            .show()
    }

    private fun ligarParaContato(numeroOriginal: String) {
        val numero = numeroOriginal.filter { it.isDigit() }
        if (numero.isBlank()) {
            Toast.makeText(this, "Número inválido.", Toast.LENGTH_SHORT).show()
            return
        }

        val formatado = when {
            numero.startsWith("55") -> "+$numero"
            numero.length in 10..11 -> "+55$numero"
            else -> numero
        }

        numeroPendenteLigacao = formatado
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            iniciarLigacao(formatado)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                REQUEST_CALL_PHONE
            )
        }
    }

    private fun iniciarLigacao(numero: String) {
        try {
            startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$numero")))
        } catch (_: Exception) {
            Toast.makeText(this, "Não foi possível iniciar a ligação.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CALL_PHONE &&
            grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        ) {
            numeroPendenteLigacao?.let(::iniciarLigacao)
        }
    }
}
