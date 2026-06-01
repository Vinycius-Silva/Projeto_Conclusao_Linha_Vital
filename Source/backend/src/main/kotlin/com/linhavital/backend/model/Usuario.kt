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

    @Column(name = "nome_usuario", nullable = false)
    var nome: String,

    @Column(name = "email_usuario", nullable = false, unique = true)
    var email: String,

    @Column(name = "telefone_usuario", nullable = false)
    var telefone: String,

    @Column(name = "data_nascimento", nullable = false)
    var dataNascimento: LocalDate,

    @Column(name = "senha_usuario", nullable = false)
    var senha: String,

    @Column(name = "fcm_token")
    var fcmToken: String? = null
)