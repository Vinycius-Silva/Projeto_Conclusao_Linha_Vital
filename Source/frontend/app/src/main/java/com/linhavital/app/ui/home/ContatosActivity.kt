package com.linhavital.app.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.linhavital.app.databinding.ActivityContatosBinding
import com.linhavital.app.utils.SessionManager
import kotlinx.coroutines.launch

class ContatosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContatosBinding
    private val viewModel: ContatoViewModel by viewModels()
    private lateinit var adapter: ContatoAdapter
    private lateinit var sessionManager: SessionManager

    private var usuarioIdLogado: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityContatosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.btnVoltar.setOnClickListener {
            finish()
        }

        window.statusBarColor = android.graphics.Color.parseColor("#FFF5F5")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        adapter = ContatoAdapter(emptyList()) { contatoId ->
            val usuarioId = usuarioIdLogado

            if (usuarioId != null) {
                viewModel.deletarContato(usuarioId, contatoId)
            } else {
                Toast.makeText(
                    this,
                    "Usuário não encontrado na sessão",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.rvContatos.layoutManager = LinearLayoutManager(this)
        binding.rvContatos.adapter = adapter

        viewModel.contatos.observe(this) { contatos ->
            if (contatos.isEmpty()) {
                binding.rvContatos.visibility = View.GONE
                binding.tvEmpty.visibility = View.VISIBLE
            } else {
                binding.rvContatos.visibility = View.VISIBLE
                binding.tvEmpty.visibility = View.GONE
                adapter.atualizar(contatos)
            }
        }

        viewModel.estado.observe(this) { estado ->
            when (estado) {
                is ContatoEstado.Sucesso -> {
                    Toast.makeText(this, "Contato cadastrado!", Toast.LENGTH_SHORT).show()
                }

                is ContatoEstado.Erro -> {
                    Toast.makeText(this, estado.message, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }

        binding.fabAdicionarContato.setOnClickListener {
            mostrarDialogCadastro()
        }

        lifecycleScope.launch {
            val usuarioId = sessionManager.getUserId()

            if (usuarioId == null || usuarioId == 0L) {
                Toast.makeText(
                    this@ContatosActivity,
                    "Usuário não encontrado na sessão. Faça login novamente.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
                return@launch
            }

            usuarioIdLogado = usuarioId
            viewModel.carregarContatos(usuarioId)
        }
    }

    private fun mostrarDialogCadastro() {
        val usuarioId = usuarioIdLogado

        if (usuarioId == null) {
            Toast.makeText(
                this,
                "Usuário não encontrado na sessão",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 20)
        }

        val etNome = TextInputEditText(this).apply {
            hint = "Nome completo"
        }

        val etTelefone = TextInputEditText(this).apply {
            hint = "Telefone (ex: 11999999999)"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
        }

        val etEmail = TextInputEditText(this).apply {
            hint = "E-mail (opcional)"
        }

        val spinner = Spinner(this)
        val tipos = listOf("Familiar", "Médico", "Amigo", "Cuidador", "Outro")
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            tipos
        )

        layout.addView(etNome)
        layout.addView(etTelefone)
        layout.addView(etEmail)
        layout.addView(spinner)

        AlertDialog.Builder(this)
            .setTitle("Adicionar contato de emergência")
            .setView(layout)
            .setPositiveButton("Cadastrar") { _, _ ->
                viewModel.cadastrarContato(
                    usuarioId = usuarioId,
                    nome = etNome.text.toString(),
                    telefone = etTelefone.text.toString(),
                    email = etEmail.text.toString(),
                    tipo = spinner.selectedItem.toString()
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}