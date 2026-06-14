package com.linhavital.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.linhavital.app.databinding.ActivityLoginBinding
import com.linhavital.app.ui.home.HomeActivity
import com.linhavital.app.utils.SessionManager
import kotlinx.coroutines.launch
import androidx.core.view.WindowCompat

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = android.graphics.Color.parseColor("#FAFAFA")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        sessionManager = SessionManager(this)

        lifecycleScope.launch {
            if (sessionManager.isLoggedIn()) {
                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                finish()
                return@launch
            }
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.login(email, password)
        }

        binding.tvCadastro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                }
                is LoginState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                is LoginState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}