package com.linhavital.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "localizacao")
data class Localizacao(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_localizacao")
    val id: Long = 0,

    val latitude: Double,
    val longitude: Double,

    @Column(name = "data_hora_localizacao")
    val dataHora: LocalDateTime,

    @ManyToOne
    @JoinColumn(name = "fk_usuario_id_usuario")
    val usuario: Usuario
)