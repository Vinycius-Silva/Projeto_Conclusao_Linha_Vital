package com.linhavital.app.ui.home

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.linhavital.app.R
import com.linhavital.app.data.model.ContatoEmergencia
import com.linhavital.app.databinding.ActivityContatosBinding
import com.linhavital.app.ui.common.NavigationTab
import com.linhavital.app.ui.common.selectTab
import com.linhavital.app.ui.common.showFormError
import com.linhavital.app.utils.SessionManager
import com.linhavital.app.utils.applySystemBarsPadding
import kotlinx.coroutines.launch

class ContatosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContatosBinding

    private val viewModel: ContatoViewModel by viewModels()

    private lateinit var adapter: ContatoAdapter

    private lateinit var itemTouchHelper: ItemTouchHelper

    private lateinit var sessionManager: SessionManager

    private var usuarioIdLogado: Long? = null

    private var numeroPendenteLigacao: String? = null

    companion object {
        private const val REQUEST_CALL_PHONE = 102
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        binding =
            ActivityContatosBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        binding.root.applySystemBarsPadding(
            left = true,
            right = true
        )

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        binding.headerContatos.applySystemBarsPadding(
            top = true
        )

        binding
            .bottomNavigation
            .bottomNavigationContainer
            .applySystemBarsPadding(
                bottom = true
            )

        window.statusBarColor =
            ContextCompat.getColor(
                this,
                R.color.lv_background
            )

        WindowCompat
            .getInsetsController(
                window,
                window.decorView
            )
            .isAppearanceLightStatusBars =
            true

        sessionManager =
            SessionManager(
                this
            )

        binding.btnVoltar.setOnClickListener {
            finish()
        }

        binding.fabAdicionarContato.setOnClickListener {
            abrirFormulario(
                null
            )
        }

        configurarLista()

        observarViewModel()

        configurarBottomBar()

        lifecycleScope.launch {

            val id =
                sessionManager.getUserId()

            if (
                id == null ||
                id == 0L
            ) {

                Toast.makeText(
                    this@ContatosActivity,
                    "Sessão inválida. Faça login novamente.",
                    Toast.LENGTH_LONG
                ).show()

                finish()

            } else {

                usuarioIdLogado =
                    id

                showListLoading()

                viewModel.carregarContatos(
                    id
                )
            }
        }
    }

    private fun configurarLista() {

        adapter =
            ContatoAdapter(
                contatosIniciais =
                    emptyList(),

                onLigar =
                    ::ligarParaContato,

                onEditar =
                    ::abrirFormulario,

                onDeletar =
                    ::confirmarExclusao,

                onIniciarArraste = { viewHolder ->

                    itemTouchHelper.startDrag(
                        viewHolder
                    )
                }
            )

        binding.rvContatos.layoutManager =
            LinearLayoutManager(
                this
            )

        binding.rvContatos.adapter =
            adapter

        binding.rvContatos.itemAnimator =
            if (
                ValueAnimator.areAnimatorsEnabled()
            ) {

                DefaultItemAnimator().apply {

                    addDuration =
                        180L

                    removeDuration =
                        180L

                    moveDuration =
                        180L

                    supportsChangeAnimations =
                        false
                }

            } else {

                null
            }

        configurarReordenacao()
    }

    private fun configurarReordenacao() {

        itemTouchHelper =
            ItemTouchHelper(
                object :
                    ItemTouchHelper.SimpleCallback(
                        ItemTouchHelper.UP or
                                ItemTouchHelper.DOWN,
                        0
                    ) {

                    private var ordemAlterada =
                        false

                    override fun isLongPressDragEnabled():
                            Boolean =
                        false

                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {

                        val origem =
                            viewHolder.adapterPosition

                        val destino =
                            target.adapterPosition

                        if (
                            origem ==
                            RecyclerView.NO_POSITION ||
                            destino ==
                            RecyclerView.NO_POSITION
                        ) {

                            return false
                        }

                        val moveu =
                            adapter.moverItem(
                                origem =
                                    origem,

                                destino =
                                    destino
                            )

                        if (
                            moveu
                        ) {

                            ordemAlterada =
                                true
                        }

                        return moveu
                    }

                    override fun onSwiped(
                        viewHolder: RecyclerView.ViewHolder,
                        direction: Int
                    ) {
                        // Swipe não é utilizado.
                    }

                    override fun onSelectedChanged(
                        viewHolder: RecyclerView.ViewHolder?,
                        actionState: Int
                    ) {

                        super.onSelectedChanged(
                            viewHolder,
                            actionState
                        )

                        if (
                            actionState ==
                            ItemTouchHelper.ACTION_STATE_DRAG &&
                            viewHolder != null
                        ) {

                            viewHolder.itemView.apply {

                                alpha =
                                    0.92f

                                scaleX =
                                    1.02f

                                scaleY =
                                    1.02f
                            }
                        }
                    }

                    override fun clearView(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder
                    ) {

                        super.clearView(
                            recyclerView,
                            viewHolder
                        )

                        viewHolder.itemView.apply {

                            alpha =
                                1f

                            scaleX =
                                1f

                            scaleY =
                                1f
                        }

                        adapter.finalizarReordenacao()

                        if (
                            !ordemAlterada
                        ) {

                            return
                        }

                        ordemAlterada =
                            false

                        val usuarioId =
                            usuarioIdLogado
                                ?: return

                        viewModel.reordenarContatos(
                            usuarioId =
                                usuarioId,

                            contatosOrdenados =
                                adapter.contatosAtuais()
                        )
                    }
                }
            )

        itemTouchHelper.attachToRecyclerView(
            binding.rvContatos
        )
    }

    private fun observarViewModel() {

        viewModel.contatos.observe(
            this
        ) { contatos ->

            binding.progressContatos.visibility =
                View.INVISIBLE

            binding.tvListError.showFormError(
                null
            )

            binding.rvContatos.visibility =
                if (
                    contatos.isEmpty()
                ) {

                    View.GONE

                } else {

                    View.VISIBLE
                }

            binding.tvEmpty.visibility =
                if (
                    contatos.isEmpty()
                ) {

                    View.VISIBLE

                } else {

                    View.GONE
                }

            /*
             * Em algumas configurações de layout o ViewBinding
             * gera esta View como nullable, portanto utilizamos
             * safe call para manter compatibilidade.
             */
            binding.tvOrdemAjuda?.visibility =
                if (
                    contatos.size >= 2
                ) {

                    View.VISIBLE

                } else {

                    View.GONE
                }

            adapter.atualizar(
                contatos
            )
        }

        viewModel.estado.observe(
            this
        ) { estado ->

            when (estado) {

                is ContatoEstado.Erro -> {

                    binding.progressContatos.visibility =
                        View.INVISIBLE

                    binding.tvListError.showFormError(
                        estado.message
                    )
                }

                is ContatoEstado.ErroReordenacao -> {

                    Toast.makeText(
                        this,
                        estado.message,
                        Toast.LENGTH_LONG
                    ).show()
                }

                ContatoEstado.OrdemAtualizada -> {

                    Toast.makeText(
                        this,
                        "Prioridade dos contatos atualizada.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> Unit
            }
        }
    }

    override fun onResume() {

        super.onResume()

        usuarioIdLogado
            ?.let { id ->

                showListLoading()

                viewModel.carregarContatos(
                    id
                )
            }
    }

    private fun showListLoading() {

        binding.progressContatos.visibility =
            View.VISIBLE

        binding.tvEmpty.visibility =
            View.GONE

        binding.tvListError.showFormError(
            null
        )
    }

    private fun configurarBottomBar() {

        binding.bottomNavigation.selectTab(
            NavigationTab.CONTATOS
        )

        binding
            .bottomNavigation
            .btnNavHome
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        HomeActivity::class.java
                    ).apply {

                        flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                )

                finish()
            }

        binding
            .bottomNavigation
            .btnNavContatos
            .setOnClickListener {
                // Já estamos na tela de contatos.
            }

        binding
            .bottomNavigation
            .btnNavCriterios
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        CriteriosActivity::class.java
                    )
                )

                finish()
            }
    }

    private fun abrirFormulario(
        contato: ContatoEmergencia?
    ) {

        startActivity(
            ContatoFormActivity.intent(
                this,
                contato
            )
        )
    }

    private fun confirmarExclusao(
        contatoId: Long
    ) {

        MaterialAlertDialogBuilder(
            this
        )
            .setTitle(
                "Excluir contato?"
            )
            .setMessage(
                "Esse contato deixará de fazer parte da sua rede de proteção."
            )
            .setNegativeButton(
                "Cancelar",
                null
            )
            .setPositiveButton(
                "Excluir"
            ) { _, _ ->

                usuarioIdLogado
                    ?.let { usuarioId ->

                        viewModel.deletarContato(
                            usuarioId,
                            contatoId
                        )
                    }
            }
            .show()
    }

    private fun ligarParaContato(
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
                "Número inválido.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        numeroPendenteLigacao =
            numero

        if (
            ContextCompat.checkSelfPermission(
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
                    numero.length >= 12 ->

                "+$numero"

            numero.length in 10..11 ->

                "+55$numero"

            else ->

                ""
        }
    }

    private fun iniciarLigacao(
        numero: String
    ) {

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

            Toast.makeText(
                this,
                "Não foi possível iniciar a ligação.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

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

        if (
            requestCode ==
            REQUEST_CALL_PHONE &&
            grantResults.firstOrNull() ==
            PackageManager.PERMISSION_GRANTED
        ) {

            numeroPendenteLigacao
                ?.let(
                    ::iniciarLigacao
                )
        }
    }
}