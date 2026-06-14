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
        return usuarioContatoRepository.findContatosByUsuarioId(usuarioId)
    }

    @Transactional
    fun salvarParaUsuario(usuarioId: Long, contato: ContatoEmergencia): ContatoEmergencia {
        val usuario = usuarioRepository.findById(usuarioId).orElseThrow {
            RuntimeException("Usuário não encontrado")
        }

        val contatoSalvo = repository.save(contato)

        val prioridade = usuarioContatoRepository.countByUsuarioId(usuarioId).toInt() + 1

        val usuarioContato = UsuarioContato(
            prioridade = prioridade,
            usuario = usuario,
            contato = contatoSalvo
        )

        usuarioContatoRepository.save(usuarioContato)

        return contatoSalvo
    }

    @Transactional
    fun deletarDoUsuario(usuarioId: Long, contatoId: Long) {
        usuarioContatoRepository.deleteByUsuarioIdAndContatoId(usuarioId, contatoId)

        val quantidadeRelacionamentos = usuarioContatoRepository.countByContatoId(contatoId)

        if (quantidadeRelacionamentos == 0L) {
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
}