package com.linhavital.backend.service

import com.linhavital.backend.model.AuthSession
import com.linhavital.backend.model.Usuario
import com.linhavital.backend.repository.AuthSessionRepository
import com.linhavital.backend.security.AuthenticatedUser
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64

data class SessionToken(
    val accessToken: String,
    val expiraEm: Instant
)

@Service
class AuthSessionService(
    private val repository: AuthSessionRepository,

    @Value("\${auth.session.ttl-hours:720}")
    private val ttlHours: Long
) {

    private val secureRandom =
        SecureRandom()

    @Transactional
    fun criarSessao(
        usuario: Usuario
    ): SessionToken {

        val rawToken =
            gerarToken()

        val agora =
            Instant.now()

        val expiracao =
            agora.plus(
                ttlHours.coerceAtLeast(1),
                ChronoUnit.HOURS
            )

        repository.save(
            AuthSession(
                tokenHash = hashToken(rawToken),
                criadoEm = agora,
                expiraEm = expiracao,
                usuario = usuario
            )
        )

        return SessionToken(
            accessToken = rawToken,
            expiraEm = expiracao
        )
    }

    @Transactional(readOnly = true)
    fun autenticar(
        rawToken: String
    ): AuthenticatedUser? {

        if (rawToken.isBlank()) {
            return null
        }

        val sessao =
            repository
                .findByTokenHashAndRevogadoEmIsNull(
                    hashToken(rawToken)
                )
                ?: return null

        if (!sessao.expiraEm.isAfter(Instant.now())) {
            return null
        }

        return AuthenticatedUser(
            usuarioId = sessao.usuario.id,
            email = sessao.usuario.email
        )
    }

    @Transactional
    fun revogar(
        rawToken: String
    ) {

        val sessao =
            repository
                .findByTokenHashAndRevogadoEmIsNull(
                    hashToken(rawToken)
                )
                ?: return

        sessao.revogadoEm =
            Instant.now()

        repository.save(
            sessao
        )
    }

    @Scheduled(
        cron = "\${AUTH_SESSION_CLEANUP_CRON:0 0 4 * * *}"
    )
    @Transactional
    fun limparSessoesAntigas() {

        repository
            .deleteExpiradasOuRevogadas(
                Instant.now()
            )
    }

    private fun gerarToken(): String {

        val bytes =
            ByteArray(32)

        secureRandom.nextBytes(
            bytes
        )

        return Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes)
    }

    private fun hashToken(
        token: String
    ): String {

        val hash =
            MessageDigest
                .getInstance("SHA-256")
                .digest(
                    token.toByteArray(
                        Charsets.UTF_8
                    )
                )

        return hash.joinToString("") {
            "%02x".format(
                it.toInt() and 0xff
            )
        }
    }
}