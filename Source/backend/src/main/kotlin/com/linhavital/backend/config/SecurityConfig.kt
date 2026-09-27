package com.linhavital.backend.config

import com.linhavital.backend.security.BearerTokenFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(
    private val bearerTokenFilter: BearerTokenFilter
) {

    @Bean
    fun securityFilterChain(
        http: HttpSecurity
    ): SecurityFilterChain {

        http
            .csrf {
                it.disable()
            }
            .cors {
                it.disable()
            }
            .formLogin {
                it.disable()
            }
            .httpBasic {
                it.disable()
            }
            .sessionManagement {
                it.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            }
            .authorizeHttpRequests {

                it.requestMatchers(
                    HttpMethod.POST,
                    "/auth/login",
                    "/auth/google",
                    "/auth/google/cadastro"
                ).permitAll()

                it.requestMatchers(
                    HttpMethod.POST,
                    "/usuarios"
                ).permitAll()

                it.requestMatchers(
                    HttpMethod.OPTIONS,
                    "/**"
                ).permitAll()

                it.requestMatchers(
                    "/teste",
                    "/error"
                ).permitAll()

                it.anyRequest()
                    .authenticated()
            }
            .exceptionHandling {

                it.authenticationEntryPoint {
                        _,
                        response,
                        _ ->

                    response.status =
                        HttpServletResponseStatus.UNAUTHORIZED

                    response.contentType =
                        "application/json"

                    response.characterEncoding =
                        "UTF-8"

                    response.writer.write(
                        """{"erro":"Não autenticado."}"""
                    )
                }

                it.accessDeniedHandler {
                        _,
                        response,
                        _ ->

                    response.status =
                        HttpServletResponseStatus.FORBIDDEN

                    response.contentType =
                        "application/json"

                    response.characterEncoding =
                        "UTF-8"

                    response.writer.write(
                        """{"erro":"Acesso negado."}"""
                    )
                }
            }
            .addFilterBefore(
                bearerTokenFilter,
                UsernamePasswordAuthenticationFilter::class.java
            )

        return http.build()
    }

    private object HttpServletResponseStatus {
        const val UNAUTHORIZED = 401
        const val FORBIDDEN = 403
    }
}