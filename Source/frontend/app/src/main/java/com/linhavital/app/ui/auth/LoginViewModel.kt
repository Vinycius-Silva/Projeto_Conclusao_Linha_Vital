package com.linhavital.app.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.linhavital.app.data.model.Usuario
import com.linhavital.app.data.repository.UsuarioRepository
import com.linhavital.app.utils.SessionManager
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UsuarioRepository()
    private val sessionManager = SessionManager(application)

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Preencha todos os campos")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _loginState.value = LoginState.Error("E-mail inválido")
            return
        }

        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            val result = repository.login(email, password)
            if (result.isSuccess) {
                val usuario = result.getOrNull()!!
                sessionManager.salvarSessao(
                    id = usuario.id ?: 0L,
                    nome = usuario.nomeUsuario,
                    email = usuario.emailUsuario
                )
                _loginState.postValue(LoginState.Success(usuario))
            } else {
                _loginState.postValue(LoginState.Error(result.exceptionOrNull()?.message ?: "Erro ao fazer login"))
            }
        }
    }
}

sealed class LoginState {
    object Loading : LoginState()
    data class Success(val usuario: Usuario) : LoginState()
    data class Error(val message: String) : LoginState()
}