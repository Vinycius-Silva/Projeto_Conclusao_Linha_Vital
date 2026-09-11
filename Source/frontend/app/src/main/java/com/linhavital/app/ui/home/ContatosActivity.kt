package com.linhavital.app.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.linhavital.app.R
import com.linhavital.app.data.model.ContatoEmergencia
import com.linhavital.app.databinding.ActivityContatosBinding
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
        binding.bottomNavigation.bottomNavigationContainer.applySystemBarsPadding(bottom = true)
        window.statusBarColor = android.graphics.Color.parseColor("#FFF5F5")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        sessionManager = SessionManager(this)
        binding.btnVoltar.setOnClickListener { finish() }
        binding.fabAdicionarContato.setOnClickListener { abrirFormulario(null) }

        adapter = ContatoAdapter(
            emptyList(),
            onLigar = ::ligarParaContato,
            onEditar = ::abrirFormulario,
            onDeletar = ::confirmarExclusao
        )
        binding.rvContatos.layoutManager = LinearLayoutManager(this)
        binding.rvContatos.adapter = adapter

        viewModel.contatos.observe(this) { contatos ->
            binding.rvContatos.visibility = if (contatos.isEmpty()) View.GONE else View.VISIBLE
            binding.tvEmpty.visibility = if (contatos.isEmpty()) View.VISIBLE else View.GONE
            adapter.atualizar(contatos)
        }
        viewModel.estado.observe(this) { estado ->
            if (estado is ContatoEstado.Erro) {
                Toast.makeText(this, estado.message, Toast.LENGTH_LONG).show()
            }
        }

        configurarBottomBar()
        lifecycleScope.launch {
            val id = sessionManager.getUserId()
            if (id == null || id == 0L) {
                Toast.makeText(this@ContatosActivity, "Sessão inválida. Faça login novamente.", Toast.LENGTH_LONG).show()
                finish()
            } else {
                usuarioIdLogado = id
                viewModel.carregarContatos(id)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        usuarioIdLogado?.let(viewModel::carregarContatos)
    }

    private fun configurarBottomBar() {
        binding.bottomNavigation.btnNavContatos.setBackgroundResource(R.drawable.nav_item_active)
        binding.bottomNavigation.iconNavContatos.setColorFilter(android.graphics.Color.parseColor("#BB0013"))
        binding.bottomNavigation.labelNavContatos.setTextColor(android.graphics.Color.parseColor("#BB0013"))

        binding.bottomNavigation.btnNavHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
            finish()
        }
        binding.bottomNavigation.btnNavContatos.setOnClickListener { }
        binding.bottomNavigation.btnNavCriterios.setOnClickListener {
            startActivity(Intent(this, CriteriosActivity::class.java))
            finish()
        }
    }

    private fun abrirFormulario(contato: ContatoEmergencia?) {
        startActivity(ContatoFormActivity.intent(this, contato))
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
        val numero = formatarNumero(numeroOriginal)
        if (numero.isBlank()) {
            Toast.makeText(this, "Número inválido.", Toast.LENGTH_SHORT).show()
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
