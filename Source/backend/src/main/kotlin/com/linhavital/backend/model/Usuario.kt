package com.linhavital.backend.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "usuario",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_usuario_oauth",
            columnNames = ["oauth_provider", "oauth_subject"]
        )
    ]
)
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

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "senha_usuario", nullable = false)
    var senha: String,

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "fcm_token")
    var fcmToken: String? = null,

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "oauth_provider", length = 30)
    var oauthProvider: String? = null,

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "oauth_subject", length = 255)
    var oauthSubject: String? = null
)