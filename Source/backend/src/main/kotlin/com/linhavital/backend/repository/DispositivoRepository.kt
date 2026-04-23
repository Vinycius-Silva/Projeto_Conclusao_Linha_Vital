package com.linhavital.backend.repository

import com.linhavital.backend.model.Dispositivo
import org.springframework.data.jpa.repository.JpaRepository

interface DispositivoRepository : JpaRepository<Dispositivo, Long>