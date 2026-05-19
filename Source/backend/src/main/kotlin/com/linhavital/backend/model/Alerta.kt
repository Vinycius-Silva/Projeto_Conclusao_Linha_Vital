package com.linhavital.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "alerta")
data class Alerta(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerta")
    val id: Long = 0,

    @Column(name = "tipo_alerta")
    val tipo: String,

    @Column(name = "data_hora_alerta")
    val dataHora: LocalDateTime = LocalDateTime.now(),

    @Column(name = "status_alerta")
    val status: String = "PENDENTE",

    @ManyToOne
    @JoinColumn(name = "fk_usuario_id_usuario")
    val usuario: Usuario
)