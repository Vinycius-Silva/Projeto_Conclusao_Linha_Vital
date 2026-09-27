package com.linhavital.app.ui.auth
import com.linhavital.app.ui.common.showFormError
import com.linhavital.app.ui.common.clearErrorWhenEditing
import com.linhavital.app.R
import androidx.core.content.ContextCompat

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.linhavital.app.databinding.ActivityRegisterBinding
import com.linhavital.app.utils.applySystemBarsPadding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvFormError.clearErrorWhenEditing(binding.etName, binding.etEmail, binding.etPhone, binding.etBirthDate, binding.etPassword, binding.etConfirmPassword)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding.rootRegister.applySystemBarsPadding(top = true, bottom = true, left = true, right = true, ime = true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.lv_background)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        binding.btnVoltar.setOnClickListener { finish() }
        binding.tvLogin.setOnClickListener { finish() }
        binding.btnCadastrar.setOnClickListener {
            viewModel.register(
                name = binding.etName.text?.toString().orEmpty(),
                email = binding.etEmail.text?.toString().orEmpty(),
                phone = binding.etPhone.text?.toString().orEmpty(),
                birthDate = binding.etBirthDate.text?.toString().orEmpty(),
                password = binding.etPassword.text?.toString().orEmpty(),
                confirmPassword = binding.etConfirmPassword.text?.toString().orEmpty()
            )
        }

        viewModel.registerState.observe(this) { state ->
            when (state) {
                RegisterState.Loading -> setLoading(true)
                RegisterState.Success -> {
                    setLoading(false)
                    Toast.makeText(this, "Cadastro realizado. Faça seu login.", Toast.LENGTH_LONG).show()
                    finish()
                }
                is RegisterState.Error -> {
                    setLoading(false)
                    binding.tvFormError.showFormError(state.message)
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnCadastrar.isEnabled = !loading
        binding.btnCadastrar.setText(if (loading) R.string.lv_creating_account else R.string.lv_create_account)
        if (loading) binding.tvFormError.showFormError(null)
    }
}
