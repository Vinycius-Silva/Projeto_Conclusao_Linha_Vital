package com.linhavital.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linhavital.app.data.model.ContatoEmergencia
import com.linhavital.app.data.repository.ContatoRepository
import kotlinx.coroutines.launch

class ContatoViewModel : ViewModel() {

    private val repository = ContatoRepository()

    private val _contatos = MutableLiveData<List<ContatoEmergencia>>()
    val contatos: LiveData<List<ContatoEmergencia>> = _contatos

    private val _estado = MutableLiveData<ContatoEstado>()
    val estado: LiveData<ContatoEstado> = _estado

    fun carregarContatos(usuarioId: Long) {
        viewModelScope.launch {
            val result = repository.listarContatos(usuarioId)

            if (result.isSuccess) {
                _contatos.postValue(result.getOrNull() ?: emptyList())
            } else {
                _estado.postValue(
                    ContatoEstado.Erro(
                        result.exceptionOrNull()?.message ?: "Erro ao carregar contatos"
                    )
                )
            }
        }
    }

    fun cadastrarContato(
        usuarioId: Long,
        nome: String,
        telefone: String,
        email: String,
        tipo: String
    ) {
        if (nome.isBlank() || telefone.isBlank()) {
            _estado.value = ContatoEstado.Erro("Preencha nome e telefone")
            return
        }
        if (telefone.filter(Char::isDigit).length < 10) {
            _estado.value = ContatoEstado.Erro("Informe um telefone válido")
            return
        }

        _estado.value = ContatoEstado.Loading

        viewModelScope.launch {
            val contato = ContatoEmergencia(
                nome = nome,
                telefone = telefone,
                email = email,
                tipoContato = tipo
            )

            val result = repository.cadastrarContato(usuarioId, contato)

            if (result.isSuccess) {
                _estado.postValue(ContatoEstado.Sucesso)
                carregarContatos(usuarioId)
            } else {
                _estado.postValue(
                    ContatoEstado.Erro(
                        result.exceptionOrNull()?.message ?: "Erro ao cadastrar"
                    )
                )
            }
        }
    }

    fun atualizarContato(
        usuarioId: Long,
        contatoId: Long,
        nome: String,
        telefone: String,
        email: String,
        tipo: String
    ) {
        if (nome.isBlank() || telefone.isBlank()) {
            _estado.value = ContatoEstado.Erro("Preencha nome e telefone")
            return
        }
        if (telefone.filter(Char::isDigit).length < 10) {
            _estado.value = ContatoEstado.Erro("Informe um telefone válido")
            return
        }

        _estado.value = ContatoEstado.Loading
        viewModelScope.launch {
            val contato = ContatoEmergencia(
                id = contatoId,
                nome = nome,
                telefone = telefone,
                email = email,
                tipoContato = tipo
            )
            val result = repository.atualizarContato(usuarioId, contatoId, contato)
            if (result.isSuccess) {
                _estado.postValue(ContatoEstado.Atualizado)
                carregarContatos(usuarioId)
            } else {
                _estado.postValue(
                    ContatoEstado.Erro(
                        result.exceptionOrNull()?.message ?: "Erro ao atualizar contato"
                    )
                )
            }
        }
    }

    fun deletarContato(usuarioId: Long, contatoId: Long) {
        viewModelScope.launch {
            val result = repository.deletarContato(usuarioId, contatoId)

            if (result.isSuccess) {
                carregarContatos(usuarioId)
            } else {
                _estado.postValue(
                    ContatoEstado.Erro(
                        result.exceptionOrNull()?.message ?: "Erro ao deletar contato"
                    )
                )
            }
        }
    }
}

sealed class ContatoEstado {
    object Loading : ContatoEstado()
    object Sucesso : ContatoEstado()
    object Atualizado : ContatoEstado()
    data class Erro(val message: String) : ContatoEstado()
}