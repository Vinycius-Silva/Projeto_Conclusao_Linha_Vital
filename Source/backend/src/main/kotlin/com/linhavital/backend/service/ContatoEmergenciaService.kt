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

        /*
         * Contato novo sempre entra no final
         * da lista de prioridade.
         */
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

    /*
     * ===================================================
     * REORDENAÇÃO DE CONTATOS
     * ===================================================
     *
     * A posição na lista define a prioridade.
     *
     * Índice 0 -> prioridade 1
     * Índice 1 -> prioridade 2
     * Índice 2 -> prioridade 3
     *
     * O primeiro contato será, portanto,
     * o contato prioritário utilizado pelo SOS.
     */
    @Transactional
    fun reordenarContatos(
        usuarioId: Long,
        contatoIds: List<Long>
    ): List<ContatoEmergencia> {

        validarUsuario(
            usuarioId
        )

        val contatosAtuais =
            usuarioContatoRepository
                .findContatosByUsuarioId(
                    usuarioId
                )

        val idsAtuais =
            contatosAtuais
                .map {
                    it.id
                }

        /*
         * Não aceitamos IDs duplicados.
         */
        require(
            contatoIds.distinct().size ==
                    contatoIds.size
        ) {
            "A lista de contatos possui IDs duplicados"
        }

        /*
         * A requisição precisa conter exatamente
         * os contatos atualmente pertencentes ao usuário.
         *
         * Isso impede:
         * - remover contato apenas pela reordenação;
         * - inserir contato de outro usuário;
         * - enviar uma lista incompleta.
         */
        require(
            contatoIds.size ==
                    idsAtuais.size &&
                    contatoIds.toSet() ==
                    idsAtuais.toSet()
        ) {
            "A lista de contatos informada é inválida"
        }

        contatoIds.forEachIndexed {
                index,
                contatoId ->

            val prioridade =
                index + 1

            val atualizados =
                usuarioContatoRepository
                    .atualizarPrioridade(
                        usuarioId =
                            usuarioId,

                        contatoId =
                            contatoId,

                        prioridade =
                            prioridade
                    )

            if (
                atualizados != 1
            ) {

                throw IllegalStateException(
                    "Não foi possível atualizar a prioridade do contato $contatoId"
                )
            }
        }

        /*
         * A consulta já possui:
         *
         * ORDER BY prioridade ASC
         *
         * Portanto devolvemos ao frontend a lista
         * exatamente na ordem que foi persistida.
         */
        return usuarioContatoRepository
            .findContatosByUsuarioId(
                usuarioId
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