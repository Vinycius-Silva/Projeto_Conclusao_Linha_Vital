package com.linhavital.app.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.linhavital.app.data.model.UsuarioSessao
import com.linhavital.app.data.repository.UsuarioRepository
import com.linhavital.app.utils.SessionManager
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UsuarioRepository()
    private val sessionManager = SessionManager(application)

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun login(email: String, password: String) {
        val emailNormalizado = email.trim()
        if (emailNormalizado.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Preencha e-mail e senha")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailNormalizado).matches()) {
            _loginState.value = LoginState.Error("E-mail inválido")
            return
        }

        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            val result = repository.login(emailNormalizado, password)
            if (result.isSuccess) {
                val usuario = result.getOrThrow()
                sessionManager.salvarSessao(usuario.id, usuario.nome, usuario.email)
                _loginState.postValue(LoginState.Success(usuario))
            } else {
                _loginState.postValue(
                    LoginState.Error(result.exceptionOrNull()?.message ?: "Erro ao fazer login")
                )
            }
        }
    }
}

sealed class LoginState {
    object Loading : LoginState()
    data class Success(val usuario: UsuarioSessao) : LoginState()
    data class Error(val message: String) : LoginState()
}
