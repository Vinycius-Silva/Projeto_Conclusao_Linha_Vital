package com.linhavital.backend.exception

class UnauthorizedException(
    message: String
) : RuntimeException(message)

class ConflictException(
    message: String
) : RuntimeException(message)