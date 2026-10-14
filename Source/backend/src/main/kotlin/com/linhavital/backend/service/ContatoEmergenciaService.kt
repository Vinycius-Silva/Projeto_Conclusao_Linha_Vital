package com.linhavital.backend.service

import com.linhavital.backend.model.ContatoEmergencia
import com.linhavital.backend.model.UsuarioContato
import com.linhavital.backend.repository.ContatoEmergenciaRepository
import com.linhavital.backend.repository.HistoricoNotificacaoRepository
import com.linhavital.backend.repository.UsuarioContatoRepository
import com.linhavital.backend.repository.UsuarioRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ContatoEmergenciaService(
    private val repository:
    ContatoEmergenciaRepository,

    private val usuarioContatoRepository:
    UsuarioContatoRepository,

    private val historicoNotificacaoRepository:
    HistoricoNotificacaoRepository,

    private val usuarioRepository:
    UsuarioRepository
) {

    companion object {

        private val EMAIL_REGEX =
            Regex(
                pattern =
                    "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",

                option =
                    RegexOption.IGNORE_CASE
            )
    }

    fun listar():
            List<ContatoEmergencia> =
        repository.findAll()

    fun buscarPorId(
        id: Long
    ): ContatoEmergencia =
        repository
            .findById(
                id
            )
            .orElseThrow {
                RuntimeException(
                    "Contato não encontrado"
                )
            }

    @Transactional
    fun salvar(
        contato: ContatoEmergencia
    ): ContatoEmergencia {

        val contatoNormalizado =
            normalizarContato(
                contato
            )

        return repository.save(
            contatoNormalizado
        )
    }

    fun listarPorUsuario(
        usuarioId: Long
    ): List<ContatoEmergencia> {

        validarUsuario(
            usuarioId
        )

        return usuarioContatoRepository
            .findContatosByUsuarioId(
                usuarioId
            )
    }

    @Transactional
    fun salvarParaUsuario(
        usuarioId: Long,
        contato: ContatoEmergencia
    ): ContatoEmergencia {

        val usuario =
            validarUsuario(
                usuarioId
            )

        val contatoNormalizado =
            normalizarContato(
                contato
            )

        val contatoSalvo =
            repository.save(
                contatoNormalizado.copy(
                    id = 0
                )
            )

        val prioridade =
            usuarioContatoRepository
                .findMaxPrioridadeByUsuarioId(
                    usuarioId
                ) + 1

        usuarioContatoRepository
            .save(
                UsuarioContato(
                    prioridade =
                        prioridade,

                    usuario =
                        usuario,

                    contato =
                        contatoSalvo
                )
            )

        return contatoSalvo
    }

    @Transactional
    fun atualizarDoUsuario(
        usuarioId: Long,
        contatoId: Long,
        contatoAtualizado: ContatoEmergencia
    ): ContatoEmergencia {

        validarUsuario(
            usuarioId
        )

        if (
            usuarioContatoRepository
                .countByUsuarioIdAndContatoId(
                    usuarioId,
                    contatoId
                ) == 0L
        ) {

            throw RuntimeException(
                "Contato não pertence ao usuário informado"
            )
        }

        val contatoNormalizado =
            normalizarContato(
                contatoAtualizado
            )

        val atual =
            buscarPorId(
                contatoId
            )

        val novo =
            atual.copy(
                nome =
                    contatoNormalizado.nome,

                telefone =
                    contatoNormalizado.telefone,

                email =
                    contatoNormalizado.email,

                tipoContato =
                    contatoNormalizado.tipoContato
            )

        return repository.save(
            novo
        )
    }

    @Transactional
    fun deletarDoUsuario(
        usuarioId: Long,
        contatoId: Long
    ) {

        validarUsuario(
            usuarioId
        )

        val vinculosRemovidos =
            usuarioContatoRepository
                .deleteByUsuarioIdAndContatoId(
                    usuarioId,
                    contatoId
                )

        if (
            vinculosRemovidos == 0
        ) {

            throw RuntimeException(
                "Contato não pertence ao usuário informado"
            )
        }

        removerContatoFisicoSePossivel(
            contatoId
        )
    }

    @Transactional
    fun deletar(
        id: Long
    ) {

        usuarioContatoRepository
            .deleteByContatoId(
                id
            )

        removerContatoFisicoSePossivel(
            id
        )
    }

    /*
     * ===================================================
     * EXCLUSÃO FÍSICA SEGURA
     * ===================================================
     *
     * O contato só é removido da tabela ContatoEmergencia
     * quando não existe:
     *
     * 1. vínculo com nenhum usuário;
     * 2. histórico de notificações relacionado a ele.
     *
     * Dessa forma, excluir um contato da agenda não apaga
     * o histórico de SOS e de outras ocorrências antigas.
     */
    private fun removerContatoFisicoSePossivel(
        contatoId: Long
    ) {

        val possuiVinculos =
            usuarioContatoRepository
                .countByContatoId(
                    contatoId
                ) > 0L

        if (
            possuiVinculos
        ) {

            return
        }

        val possuiHistorico =
            historicoNotificacaoRepository
                .countByContatoId(
                    contatoId
                ) > 0L

        if (
            possuiHistorico
        ) {

            /*
             * O contato permanece fisicamente na tabela
             * somente para preservar o histórico.
             *
             * Como não existe mais UsuarioContato,
             * ele não aparecerá na agenda do usuário
             * nem participará de novos SOS.
             */
            return
        }

        if (
            repository.existsById(
                contatoId
            )
        ) {

            repository.deleteById(
                contatoId
            )
        }
    }

    private fun validarUsuario(
        usuarioId: Long
    ) =
        usuarioRepository
            .findById(
                usuarioId
            )
            .orElseThrow {
                RuntimeException(
                    "Usuário não encontrado"
                )
            }

    private fun normalizarContato(
        contato: ContatoEmergencia
    ): ContatoEmergencia {

        val nome =
            contato.nome
                .trim()

        val telefone =
            contato.telefone
                .filter(
                    Char::isDigit
                )

        val email =
            contato.email
                .trim()
                .lowercase()

        val tipoContato =
            contato.tipoContato
                .trim()

        require(
            nome.isNotBlank()
        ) {
            "Nome do contato é obrigatório"
        }

        require(
            telefone.length in 10..13
        ) {
            "Telefone do contato é inválido"
        }

        require(
            email.isNotBlank() &&
                    EMAIL_REGEX.matches(
                        email
                    )
        ) {
            "E-mail do contato é inválido"
        }

        require(
            tipoContato.isNotBlank()
        ) {
            "Tipo do contato é obrigatório"
        }

        return contato.copy(
            nome =
                nome,

            telefone =
                telefone,

            email =
                email,

            tipoContato =
                tipoContato
        )
    }
}