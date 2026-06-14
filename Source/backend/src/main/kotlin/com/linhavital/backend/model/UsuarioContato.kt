package com.linhavital.backend.model

import jakarta.persistence.*
import java.io.Serializable

data class UsuarioContatoId(
    val usuario: Long = 0,
    val contato: Long = 0
) : Serializable

@Entity
@Table(name = "usuariocontato")
@IdClass(UsuarioContatoId::class)
data class UsuarioContato(

    @Column(name = "prioridade")
    val prioridade: Int = 0,

    @Id
    @ManyToOne
    @JoinColumn(name = "fk_usuario_id_usuario")
    val usuario: Usuario,

    @Id
    @ManyToOne
    @JoinColumn(name = "fk_contato_id_contato")
    val contato: ContatoEmergencia
)