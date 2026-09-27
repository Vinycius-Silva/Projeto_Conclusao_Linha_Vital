package com.linhavital.app.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.linhavital.app.R
import com.linhavital.app.data.model.ContatoEmergencia
import com.linhavital.app.databinding.ActivityContatoFormBinding
import com.linhavital.app.ui.common.accentLastWord
import com.linhavital.app.ui.common.clearErrorWhenEditing
import com.linhavital.app.ui.common.showFormError
import com.linhavital.app.utils.SessionManager
import com.linhavital.app.utils.applySystemBarsPadding
import kotlinx.coroutines.launch

class ContatoFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContatoFormBinding

    private val viewModel: ContatoViewModel by viewModels()

    private var usuarioId: Long? = null

    private var contatoId: Long? = null

    private val tipos = listOf(
        "Emergência",
        "Confiança",
        "Familiar",
        "Médico",
        "Amigo",
        "Cuidador",
        "Outro"
    )

    /*
     * Abre o seletor nativo do Android.
     *
     * Não é necessário solicitar READ_CONTACTS porque
     * o próprio usuário escolhe explicitamente qual
     * telefone deseja compartilhar com o Linha Vital.
     */
    private val seletorContato =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { resultado ->

            if (
                resultado.resultCode != RESULT_OK
            ) {
                return@registerForActivityResult
            }

            val uri =
                resultado.data?.data
                    ?: return@registerForActivityResult

            importarContatoSelecionado(
                uri
            )
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityContatoFormBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        binding.tvFormError
            .clearErrorWhenEditing(
                binding.etNome,
                binding.etTelefone,
                binding.etEmail
            )

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        binding.headerContatoForm
            .applySystemBarsPadding(
                top = true
            )

        binding.rootContatoForm
            .applySystemBarsPadding(
                bottom = true,
                left = true,
                right = true,
                ime = true
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

        contatoId =
            intent.getLongExtra(
                EXTRA_ID,
                0L
            ).takeIf {
                it > 0
            }

        binding.tvTitle.text =
            if (contatoId == null) {
                "Adicionar contato"
            } else {
                "Editar contato"
            }

        binding.tvTitle
            .accentLastWord()

        binding.etNome.setText(
            intent.getStringExtra(
                EXTRA_NOME
            ).orEmpty()
        )

        binding.etTelefone.setText(
            intent.getStringExtra(
                EXTRA_TELEFONE
            ).orEmpty()
        )

        binding.etEmail.setText(
            intent.getStringExtra(
                EXTRA_EMAIL
            ).orEmpty()
        )

        binding.spinnerTipo.adapter =
            ArrayAdapter(
                this,
                R.layout.item_spinner,
                tipos
            ).apply {

                setDropDownViewResource(
                    R.layout.item_spinner_dropdown
                )
            }

        intent
            .getStringExtra(
                EXTRA_TIPO
            )
            ?.let { tipoAtual ->

                tipos
                    .indexOf(tipoAtual)
                    .takeIf {
                        it >= 0
                    }
                    ?.let(
                        binding.spinnerTipo::setSelection
                    )
            }

        binding.btnVoltar
            .setOnClickListener {

                finish()
            }

        binding.btnImportarAgenda
            .setOnClickListener {

                abrirAgenda()
            }

        binding.btnSalvar
            .setOnClickListener {

                salvar()
            }

        /*
         * Na edição de um contato já existente,
         * escondemos a importação para evitar substituir
         * acidentalmente nome e telefone.
         */
        binding.btnImportarAgenda.visibility =
            if (contatoId == null) {
                View.VISIBLE
            } else {
                View.GONE
            }

        lifecycleScope.launch {

            usuarioId =
                SessionManager(
                    this@ContatoFormActivity
                ).getUserId()

            if (usuarioId == null) {

                Toast
                    .makeText(
                        this@ContatoFormActivity,
                        "Sessão inválida.",
                        Toast.LENGTH_LONG
                    )
                    .show()

                finish()
            }
        }

        viewModel.estado
            .observe(this) { estado ->

                when (estado) {

                    ContatoEstado.Loading -> {

                        setLoading(
                            true
                        )
                    }

                    ContatoEstado.Sucesso -> {

                        Toast
                            .makeText(
                                this,
                                "Contato adicionado.",
                                Toast.LENGTH_SHORT
                            )
                            .show()

                        finish()
                    }

                    ContatoEstado.Atualizado -> {

                        Toast
                            .makeText(
                                this,
                                "Contato atualizado.",
                                Toast.LENGTH_SHORT
                            )
                            .show()

                        finish()
                    }

                    is ContatoEstado.Erro -> {

                        setLoading(
                            false
                        )

                        binding.tvFormError
                            .showFormError(
                                estado.message
                            )
                    }
                }
            }
    }

    private fun abrirAgenda() {

        val intent =
            Intent(
                Intent.ACTION_PICK,
                ContactsContract
                    .CommonDataKinds
                    .Phone
                    .CONTENT_URI
            )

        runCatching {

            seletorContato.launch(
                intent
            )

        }.onFailure {

            Toast
                .makeText(
                    this,
                    "Não foi possível abrir a agenda deste dispositivo.",
                    Toast.LENGTH_LONG
                )
                .show()
        }
    }

    private fun importarContatoSelecionado(
        uri: Uri
    ) {

        val campos =
            arrayOf(
                ContactsContract
                    .CommonDataKinds
                    .Phone
                    .DISPLAY_NAME,

                ContactsContract
                    .CommonDataKinds
                    .Phone
                    .NUMBER
            )

        runCatching {

            contentResolver
                .query(
                    uri,
                    campos,
                    null,
                    null,
                    null
                )
                ?.use { cursor ->

                    if (
                        !cursor.moveToFirst()
                    ) {
                        return@use
                    }

                    val indiceNome =
                        cursor.getColumnIndex(
                            ContactsContract
                                .CommonDataKinds
                                .Phone
                                .DISPLAY_NAME
                        )

                    val indiceTelefone =
                        cursor.getColumnIndex(
                            ContactsContract
                                .CommonDataKinds
                                .Phone
                                .NUMBER
                        )

                    val nome =
                        if (indiceNome >= 0) {

                            cursor
                                .getString(
                                    indiceNome
                                )
                                .orEmpty()
                                .trim()

                        } else {

                            ""
                        }

                    val telefone =
                        if (indiceTelefone >= 0) {

                            cursor
                                .getString(
                                    indiceTelefone
                                )
                                .orEmpty()
                                .trim()

                        } else {

                            ""
                        }

                    if (nome.isNotBlank()) {

                        binding.etNome
                            .setText(
                                nome
                            )
                    }

                    if (telefone.isNotBlank()) {

                        binding.etTelefone
                            .setText(
                                telefone
                            )
                    }

                    if (
                        nome.isBlank() &&
                        telefone.isBlank()
                    ) {

                        Toast
                            .makeText(
                                this,
                                "Não foi possível obter os dados desse contato.",
                                Toast.LENGTH_LONG
                            )
                            .show()

                    } else {

                        Toast
                            .makeText(
                                this,
                                "Contato importado da agenda.",
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    }
                }

        }.onFailure {

            Toast
                .makeText(
                    this,
                    "Não foi possível importar esse contato.",
                    Toast.LENGTH_LONG
                )
                .show()
        }
    }

    private fun salvar() {

        val idUsuario =
            usuarioId
                ?: return

        val nome =
            binding.etNome
                .text
                ?.toString()
                .orEmpty()
                .trim()

        val telefone =
            binding.etTelefone
                .text
                ?.toString()
                .orEmpty()
                .trim()

        val email =
            binding.etEmail
                .text
                ?.toString()
                .orEmpty()
                .trim()

        val tipo =
            binding.spinnerTipo
                .selectedItem
                ?.toString()
                .orEmpty()

        val id =
            contatoId

        if (id == null) {

            viewModel.cadastrarContato(
                idUsuario,
                nome,
                telefone,
                email,
                tipo
            )

        } else {

            viewModel.atualizarContato(
                idUsuario,
                id,
                nome,
                telefone,
                email,
                tipo
            )
        }
    }

    private fun setLoading(
        loading: Boolean
    ) {

        binding.progressBar.visibility =
            if (loading) {
                View.VISIBLE
            } else {
                View.GONE
            }

        binding.btnSalvar.isEnabled =
            !loading

        binding.btnImportarAgenda.isEnabled =
            !loading

        binding.btnSalvar.setText(
            if (loading) {
                R.string.lv_saving
            } else {
                R.string.lv_save
            }
        )

        if (loading) {

            binding.tvFormError
                .showFormError(null)
        }
    }

    companion object {

        private const val EXTRA_ID =
            "contato_id"

        private const val EXTRA_NOME =
            "contato_nome"

        private const val EXTRA_TELEFONE =
            "contato_telefone"

        private const val EXTRA_EMAIL =
            "contato_email"

        private const val EXTRA_TIPO =
            "contato_tipo"

        fun intent(
            context: Context,
            contato: ContatoEmergencia?
        ): Intent =
            Intent(
                context,
                ContatoFormActivity::class.java
            ).apply {

                contato?.let {

                    putExtra(
                        EXTRA_ID,
                        it.id ?: 0L
                    )

                    putExtra(
                        EXTRA_NOME,
                        it.nome
                    )

                    putExtra(
                        EXTRA_TELEFONE,
                        it.telefone
                    )

                    putExtra(
                        EXTRA_EMAIL,
                        it.email
                    )

                    putExtra(
                        EXTRA_TIPO,
                        it.tipoContato
                    )
                }
            }
    }
}