package com.linhavital.app.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.linhavital.app.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor =
            android.graphics.Color.parseColor("#FFF5F5")

        WindowCompat.getInsetsController(
            window,
            window.decorView
        ).isAppearanceLightStatusBars = true

        binding.btnVoltar.setOnClickListener {
            finish()
        }

        binding.btnCadastrar.setOnClickListener {

            val email =
                binding.etEmail.text.toString().trim()

            val password =
                binding.etPassword.text.toString().trim()

            val confirmPassword =
                binding.etConfirmPassword.text.toString().trim()

            viewModel.register(
                "",
                email,
                password,
                confirmPassword
            )
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }

        viewModel.registerState.observe(this) { state ->

            when (state) {

                is RegisterState.Loading -> {

                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnCadastrar.isEnabled = false
                }

                is RegisterState.Success -> {

                    binding.progressBar.visibility = View.GONE

                    Toast.makeText(
                        this,
                        "Cadastro realizado!",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                }

                is RegisterState.Error -> {

                    binding.progressBar.visibility = View.GONE
                    binding.btnCadastrar.isEnabled = true

                    Toast.makeText(
                        this,
                        state.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}