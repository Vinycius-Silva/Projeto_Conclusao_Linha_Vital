package com.linhavital.backend.controller

import com.linhavital.backend.exception.ConflictException
import com.linhavital.backend.exception.UnauthorizedException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(
        UnauthorizedException::class
    )
    fun handleUnauthorized(
        ex: UnauthorizedException
    ): ResponseEntity<Map<String, String>> =
        ResponseEntity
            .status(
                HttpStatus.UNAUTHORIZED
            )
            .body(
                mapOf(
                    "erro" to
                            (ex.message
                                ?: "Não autenticado.")
                )
            )

    @ExceptionHandler(
        AccessDeniedException::class
    )
    fun handleForbidden(
        ex: AccessDeniedException
    ): ResponseEntity<Map<String, String>> =
        ResponseEntity
            .status(
                HttpStatus.FORBIDDEN
            )
            .body(
                mapOf(
                    "erro" to
                            (ex.message
                                ?: "Acesso negado.")
                )
            )

    @ExceptionHandler(
        ConflictException::class
    )
    fun handleConflict(
        ex: ConflictException
    ): ResponseEntity<Map<String, String>> =
        ResponseEntity
            .status(
                HttpStatus.CONFLICT
            )
            .body(
                mapOf(
                    "erro" to
                            (ex.message
                                ?: "Conflito.")
                )
            )

    @ExceptionHandler(
        IllegalArgumentException::class
    )
    fun handleBadRequest(
        ex: IllegalArgumentException
    ): ResponseEntity<Map<String, String>> =
        ResponseEntity
            .badRequest()
            .body(
                mapOf(
                    "erro" to
                            (ex.message
                                ?: "Requisição inválida.")
                )
            )

    @ExceptionHandler(
        IllegalStateException::class
    )
    fun handleIllegalState(
        ex: IllegalStateException
    ): ResponseEntity<Map<String, String>> =
        ResponseEntity
            .status(
                HttpStatus.INTERNAL_SERVER_ERROR
            )
            .body(
                mapOf(
                    "erro" to
                            (ex.message
                                ?: "Erro interno.")
                )
            )

    @ExceptionHandler(
        RuntimeException::class
    )
    fun handleRuntime(
        ex: RuntimeException
    ): ResponseEntity<Map<String, String>> =
        ResponseEntity
            .status(
                HttpStatus.NOT_FOUND
            )
            .body(
                mapOf(
                    "erro" to
                            (ex.message
                                ?: "Recurso não encontrado.")
                )
            )
}