package com.linhavital.backend.model

import jakarta.persistence.*

@Entity
@Table(name = "contatoemergencia")
data class ContatoEmergencia(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contato")
    val id: Long = 0,

    @Column(name = "nome_contato")
    val nome: String,

    @Column(name = "telefone_contato")
    val telefone: String,

    @Column(name = "email_contato")
    val email: String,

    @Column(name = "tipo_contato")
    val tipoContato: String
)