package com.linhavital.backend.repository

import com.linhavital.backend.model.AuthSession
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface AuthSessionRepository : JpaRepository<AuthSession, Long> {

    fun findByTokenHashAndRevogadoEmIsNull(
        tokenHash: String
    ): AuthSession?

    @Modifying
    @Query(
        """
        DELETE FROM AuthSession s
        WHERE s.expiraEm < :agora
           OR s.revogadoEm IS NOT NULL
        """
    )
    fun deleteExpiradasOuRevogadas(
        @Param("agora") agora: Instant
    ): Int

    @Modifying
    @Query(
        """
        DELETE FROM AuthSession s
        WHERE s.usuario.id = :usuarioId
        """
    )
    fun deleteByUsuarioId(
        @Param("usuarioId") usuarioId: Long
    ): Int
}