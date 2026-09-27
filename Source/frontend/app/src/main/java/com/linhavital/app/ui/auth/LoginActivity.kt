package com.linhavital.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.linhavital.app.BuildConfig
import com.linhavital.app.R
import com.linhavital.app.databinding.ActivityLoginBinding
import com.linhavital.app.ui.common.clearErrorWhenEditing
import com.linhavital.app.ui.common.showFormError
import com.linhavital.app.ui.home.HomeActivity
import com.linhavital.app.ui.onboarding.OnboardingActivity
import com.linhavital.app.utils.SessionManager
import com.linhavital.app.utils.applySystemBarsPadding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val viewModel: LoginViewModel by viewModels()

    private lateinit var sessionManager: SessionManager

    private lateinit var credentialManager: CredentialManager

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityLoginBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        binding.tvFormError
            .clearErrorWhenEditing(
                binding.etEmail,
                binding.etPassword
            )

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        binding.rootLogin
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

        sessionManager =
            SessionManager(this)

        credentialManager =
            CredentialManager.create(this)

        lifecycleScope.launch {

            if (
                sessionManager.isLoggedIn()
            ) {
                abrirPosLogin()
            }
        }

        binding.btnLogin
            .setOnClickListener {

                viewModel.login(
                    binding.etEmail
                        .text
                        ?.toString()
                        .orEmpty(),

                    binding.etPassword
                        .text
                        ?.toString()
                        .orEmpty()
                )
            }

        binding.btnGoogle
            .setOnClickListener {

                iniciarLoginGoogle()
            }

        binding.tvCadastro
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        RegisterActivity::class.java
                    )
                )
            }

        binding.tvEsqueciSenha.visibility =
            View.GONE

        observarLogin()
    }

    private fun observarLogin() {

        viewModel.loginState
            .observe(this) { state ->

                when (state) {

                    LoginState.Loading -> {

                        setLoading(true)
                    }

                    is LoginState.Success -> {

                        setLoading(false)

                        lifecycleScope.launch {

                            abrirPosLogin()
                        }
                    }

                    is LoginState.GoogleCadastroNecessario -> {

                        setLoading(false)

                        startActivity(
                            RegisterActivity
                                .intentGoogle(
                                    context = this,
                                    idToken = state.idToken,
                                    nome = state.nome,
                                    email = state.email
                                )
                        )
                    }

                    is LoginState.Error -> {

                        setLoading(false)

                        binding.tvFormError
                            .showFormError(
                                state.message
                            )
                    }
                }
            }
    }

    private fun iniciarLoginGoogle() {

        binding.tvFormError
            .showFormError(null)

        val clientId =
            BuildConfig.GOOGLE_WEB_CLIENT_ID

        if (clientId.isBlank()) {

            binding.tvFormError
                .showFormError(
                    "GOOGLE_WEB_CLIENT_ID não configurado."
                )

            return
        }

        binding.btnGoogle.isEnabled =
            false

        val googleOption =
            GetSignInWithGoogleOption
                .Builder(
                    clientId
                )
                .build()

        val request =
            GetCredentialRequest
                .Builder()
                .addCredentialOption(
                    googleOption
                )
                .build()

        lifecycleScope.launch {

            try {

                val result =
                    credentialManager
                        .getCredential(
                            context =
                                this@LoginActivity,

                            request =
                                request
                        )

                val credential =
                    result.credential

                if (
                    credential
                            !is CustomCredential
                ) {

                    binding.tvFormError
                        .showFormError(
                            "Credencial Google não reconhecida."
                        )

                    binding.btnGoogle.isEnabled =
                        true

                    return@launch
                }

                if (
                    credential.type !=
                    GoogleIdTokenCredential
                        .TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {

                    binding.tvFormError
                        .showFormError(
                            "Tipo de credencial Google não reconhecido."
                        )

                    binding.btnGoogle.isEnabled =
                        true

                    return@launch
                }

                val googleCredential =
                    GoogleIdTokenCredential
                        .createFrom(
                            credential.data
                        )

                viewModel.loginGoogle(
                    googleCredential.idToken
                )

            } catch (
                _: GetCredentialCancellationException
            ) {

                binding.btnGoogle.isEnabled =
                    true

            } catch (
                _: NoCredentialException
            ) {

                binding.btnGoogle.isEnabled =
                    true

                binding.tvFormError
                    .showFormError(
                        "Nenhuma conta Google foi encontrada neste dispositivo."
                    )

            } catch (
                _: GoogleIdTokenParsingException
            ) {

                binding.btnGoogle.isEnabled =
                    true

                binding.tvFormError
                    .showFormError(
                        "Não foi possível interpretar a conta Google."
                    )

            } catch (
                exception: GetCredentialException
            ) {

                binding.btnGoogle.isEnabled =
                    true

                binding.tvFormError
                    .showFormError(
                        exception.message
                            ?: "Não foi possível abrir o login do Google."
                    )

            } catch (
                exception: Exception
            ) {

                binding.btnGoogle.isEnabled =
                    true

                binding.tvFormError
                    .showFormError(
                        exception.message
                            ?: "Erro ao entrar com Google."
                    )
            }
        }
    }

    private suspend fun abrirPosLogin() {

        val destino =
            if (
                sessionManager
                    .hasCompletedOnboarding()
            ) {

                HomeActivity::class.java

            } else {

                OnboardingActivity::class.java
            }

        startActivity(
            Intent(
                this,
                destino
            )
        )

        finish()
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

        binding.btnLogin.isEnabled =
            !loading

        binding.btnGoogle.isEnabled =
            !loading

        binding.tvCadastro.isEnabled =
            !loading

        binding.btnLogin.setText(
            if (loading) {
                R.string.lv_signing_in
            } else {
                R.string.lv_login
            }
        )

        if (loading) {

            binding.tvFormError
                .showFormError(null)
        }
    }
}