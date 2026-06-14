package com.linhavital.backend.repository

import com.linhavital.backend.model.HistoricoNotificacao
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface HistoricoNotificacaoRepository : JpaRepository<HistoricoNotificacao, Long> {

    @Modifying
    @Query(
        value = "DELETE FROM historiconotificacao WHERE fk_contato_id_contato = :contatoId",
        nativeQuery = true
    )
    fun deleteByContatoId(@Param("contatoId") contatoId: Long): Int
}   