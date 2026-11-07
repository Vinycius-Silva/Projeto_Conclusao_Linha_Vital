package com.linhavital.app.ui.home

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linhavital.app.data.model.ContatoEmergencia
import com.linhavital.app.data.repository.ContatoRepository
import kotlinx.coroutines.launch

class ContatoViewModel : ViewModel() {

    private val repository =
        ContatoRepository()

    private val _contatos =
        MutableLiveData<List<ContatoEmergencia>>()

    val contatos:
            LiveData<List<ContatoEmergencia>> =
        _contatos

    private val _estado =
        MutableLiveData<ContatoEstado>()

    val estado:
            LiveData<ContatoEstado> =
        _estado

    fun carregarContatos(
        usuarioId: Long
    ) {

        viewModelScope.launch {

            val result =
                repository.listarContatos(
                    usuarioId
                )

            if (
                result.isSuccess
            ) {

                _contatos.postValue(
                    result.getOrNull()
                        ?: emptyList()
                )

            } else {

                _estado.postValue(
                    ContatoEstado.Erro(
                        result
                            .exceptionOrNull()
                            ?.message
                            ?: "Erro ao carregar contatos"
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

        val nomeNormalizado =
            nome.trim()

        val telefoneNormalizado =
            telefone.trim()

        val emailNormalizado =
            email.trim()

        val erro =
            validarContato(
                nome =
                    nomeNormalizado,

                telefone =
                    telefoneNormalizado,

                email =
                    emailNormalizado
            )

        if (
            erro != null
        ) {

            _estado.value =
                ContatoEstado.Erro(
                    erro
                )

            return
        }

        _estado.value =
            ContatoEstado.Loading

        viewModelScope.launch {

            val contato =
                ContatoEmergencia(
                    nome =
                        nomeNormalizado,

                    telefone =
                        telefoneNormalizado,

                    email =
                        emailNormalizado,

                    tipoContato =
                        tipo
                )

            val result =
                repository.cadastrarContato(
                    usuarioId,
                    contato
                )

            if (
                result.isSuccess
            ) {

                _estado.postValue(
                    ContatoEstado.Sucesso
                )

                carregarContatos(
                    usuarioId
                )

            } else {

                _estado.postValue(
                    ContatoEstado.Erro(
                        result
                            .exceptionOrNull()
                            ?.message
                            ?: "Erro ao cadastrar"
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

        val nomeNormalizado =
            nome.trim()

        val telefoneNormalizado =
            telefone.trim()

        val emailNormalizado =
            email.trim()

        val erro =
            validarContato(
                nome =
                    nomeNormalizado,

                telefone =
                    telefoneNormalizado,

                email =
                    emailNormalizado
            )

        if (
            erro != null
        ) {

            _estado.value =
                ContatoEstado.Erro(
                    erro
                )

            return
        }

        _estado.value =
            ContatoEstado.Loading

        viewModelScope.launch {

            val contato =
                ContatoEmergencia(
                    id =
                        contatoId,

                    nome =
                        nomeNormalizado,

                    telefone =
                        telefoneNormalizado,

                    email =
                        emailNormalizado,

                    tipoContato =
                        tipo
                )

            val result =
                repository.atualizarContato(
                    usuarioId,
                    contatoId,
                    contato
                )

            if (
                result.isSuccess
            ) {

                _estado.postValue(
                    ContatoEstado.Atualizado
                )

                carregarContatos(
                    usuarioId
                )

            } else {

                _estado.postValue(
                    ContatoEstado.Erro(
                        result
                            .exceptionOrNull()
                            ?.message
                            ?: "Erro ao atualizar contato"
                    )
                )
            }
        }
    }

    fun deletarContato(
        usuarioId: Long,
        contatoId: Long
    ) {

        viewModelScope.launch {

            val result =
                repository.deletarContato(
                    usuarioId,
                    contatoId
                )

            if (
                result.isSuccess
            ) {

                carregarContatos(
                    usuarioId
                )

            } else {

                _estado.postValue(
                    ContatoEstado.Erro(
                        result
                            .exceptionOrNull()
                            ?.message
                            ?: "Erro ao deletar contato"
                    )
                )
            }
        }
    }

    fun reordenarContatos(
        usuarioId: Long,
        contatosOrdenados:
        List<ContatoEmergencia>
    ) {

        if (
            contatosOrdenados.size < 2
        ) {
            return
        }

        val ids =
            contatosOrdenados
                .mapNotNull {
                    it.id
                }

        /*
         * Todos os contatos vindos do backend
         * precisam possuir ID.
         */
        if (
            ids.size != contatosOrdenados.size
        ) {

            _estado.value =
                ContatoEstado.ErroReordenacao(
                    "Não foi possível identificar todos os contatos."
                )

            carregarContatos(
                usuarioId
            )

            return
        }

        /*
         * Atualização otimista:
         * a nova ordem aparece imediatamente
         * na interface.
         */
        _contatos.value =
            contatosOrdenados.toList()

        viewModelScope.launch {

            val result =
                repository.reordenarContatos(
                    usuarioId =
                        usuarioId,

                    contatoIds =
                        ids
                )

            if (
                result.isSuccess
            ) {

                val contatosConfirmados =
                    result.getOrNull()
                        ?: contatosOrdenados

                _contatos.postValue(
                    contatosConfirmados
                )

                _estado.postValue(
                    ContatoEstado.OrdemAtualizada
                )

            } else {

                _estado.postValue(
                    ContatoEstado.ErroReordenacao(
                        "Não foi possível salvar a nova prioridade."
                    )
                )

                /*
                 * Caso o backend rejeite a mudança,
                 * recuperamos novamente a ordem persistida.
                 */
                carregarContatos(
                    usuarioId
                )
            }
        }
    }

    private fun validarContato(
        nome: String,
        telefone: String,
        email: String
    ): String? {

        if (
            nome.isBlank() ||
            telefone.isBlank() ||
            email.isBlank()
        ) {

            return "Preencha nome, telefone e e-mail"
        }

        val quantidadeDigitosTelefone =
            telefone
                .filter(
                    Char::isDigit
                )
                .length

        if (
            quantidadeDigitosTelefone !in 10..13
        ) {

            return "Informe um telefone válido"
        }

        if (
            !Patterns
                .EMAIL_ADDRESS
                .matcher(
                    email
                )
                .matches()
        ) {

            return "Informe um e-mail válido"
        }

        return null
    }
}

sealed class ContatoEstado {

    object Loading :
        ContatoEstado()

    object Sucesso :
        ContatoEstado()

    object Atualizado :
        ContatoEstado()

    object OrdemAtualizada :
        ContatoEstado()

    data class Erro(
        val message: String
    ) : ContatoEstado()

    data class ErroReordenacao(
        val message: String
    ) : ContatoEstado()
}