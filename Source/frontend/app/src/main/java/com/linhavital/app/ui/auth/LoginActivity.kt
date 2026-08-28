package com.linhavital.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.linhavital.app.databinding.ActivityLoginBinding
import com.linhavital.app.ui.home.HomeActivity
import com.linhavital.app.ui.onboarding.OnboardingActivity
import com.linhavital.app.utils.SessionManager
import com.linhavital.app.utils.applySystemBarsPadding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding.rootLogin.applySystemBarsPadding(top = true, bottom = true)
        window.statusBarColor = android.graphics.Color.parseColor("#FFF5F5")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        sessionManager = SessionManager(this)
        lifecycleScope.launch {
            if (sessionManager.isLoggedIn()) {
                abrirPosLogin()
            }
        }

        binding.btnLogin.setOnClickListener {
            viewModel.login(
                binding.etEmail.text?.toString().orEmpty(),
                binding.etPassword.text?.toString().orEmpty()
            )
        }
        binding.tvCadastro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.tvEsqueciSenha.visibility = View.GONE

        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Loading -> setLoading(true)
                is LoginState.Success -> {
                    setLoading(false)
                    lifecycleScope.launch { abrirPosLogin() }
                }
                is LoginState.Error -> {
                    setLoading(false)
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun abrirPosLogin() {
        val destino = if (sessionManager.hasCompletedOnboarding()) {
            HomeActivity::class.java
        } else {
            OnboardingActivity::class.java
        }
        startActivity(Intent(this, destino))
        finish()
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
    }
}
