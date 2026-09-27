package com.linhavital.backend.security

import com.linhavital.backend.service.AuthSessionService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class BearerTokenFilter(
    private val authSessionService: AuthSessionService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val header =
            request.getHeader(
                HttpHeaders.AUTHORIZATION
            )

        if (
            header != null &&
            header.startsWith(
                "Bearer ",
                ignoreCase = true
            ) &&
            SecurityContextHolder
                .getContext()
                .authentication == null
        ) {

            val rawToken =
                header
                    .substring(7)
                    .trim()

            val usuario =
                authSessionService
                    .autenticar(
                        rawToken
                    )

            if (usuario != null) {

                val authentication =
                    UsernamePasswordAuthenticationToken(
                        usuario,
                        null,
                        listOf(
                            SimpleGrantedAuthority(
                                "ROLE_USER"
                            )
                        )
                    )

                authentication.details =
                    WebAuthenticationDetailsSource()
                        .buildDetails(
                            request
                        )

                SecurityContextHolder
                    .getContext()
                    .authentication =
                    authentication
            }
        }

        filterChain.doFilter(
            request,
            response
        )
    }
}