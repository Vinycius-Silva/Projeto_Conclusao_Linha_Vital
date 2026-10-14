package com.linhavital.backend.repository

import com.linhavital.backend.model.HistoricoNotificacao
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface HistoricoNotificacaoRepository :
    JpaRepository<HistoricoNotificacao, Long> {

    fun findByAlertaUsuarioIdOrderByDataHoraDesc(
        usuarioId: Long
    ): List<HistoricoNotificacao>

    @Query(
        value =
            """
            SELECT COUNT(*)
            FROM historiconotificacao
            WHERE fk_contato_id_contato = :contatoId
            """,
        nativeQuery = true
    )
    fun countByContatoId(
        @Param("contatoId")
        contatoId: Long
    ): Long
}