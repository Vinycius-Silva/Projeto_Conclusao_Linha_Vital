package com.linhavital.backend.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.LocalDate
import java.time.ZoneId

@Component
class DemoAccessFilter(

    @Value(
        "\${demo.access.header-name:X-LinhaVital-Demo}"
    )
    private val demoHeaderName: String,

    @Value(
        "\${demo.access.header-value:EVENTO-2026-10-21}"
    )
    private val demoHeaderValue: String,

    @Value(
        "\${demo.access.valid-date:2026-10-21}"
    )
    private val demoValidDate: String,

    @Value(
        "\${demo.access.zone-id:America/Sao_Paulo}"
    )
    private val demoZoneId: String

) : OncePerRequestFilter() {

    private val validDate: LocalDate by lazy {

        LocalDate.parse(
            demoValidDate
        )
    }

    private val zoneId: ZoneId by lazy {

        ZoneId.of(
            demoZoneId
        )
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val marker =
            request.getHeader(
                demoHeaderName
            )

        if (
            marker.isNullOrBlank()
        ) {

            filterChain.doFilter(
                request,
                response
            )

            return
        }

        if (
            marker != demoHeaderValue
        ) {

            bloquear(
                response,
                "Identificação da versão de demonstração inválida."
            )

            return
        }

        val hoje =
            LocalDate.now(
                zoneId
            )

        if (
            hoje != validDate
        ) {

            bloquear(
                response,
                "Esta versão de demonstração está disponível somente em 21/10/2026."
            )

            return
        }

        filterChain.doFilter(
            request,
            response
        )
    }

    private fun bloquear(
        response: HttpServletResponse,
        mensagem: String
    ) {

        response.status =
            HttpServletResponse.SC_FORBIDDEN

        response.contentType =
            "application/json"

        response.characterEncoding =
            "UTF-8"

        response.writer.write(
            """{"erro":"$mensagem"}"""
        )
    }
}
