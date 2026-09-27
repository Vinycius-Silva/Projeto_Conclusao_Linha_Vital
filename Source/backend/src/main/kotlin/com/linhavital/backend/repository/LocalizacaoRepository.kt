package com.linhavital.backend.repository

import com.linhavital.backend.model.Localizacao
import org.springframework.data.jpa.repository.JpaRepository

interface LocalizacaoRepository :
    JpaRepository<Localizacao, Long> {

    fun findByUsuarioIdOrderByDataHoraDesc(
        usuarioId: Long
    ): List<Localizacao>

    fun findFirstByUsuarioIdOrderByDataHoraDesc(
        usuarioId: Long
    ): Localizacao?
}