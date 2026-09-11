package com.linhavital.backend.repository

import com.linhavital.backend.model.ConfiguracaoMonitoramento
import org.springframework.data.jpa.repository.JpaRepository

interface ConfiguracaoMonitoramentoRepository : JpaRepository<ConfiguracaoMonitoramento, Long> {
    fun findByUsuarioId(usuarioId: Long): ConfiguracaoMonitoramento?
}
