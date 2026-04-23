package com.linhavital.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "monitoramento")
data class Monitoramento(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_monitoramento")
    val id: Long = 0,

    @Column(name = "data_hora_monitoramento")
    val dataHora: LocalDateTime,

    @Column(name = "status_monitoramento")
    val status: String,

    @ManyToOne
    @JoinColumn(name = "fk_usuario_id_usuario")
    val usuario: Usuario
)