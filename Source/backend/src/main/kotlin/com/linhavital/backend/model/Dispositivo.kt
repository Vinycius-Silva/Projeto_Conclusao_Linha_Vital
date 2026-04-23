package com.linhavital.backend.model

import jakarta.persistence.*

@Entity
@Table(name = "dispostivo") // (mantive igual ao seu banco)
data class Dispositivo(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dispositivo")
    val id: Long = 0,

    @Column(name = "sistema_dispositio")
    val sistema: String,

    @Column(name = "versao_sistema_dispositivo")
    val versao: String,

    @Column(name = "status_dispositivo")
    val status: String,

    @ManyToOne
    @JoinColumn(name = "fk_usuario_id_usuario")
    val usuario: Usuario
)