package com.linhavital.backend.service

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.linhavital.backend.exception.UnauthorizedException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

data class GoogleIdentity(
    val subject: String,
    val email: String,
    val nome: String
)

@Service
class GoogleOAuthService(

    @Value("\${oauth.google.client-id:}")
    private val clientId: String
) {

    private val verifier: GoogleIdTokenVerifier by lazy {

        if (clientId.isBlank()) {
            throw IllegalStateException(
                "GOOGLE_OAUTH_CLIENT_ID não está configurado."
            )
        }

        GoogleIdTokenVerifier.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance()
        )
            .setAudience(
                listOf(clientId)
            )
            .build()
    }

    fun verificar(
        idTokenString: String
    ): GoogleIdentity {

        if (idTokenString.isBlank()) {
            throw UnauthorizedException(
                "Token Google não informado."
            )
        }

        val idToken = try {

            verifier.verify(
                idTokenString
            )

        } catch (_: Exception) {

            throw UnauthorizedException(
                "Token Google inválido."
            )
        } ?: throw UnauthorizedException(
            "Token Google inválido ou expirado."
        )

        val payload =
            idToken.payload

        if (payload.emailVerified != true) {
            throw UnauthorizedException(
                "O e-mail da Conta Google não foi verificado."
            )
        }

        val subject =
            payload.subject
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: throw UnauthorizedException(
                    "Conta Google sem identificador válido."
                )

        val email =
            payload.email
                ?.trim()
                ?.lowercase()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: throw UnauthorizedException(
                    "Conta Google sem e-mail válido."
                )

        val nome =
            (payload["name"] as? String)
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: email.substringBefore("@")

        return GoogleIdentity(
            subject = subject,
            email = email,
            nome = nome
        )
    }
}