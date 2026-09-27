package com.linhavital.app.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.linhavital.app.R
import com.linhavital.app.databinding.ActivityRegisterBinding
import com.linhavital.app.ui.common.clearErrorWhenEditing
import com.linhavital.app.ui.common.showFormError
import com.linhavital.app.ui.onboarding.OnboardingActivity
import com.linhavital.app.utils.applySystemBarsPadding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding:
            ActivityRegisterBinding

    private val viewModel:
            RegisterViewModel by viewModels()

    private var modoGoogle =
        false

    private var googleIdToken:
            String? = null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityRegisterBinding.inflate(
                layoutInflater
            )

        setContentView(
            binding.root
        )

        binding.tvFormError
            .clearErrorWhenEditing(
                binding.etName,
                binding.etEmail,
                binding.etPhone,
                binding.etBirthDate,
                binding.etPassword,
                binding.etConfirmPassword
            )

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        binding.rootRegister
            .applySystemBarsPadding(
                top = true,
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

        configurarModo()

        binding.btnVoltar
            .setOnClickListener {

                finish()
            }

        binding.tvLogin
            .setOnClickListener {

                finish()
            }

        binding.btnCadastrar
            .setOnClickListener {

                salvar()
            }

        observarCadastro()
    }

    private fun configurarModo() {

        modoGoogle =
            intent.getBooleanExtra(
                EXTRA_GOOGLE_MODE,
                false
            )

        if (!modoGoogle) {
            return
        }

        googleIdToken =
            intent.getStringExtra(
                EXTRA_GOOGLE_ID_TOKEN
            )

        binding.etName.setText(
            intent.getStringExtra(
                EXTRA_GOOGLE_NAME
            ).orEmpty()
        )

        binding.etEmail.setText(
            intent.getStringExtra(
                EXTRA_GOOGLE_EMAIL
            ).orEmpty()
        )

        binding.etName.isEnabled =
            false

        binding.etEmail.isEnabled =
            false

        binding.inputPassword.visibility =
            View.GONE

        binding.inputConfirmPassword.visibility =
            View.GONE

        binding.btnCadastrar.text =
            "Concluir cadastro com Google"

        binding.tvLogin.visibility =
            View.GONE
    }

    private fun salvar() {

        if (modoGoogle) {

            viewModel.registerGoogle(
                idToken =
                    googleIdToken.orEmpty(),

                phone =
                    binding.etPhone
                        .text
                        ?.toString()
                        .orEmpty(),

                birthDate =
                    binding.etBirthDate
                        .text
                        ?.toString()
                        .orEmpty()
            )

            return
        }

        viewModel.register(
            name =
                binding.etName
                    .text
                    ?.toString()
                    .orEmpty(),

            email =
                binding.etEmail
                    .text
                    ?.toString()
                    .orEmpty(),

            phone =
                binding.etPhone
                    .text
                    ?.toString()
                    .orEmpty(),

            birthDate =
                binding.etBirthDate
                    .text
                    ?.toString()
                    .orEmpty(),

            password =
                binding.etPassword
                    .text
                    ?.toString()
                    .orEmpty(),

            confirmPassword =
                binding.etConfirmPassword
                    .text
                    ?.toString()
                    .orEmpty()
        )
    }

    private fun observarCadastro() {

        viewModel.registerState
            .observe(this) { state ->

                when (state) {

                    RegisterState.Loading -> {

                        setLoading(
                            true
                        )
                    }

                    RegisterState.Success -> {

                        setLoading(
                            false
                        )

                        Toast
                            .makeText(
                                this,
                                "Cadastro realizado. Faça seu login.",
                                Toast.LENGTH_LONG
                            )
                            .show()

                        finish()
                    }

                    RegisterState.GoogleSuccess -> {

                        setLoading(
                            false
                        )

                        Toast
                            .makeText(
                                this,
                                "Cadastro Google concluído.",
                                Toast.LENGTH_SHORT
                            )
                            .show()

                        startActivity(
                            Intent(
                                this,
                                OnboardingActivity::class.java
                            ).apply {

                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                        )

                        finish()
                    }

                    is RegisterState.Error -> {

                        setLoading(
                            false
                        )

                        binding.tvFormError
                            .showFormError(
                                state.message
                            )
                    }
                }
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

        binding.btnCadastrar.isEnabled =
            !loading

        if (!modoGoogle) {

            binding.btnCadastrar.setText(
                if (loading) {
                    R.string.lv_creating_account
                } else {
                    R.string.lv_create_account
                }
            )

        } else {

            binding.btnCadastrar.text =
                if (loading) {
                    "Concluindo cadastro..."
                } else {
                    "Concluir cadastro com Google"
                }
        }

        if (loading) {

            binding.tvFormError
                .showFormError(null)
        }
    }

    companion object {

        private const val EXTRA_GOOGLE_MODE =
            "google_mode"

        private const val EXTRA_GOOGLE_ID_TOKEN =
            "google_id_token"

        private const val EXTRA_GOOGLE_NAME =
            "google_name"

        private const val EXTRA_GOOGLE_EMAIL =
            "google_email"

        fun intentGoogle(
            context: Context,
            idToken: String,
            nome: String,
            email: String
        ): Intent {

            return Intent(
                context,
                RegisterActivity::class.java
            ).apply {

                putExtra(
                    EXTRA_GOOGLE_MODE,
                    true
                )

                putExtra(
                    EXTRA_GOOGLE_ID_TOKEN,
                    idToken
                )

                putExtra(
                    EXTRA_GOOGLE_NAME,
                    nome
                )

                putExtra(
                    EXTRA_GOOGLE_EMAIL,
                    email
                )
            }
        }
    }
}