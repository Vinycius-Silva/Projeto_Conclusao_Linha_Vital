package com.linhavital.backend.controller

import com.linhavital.backend.dto.*
import com.linhavital.backend.exception.UnauthorizedException
import com.linhavital.backend.service.AuthService
import com.linhavital.backend.service.AuthorizationService
import com.linhavital.backend.service.UsuarioService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val authorizationService: AuthorizationService,
    private val usuarioService: UsuarioService
) {

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest
    ): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(
            authService.login(
                request.email,
                request.senha
            )
        )

    @PostMapping("/google")
    fun loginGoogle(
        @RequestBody request: GoogleLoginRequest
    ): ResponseEntity<GoogleLoginResponse> =
        ResponseEntity.ok(
            authService.loginGoogle(
                request.idToken
            )
        )

    @PostMapping("/google/cadastro")
    fun cadastrarGoogle(
        @RequestBody request: GoogleCadastroRequest
    ): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(
            authService.cadastrarGoogle(
                request
            )
        )

    @GetMapping("/me")
    fun me(): ResponseEntity<UsuarioResponse> {

        val usuarioId =
            authorizationService
                .usuarioAtualId()

        return ResponseEntity.ok(
            usuarioService
                .buscarRespostaPorId(
                    usuarioId
                )
        )
    }

    @PostMapping("/logout")
    fun logout(
        request: HttpServletRequest
    ): ResponseEntity<Map<String, String>> {

        val header =
            request.getHeader(
                HttpHeaders.AUTHORIZATION
            )

        val token =
            header
                ?.takeIf {
                    it.startsWith(
                        "Bearer ",
                        ignoreCase = true
                    )
                }
                ?.substring(7)
                ?.trim()

        if (token.isNullOrBlank()) {
            throw UnauthorizedException(
                "Token de autenticação não informado."
            )
        }

        authService.logout(
            token
        )

        return ResponseEntity.ok(
            mapOf(
                "mensagem" to "Logout realizado com sucesso."
            )
        )
    }
}