package com.linhavital.backend.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "auth_session",
    indexes = [
        Index(
            name = "idx_auth_session_token_hash",
            columnList = "token_hash",
            unique = true
        ),
        Index(
            name = "idx_auth_session_usuario",
            columnList = "fk_usuario_id_usuario"
        )
    ]
)
class AuthSession(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auth_session")
    var id: Long = 0,

    @Column(
        name = "token_hash",
        nullable = false,
        unique = true,
        length = 64
    )
    var tokenHash: String,

    @Column(
        name = "criado_em",
        nullable = false
    )
    var criadoEm: Instant,

    @Column(
        name = "expira_em",
        nullable = false
    )
    var expiraEm: Instant,

    @Column(name = "revogado_em")
    var revogadoEm: Instant? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "fk_usuario_id_usuario",
        nullable = false
    )
    var usuario: Usuario
)