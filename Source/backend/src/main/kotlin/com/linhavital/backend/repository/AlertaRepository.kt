package com.linhavital.backend.repository

import com.linhavital.backend.model.Alerta
import org.springframework.data.jpa.repository.JpaRepository

interface AlertaRepository : JpaRepository<Alerta, Long>