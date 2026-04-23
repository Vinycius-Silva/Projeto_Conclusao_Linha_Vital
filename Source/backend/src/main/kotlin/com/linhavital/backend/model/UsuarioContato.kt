package com.linhavital.backend.model

import jakarta.persistence.*

@Entity
@Table(name = "usuariocontato_possui")
data class UsuarioContato(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val prioridade: Int,

    @ManyToOne
    @JoinColumn(name = "fk_usuario_id_usuario")
    val usuario: Usuario,

    @ManyToOne
    @JoinColumn(name = "fk_contatoemergencia_id_contato")
    val contato: ContatoEmergencia
)