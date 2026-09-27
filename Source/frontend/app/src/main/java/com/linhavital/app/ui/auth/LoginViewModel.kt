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

class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        UsuarioRepository()

    private val sessionManager =
        SessionManager(application)

    private val _loginState =
        MutableLiveData<LoginState>()

    val loginState: LiveData<LoginState> =
        _loginState

    fun login(
        email: String,
        password: String
    ) {
        val emailNormalizado =
            email.trim()

        if (
            emailNormalizado.isBlank() ||
            password.isBlank()
        ) {
            _loginState.value =
                LoginState.Error(
                    "Preencha e-mail e senha"
                )

            return
        }

        if (
            !android.util.Patterns
                .EMAIL_ADDRESS
                .matcher(emailNormalizado)
                .matches()
        ) {
            _loginState.value =
                LoginState.Error(
                    "E-mail inválido"
                )

            return
        }

        _loginState.value =
            LoginState.Loading

        viewModelScope.launch {

            val result =
                repository.login(
                    emailNormalizado,
                    password
                )

            if (result.isSuccess) {

                val authResponse =
                    result.getOrThrow()

                sessionManager.salvarSessao(
                    authResponse
                )

                _loginState.postValue(
                    LoginState.Success(
                        authResponse.usuario
                    )
                )

            } else {

                _loginState.postValue(
                    LoginState.Error(
                        result
                            .exceptionOrNull()
                            ?.message
                            ?: "Erro ao fazer login"
                    )
                )
            }
        }
    }

    fun loginGoogle(
        idToken: String
    ) {
        if (idToken.isBlank()) {
            _loginState.value =
                LoginState.Error(
                    "O Google não retornou um token válido."
                )

            return
        }

        _loginState.value =
            LoginState.Loading

        viewModelScope.launch {

            val result =
                repository.loginGoogle(
                    idToken
                )

            if (result.isFailure) {

                _loginState.postValue(
                    LoginState.Error(
                        result
                            .exceptionOrNull()
                            ?.message
                            ?: "Não foi possível entrar com o Google"
                    )
                )

                return@launch
            }

            val resposta =
                result.getOrThrow()

            if (resposta.cadastroNecessario) {

                _loginState.postValue(
                    LoginState.GoogleCadastroNecessario(
                        idToken = idToken,
                        nome = resposta.nomeGoogle.orEmpty(),
                        email = resposta.emailGoogle.orEmpty()
                    )
                )

                return@launch
            }

            val autenticacao =
                resposta.autenticacao

            if (autenticacao == null) {

                _loginState.postValue(
                    LoginState.Error(
                        "O backend não retornou os dados de autenticação."
                    )
                )

                return@launch
            }

            sessionManager.salvarSessao(
                autenticacao
            )

            _loginState.postValue(
                LoginState.Success(
                    autenticacao.usuario
                )
            )
        }
    }
}

sealed class LoginState {

    object Loading :
        LoginState()

    data class Success(
        val usuario: UsuarioSessao
    ) : LoginState()

    data class GoogleCadastroNecessario(
        val idToken: String,
        val nome: String,
        val email: String
    ) : LoginState()

    data class Error(
        val message: String
    ) : LoginState()
}