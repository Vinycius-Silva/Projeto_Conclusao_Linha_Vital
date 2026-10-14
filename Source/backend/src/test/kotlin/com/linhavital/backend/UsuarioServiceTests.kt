package com.linhavital.backend

import com.linhavital.backend.model.Usuario
import com.linhavital.backend.repository.UsuarioRepository
import com.linhavital.backend.service.PasswordService
import com.linhavital.backend.service.UsuarioService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDate

class UsuarioServiceTests {

    private val repository =
        Mockito.mock(
            UsuarioRepository::class.java
        )

    private val passwordService =
        PasswordService()

    private val service =
        UsuarioService(
            repository =
                repository,

            passwordService =
                passwordService
        )

    @Test
    fun `cadastro deve rejeitar data de nascimento futura`() {

        val usuario =
            usuarioValido()
                .copy(
                    dataNascimento =
                        LocalDate
                            .now()
                            .plusDays(
                                1
                            )
                )

        val erro =
            assertThrows(
                IllegalArgumentException::class.java
            ) {

                service.salvar(
                    usuario
                )
            }

        assertEquals(
            "Data de nascimento não pode estar no futuro",
            erro.message
        )
    }

    @Test
    fun `cadastro deve rejeitar email invalido`() {

        val usuario =
            usuarioValido()
                .copy(
                    email =
                        "email-invalido"
                )

        val erro =
            assertThrows(
                IllegalArgumentException::class.java
            ) {

                service.salvar(
                    usuario
                )
            }

        assertEquals(
            "E-mail inválido",
            erro.message
        )
    }

    @Test
    fun `cadastro deve rejeitar telefone curto`() {

        val usuario =
            usuarioValido()
                .copy(
                    telefone =
                        "12345"
                )

        val erro =
            assertThrows(
                IllegalArgumentException::class.java
            ) {

                service.salvar(
                    usuario
                )
            }

        assertEquals(
            "Telefone inválido",
            erro.message
        )
    }

    @Test
    fun `cadastro deve rejeitar nome vazio`() {

        val usuario =
            usuarioValido()
                .copy(
                    nome =
                        "   "
                )

        val erro =
            assertThrows(
                IllegalArgumentException::class.java
            ) {

                service.salvar(
                    usuario
                )
            }

        assertEquals(
            "Nome é obrigatório",
            erro.message
        )
    }

    @Test
    fun `cadastro deve rejeitar senha menor que seis caracteres`() {

        val usuario =
            usuarioValido()
                .copy(
                    senha =
                        "12345"
                )

        val erro =
            assertThrows(
                IllegalArgumentException::class.java
            ) {

                service.salvar(
                    usuario
                )
            }

        assertEquals(
            "Senha deve ter no mínimo 6 caracteres",
            erro.message
        )
    }

    private fun usuarioValido():
            Usuario =
        Usuario(
            nome =
                "Usuário Teste",

            email =
                "usuario@teste.com",

            telefone =
                "11999999999",

            dataNascimento =
                LocalDate.of(
                    2000,
                    1,
                    1
                ),

            senha =
                "123456"
        )
}