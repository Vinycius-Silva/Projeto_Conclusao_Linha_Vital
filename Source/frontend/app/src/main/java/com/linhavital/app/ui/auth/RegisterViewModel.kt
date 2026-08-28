package com.linhavital.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linhavital.app.data.model.Usuario
import com.linhavital.app.data.repository.UsuarioRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class RegisterViewModel : ViewModel() {

    private val repository = UsuarioRepository()
    private val _registerState = MutableLiveData<RegisterState>()
    val registerState: LiveData<RegisterState> = _registerState

    fun register(
        name: String,
        email: String,
        phone: String,
        birthDate: String,
        password: String,
        confirmPassword: String
    ) {
        val nome = name.trim()
        val emailNormalizado = email.trim()
        val telefone = phone.filter(Char::isDigit)

        if (nome.isBlank() || emailNormalizado.isBlank() || telefone.isBlank() ||
            birthDate.isBlank() || password.isBlank() || confirmPassword.isBlank()
        ) {
            _registerState.value = RegisterState.Error("Preencha todos os campos")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailNormalizado).matches()) {
            _registerState.value = RegisterState.Error("E-mail inválido")
            return
        }
        if (telefone.length !in 10..13) {
            _registerState.value = RegisterState.Error("Telefone inválido")
            return
        }
        if (password.length < 6) {
            _registerState.value = RegisterState.Error("Senha deve ter no mínimo 6 caracteres")
            return
        }
        if (password != confirmPassword) {
            _registerState.value = RegisterState.Error("As senhas não coincidem")
            return
        }

        val dataIso = try {
            LocalDate.parse(birthDate.trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
        } catch (_: DateTimeParseException) {
            _registerState.value = RegisterState.Error("Use a data no formato DD/MM/AAAA")
            return
        }

        _registerState.value = RegisterState.Loading
        viewModelScope.launch {
            val result = repository.cadastrar(
                Usuario(
                    nomeUsuario = nome,
                    emailUsuario = emailNormalizado,
                    senhaUsuario = password,
                    telefoneUsuario = telefone,
                    dataNascimento = dataIso
                )
            )
            _registerState.postValue(
                if (result.isSuccess) RegisterState.Success
                else RegisterState.Error(result.exceptionOrNull()?.message ?: "Erro ao cadastrar")
            )
        }
    }
}

sealed class RegisterState {
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}
