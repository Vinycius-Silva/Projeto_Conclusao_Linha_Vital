package com.linhavital.backend.repository

import com.linhavital.backend.model.ContatoEmergencia
import com.linhavital.backend.model.UsuarioContato
import com.linhavital.backend.model.UsuarioContatoId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UsuarioContatoRepository : JpaRepository<UsuarioContato, UsuarioContatoId> {

    @Query(
        """
        SELECT uc.contato
        FROM UsuarioContato uc
        WHERE uc.usuario.id = :usuarioId
        ORDER BY uc.prioridade ASC, uc.contato.id ASC
        """
    )
    fun findContatosByUsuarioId(@Param("usuarioId") usuarioId: Long): List<ContatoEmergencia>

    @Query(
        """
        SELECT COALESCE(MAX(uc.prioridade), 0)
        FROM UsuarioContato uc
        WHERE uc.usuario.id = :usuarioId
        """
    )
    fun findMaxPrioridadeByUsuarioId(@Param("usuarioId") usuarioId: Long): Int

    @Query(
        value = "SELECT COUNT(*) FROM usuariocontato WHERE fk_contato_id_contato = :contatoId",
        nativeQuery = true
    )
    fun countByContatoId(@Param("contatoId") contatoId: Long): Long

    @Query(
        value = "SELECT COUNT(*) FROM usuariocontato WHERE fk_usuario_id_usuario = :usuarioId AND fk_contato_id_contato = :contatoId",
        nativeQuery = true
    )
    fun countByUsuarioIdAndContatoId(
        @Param("usuarioId") usuarioId: Long,
        @Param("contatoId") contatoId: Long
    ): Long

    @Modifying
    @Query(
        value = "DELETE FROM usuariocontato WHERE fk_usuario_id_usuario = :usuarioId AND fk_contato_id_contato = :contatoId",
        nativeQuery = true
    )
    fun deleteByUsuarioIdAndContatoId(
        @Param("usuarioId") usuarioId: Long,
        @Param("contatoId") contatoId: Long
    ): Int

    @Modifying
    @Query(
        value = "DELETE FROM usuariocontato WHERE fk_contato_id_contato = :contatoId",
        nativeQuery = true
    )
    fun deleteByContatoId(@Param("contatoId") contatoId: Long): Int
}
