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
    private val repository: ContatoEmergenciaRepository,
    private val usuarioContatoRepository: UsuarioContatoRepository,
    private val historicoNotificacaoRepository: HistoricoNotificacaoRepository,
    private val usuarioRepository: UsuarioRepository
) {

    fun listar() = repository.findAll()

    fun buscarPorId(id: Long) =
        repository.findById(id).orElseThrow { RuntimeException("Contato não encontrado") }

    fun salvar(contato: ContatoEmergencia) = repository.save(contato)

    fun listarPorUsuario(usuarioId: Long): List<ContatoEmergencia> {
        validarUsuario(usuarioId)
        return usuarioContatoRepository.findContatosByUsuarioId(usuarioId)
    }

    @Transactional
    fun salvarParaUsuario(usuarioId: Long, contato: ContatoEmergencia): ContatoEmergencia {
        val usuario = validarUsuario(usuarioId)
        validarContato(contato)

        val contatoSalvo = repository.save(contato.copy(id = 0))
        val prioridade = usuarioContatoRepository.findMaxPrioridadeByUsuarioId(usuarioId) + 1

        usuarioContatoRepository.save(
            UsuarioContato(
                prioridade = prioridade,
                usuario = usuario,
                contato = contatoSalvo
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
        validarUsuario(usuarioId)
        if (usuarioContatoRepository.countByUsuarioIdAndContatoId(usuarioId, contatoId) == 0L) {
            throw RuntimeException("Contato não pertence ao usuário informado")
        }
        validarContato(contatoAtualizado)

        val atual = buscarPorId(contatoId)
        val novo = atual.copy(
            nome = contatoAtualizado.nome.trim(),
            telefone = contatoAtualizado.telefone.trim(),
            email = contatoAtualizado.email.trim(),
            tipoContato = contatoAtualizado.tipoContato.trim()
        )
        return repository.save(novo)
    }

    @Transactional
    fun deletarDoUsuario(usuarioId: Long, contatoId: Long) {
        validarUsuario(usuarioId)
        if (usuarioContatoRepository.deleteByUsuarioIdAndContatoId(usuarioId, contatoId) == 0) {
            throw RuntimeException("Contato não pertence ao usuário informado")
        }

        if (usuarioContatoRepository.countByContatoId(contatoId) == 0L) {
            historicoNotificacaoRepository.deleteByContatoId(contatoId)
            repository.deleteById(contatoId)
        }
    }

    @Transactional
    fun deletar(id: Long) {
        historicoNotificacaoRepository.deleteByContatoId(id)
        usuarioContatoRepository.deleteByContatoId(id)
        repository.deleteById(id)
    }

    private fun validarUsuario(usuarioId: Long) =
        usuarioRepository.findById(usuarioId).orElseThrow { RuntimeException("Usuário não encontrado") }

    private fun validarContato(contato: ContatoEmergencia) {
        require(contato.nome.isNotBlank()) { "Nome do contato é obrigatório" }
        require(contato.telefone.filter(Char::isDigit).length >= 10) { "Telefone do contato é inválido" }
    }
}
