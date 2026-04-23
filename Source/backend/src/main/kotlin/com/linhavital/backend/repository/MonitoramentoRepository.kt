package com.linhavital.backend.repository

import com.linhavital.backend.model.Monitoramento
import org.springframework.data.jpa.repository.JpaRepository

interface MonitoramentoRepository : JpaRepository<Monitoramento, Long>