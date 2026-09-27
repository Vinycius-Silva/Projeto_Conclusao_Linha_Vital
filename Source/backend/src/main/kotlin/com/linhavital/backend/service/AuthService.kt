package com.linhavital.backend.service

import com.linhavital.backend.dto.AuthResponse
import com.linhavital.backend.dto.GoogleCadastroRequest
import com.linhavital.backend.dto.GoogleLoginResponse
import com.linhavital.backend.dto.toResponse
import com.linhavital.backend.exception.ConflictException
import com.linhavital.backend.exception.UnauthorizedException
import com.linhavital.backend.model.Usuario
import com.linhavital.backend.repository.UsuarioRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.UUID

@Service
class AuthService(
    private val usuarioRepository: UsuarioRepository,
    private val passwordService: PasswordService,
    private val googleOAuthService: GoogleOAuthService,
    private val authSessionService: AuthSessionService
) {

    @Transactional
    fun login(
        email: String,
        senha: String
    ): AuthResponse {

        val usuario =
            usuarioRepository
                .findByEmailIgnoreCase(
                    email.trim()
                )
                ?: throw UnauthorizedException(
                    "E-mail ou senha incorretos."
                )

        if (
            !passwordService.matches(
                senha,
                usuario.senha
            )
        ) {
            throw UnauthorizedException(
                "E-mail ou senha incorretos."
            )
        }

        // Migração automática das senhas legadas
        // em texto puro.
        if (
            !passwordService.isEncoded(
                usuario.senha
            )
        ) {

            usuario.senha =
                passwordService.hash(
                    senha
                )

            usuarioRepository.save(
                usuario
            )
        }

        return criarRespostaAutenticada(
            usuario
        )
    }

    @Transactional
    fun loginGoogle(
        idToken: String
    ): GoogleLoginResponse {

        val identidade =
            googleOAuthService.verificar(
                idToken
            )

        val usuarioPorGoogle =
            usuarioRepository
                .findByOauthProviderAndOauthSubject(
                    "GOOGLE",
                    identidade.subject
                )

        if (usuarioPorGoogle != null) {

            return GoogleLoginResponse(
                cadastroNecessario = false,
                autenticacao =
                    criarRespostaAutenticada(
                        usuarioPorGoogle
                    )
            )
        }

        val usuarioPorEmail =
            usuarioRepository
                .findByEmailIgnoreCase(
                    identidade.email
                )

        if (usuarioPorEmail != null) {

            validarVinculoGoogle(
                usuarioPorEmail,
                identidade
            )

            usuarioPorEmail.oauthProvider =
                "GOOGLE"

            usuarioPorEmail.oauthSubject =
                identidade.subject

            usuarioRepository.save(
                usuarioPorEmail
            )

            return GoogleLoginResponse(
                cadastroNecessario = false,
                autenticacao =
                    criarRespostaAutenticada(
                        usuarioPorEmail
                    )
            )
        }

        return GoogleLoginResponse(
            cadastroNecessario = true,
            nomeGoogle = identidade.nome,
            emailGoogle = identidade.email
        )
    }

    @Transactional
    fun cadastrarGoogle(
        request: GoogleCadastroRequest
    ): AuthResponse {

        val identidade =
            googleOAuthService.verificar(
                request.idToken
            )

        usuarioRepository
            .findByOauthProviderAndOauthSubject(
                "GOOGLE",
                identidade.subject
            )
            ?.let {
                return criarRespostaAutenticada(
                    it
                )
            }

        val usuarioExistente =
            usuarioRepository
                .findByEmailIgnoreCase(
                    identidade.email
                )

        if (usuarioExistente != null) {

            validarVinculoGoogle(
                usuarioExistente,
                identidade
            )

            usuarioExistente.oauthProvider =
                "GOOGLE"

            usuarioExistente.oauthSubject =
                identidade.subject

            usuarioRepository.save(
                usuarioExistente
            )

            return criarRespostaAutenticada(
                usuarioExistente
            )
        }

        val telefone =
            request.telefone
                .filter(
                    Char::isDigit
                )

        require(
            telefone.length in 10..13
        ) {
            "Telefone inválido."
        }

        val dataNascimento =
            try {

                LocalDate.parse(
                    request.dataNascimento.trim()
                )

            } catch (_: DateTimeParseException) {

                throw IllegalArgumentException(
                    "Data de nascimento inválida. Utilize AAAA-MM-DD."
                )
            }

        require(
            !dataNascimento.isAfter(
                LocalDate.now()
            )
        ) {
            "Data de nascimento não pode estar no futuro."
        }

        val senhaInterna =
            passwordService.hash(
                "oauth2-" +
                        UUID.randomUUID() +
                        "-" +
                        identidade.subject
            )

        val usuario =
            usuarioRepository.save(
                Usuario(
                    nome = identidade.nome,
                    email = identidade.email,
                    telefone = telefone,
                    dataNascimento = dataNascimento,
                    senha = senhaInterna,
                    oauthProvider = "GOOGLE",
                    oauthSubject = identidade.subject
                )
            )

        return criarRespostaAutenticada(
            usuario
        )
    }

    fun logout(
        rawToken: String
    ) {

        authSessionService.revogar(
            rawToken
        )
    }

    private fun validarVinculoGoogle(
        usuario: Usuario,
        identidade: GoogleIdentity
    ) {

        val provider =
            usuario.oauthProvider

        val subject =
            usuario.oauthSubject

        if (
            provider != null &&
            provider != "GOOGLE"
        ) {
            throw ConflictException(
                "A conta já está vinculada a outro provedor."
            )
        }

        if (
            subject != null &&
            subject != identidade.subject
        ) {
            throw ConflictException(
                "Este e-mail já está vinculado a outra Conta Google."
            )
        }
    }

    private fun criarRespostaAutenticada(
        usuario: Usuario
    ): AuthResponse {

        val sessao =
            authSessionService
                .criarSessao(
                    usuario
                )

        return AuthResponse(
            accessToken =
                sessao.accessToken,

            tokenType =
                "Bearer",

            expiresAt =
                sessao.expiraEm.toString(),

            usuario =
                usuario.toResponse()
        )
    }
}