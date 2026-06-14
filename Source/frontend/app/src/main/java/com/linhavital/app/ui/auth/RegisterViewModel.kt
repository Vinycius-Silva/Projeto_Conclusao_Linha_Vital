package com.linhavital.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linhavital.app.data.model.Usuario
import com.linhavital.app.data.repository.UsuarioRepository
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val repository = UsuarioRepository()

    private val _registerState = MutableLiveData<RegisterState>()
    val registerState: LiveData<RegisterState> = _registerState

    fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {

        if (
            email.isBlank() ||
            password.isBlank() ||
            confirmPassword.isBlank()
        ) {

            _registerState.value =
                RegisterState.Error("Preencha todos os campos")

            return
        }

        if (
            !android.util.Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()
        ) {

            _registerState.value =
                RegisterState.Error("E-mail inválido")

            return
        }

        if (password.length < 6) {

            _registerState.value =
                RegisterState.Error(
                    "Senha deve ter no mínimo 6 caracteres"
                )

            return
        }

        if (password != confirmPassword) {

            _registerState.value =
                RegisterState.Error(
                    "As senhas não coincidem"
                )

            return
        }

        _registerState.value = RegisterState.Loading

        viewModelScope.launch {

            val usuario = Usuario(
                nomeUsuario = "Usuário",
                emailUsuario = email,
                senhaUsuario = password,
                telefoneUsuario = "00000000000",
                dataNascimento = "2000-01-01"
            )

            val result = repository.cadastrar(usuario)

            if (result.isSuccess) {

                _registerState.postValue(
                    RegisterState.Success
                )

            } else {

                _registerState.postValue(
                    RegisterState.Error(
                        result.exceptionOrNull()?.message
                            ?: "Erro ao cadastrar"
                    )
                )
            }
        }
    }
}

sealed class RegisterState {

    object Loading : RegisterState()

    object Success : RegisterState()

    data class Error(
        val message: String
    ) : RegisterState()
}