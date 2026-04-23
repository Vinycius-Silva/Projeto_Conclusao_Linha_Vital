package com.linhavital.backend.model

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "usuario")
data class Usuario(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    val id: Long = 0,

    @Column(name = "nome_usuario")
    val nome: String,

    @Column(name = "email_usuario")
    val email: String,

    @Column(name = "telefone_usuario")
    val telefone: String,

    @Column(name = "data_nascimento")
    val dataNascimento: LocalDate,

    @Column(name = "senha_usuario")
    val senha: String
)