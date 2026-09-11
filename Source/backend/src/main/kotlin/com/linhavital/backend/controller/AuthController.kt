package com.linhavital.backend.controller

import com.linhavital.backend.dto.LoginRequest
import com.linhavital.backend.dto.UsuarioResponse
import com.linhavital.backend.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<UsuarioResponse> =
        ResponseEntity.ok(authService.login(request.email, request.senha))
}
