package com.linhavital.backend.service

import com.linhavital.backend.dto.UsuarioResponse
import com.linhavital.backend.dto.toResponse
import com.linhavital.backend.model.Usuario
import com.linhavital.backend.repository.UsuarioRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class UsuarioService(
    private val repository: UsuarioRepository,
    private val passwordService: PasswordService
) {

    companion object {

        private val EMAIL_REGEX =
            Regex(
                pattern =
                    "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",

                option =
                    RegexOption.IGNORE_CASE
            )
    }

    fun listar():
            List<UsuarioResponse> =
        repository
            .findAll()
            .map {
                it.toResponse()
            }

    fun buscarPorId(
        id: Long
    ): Usuario =
        repository
            .findById(
                id
            )
            .orElseThrow {
                RuntimeException(
                    "Usuário não encontrado"
                )
            }

    fun buscarRespostaPorId(
        id: Long
    ): UsuarioResponse =
        buscarPorId(
            id
        ).toResponse()

    @Transactional
    fun salvar(
        usuario: Usuario
    ): UsuarioResponse {

        val nomeNormalizado =
            usuario.nome
                .trim()

        val emailNormalizado =
            usuario.email
                .trim()
                .lowercase()

        val telefoneNormalizado =
            usuario.telefone
                .filter(
                    Char::isDigit
                )

        validarDadosUsuario(
            nome =
                nomeNormalizado,

            email =
                emailNormalizado,

            telefone =
                telefoneNormalizado,

            dataNascimento =
                usuario.dataNascimento
        )

        require(
            usuario.senha.length >= 6
        ) {
            "Senha deve ter no mínimo 6 caracteres"
        }

        if (
            repository
                .existsByEmailIgnoreCase(
                    emailNormalizado
                )
        ) {

            throw IllegalArgumentException(
                "Já existe uma conta com este e-mail"
            )
        }

        usuario.nome =
            nomeNormalizado

        usuario.email =
            emailNormalizado

        usuario.telefone =
            telefoneNormalizado

        usuario.senha =
            passwordService
                .hash(
                    usuario.senha
                )

        return repository
            .save(
                usuario
            )
            .toResponse()
    }

    @Transactional
    fun atualizar(
        id: Long,
        usuarioAtualizado: Usuario
    ): UsuarioResponse {

        val usuario =
            buscarPorId(
                id
            )

        val nomeNormalizado =
            usuarioAtualizado
                .nome
                .trim()

        val emailNormalizado =
            usuarioAtualizado
                .email
                .trim()
                .lowercase()

        val telefoneNormalizado =
            usuarioAtualizado
                .telefone
                .filter(
                    Char::isDigit
                )

        validarDadosUsuario(
            nome =
                nomeNormalizado,

            email =
                emailNormalizado,

            telefone =
                telefoneNormalizado,

            dataNascimento =
                usuarioAtualizado.dataNascimento
        )

        if (
            usuarioAtualizado
                .senha
                .isNotBlank()
        ) {

            require(
                usuarioAtualizado
                    .senha
                    .length >= 6
            ) {
                "Senha deve ter no mínimo 6 caracteres"
            }
        }

        val outroUsuario =
            repository
                .findByEmailIgnoreCase(
                    emailNormalizado
                )

        if (
            outroUsuario != null &&
            outroUsuario.id != id
        ) {

            throw IllegalArgumentException(
                "Já existe uma conta com este e-mail"
            )
        }

        usuario.nome =
            nomeNormalizado

        usuario.email =
            emailNormalizado

        usuario.telefone =
            telefoneNormalizado

        usuario.dataNascimento =
            usuarioAtualizado.dataNascimento

        if (
            usuarioAtualizado
                .senha
                .isNotBlank()
        ) {

            usuario.senha =
                passwordService
                    .hash(
                        usuarioAtualizado.senha
                    )
        }

        return repository
            .save(
                usuario
            )
            .toResponse()
    }

    fun deletar(
        id: Long
    ) {

        repository.delete(
            buscarPorId(
                id
            )
        )
    }

    private fun validarDadosUsuario(
        nome: String,
        email: String,
        telefone: String,
        dataNascimento: LocalDate
    ) {

        require(
            nome.isNotBlank()
        ) {
            "Nome é obrigatório"
        }

        require(
            email.isNotBlank() &&
                    EMAIL_REGEX.matches(
                        email
                    )
        ) {
            "E-mail inválido"
        }

        require(
            telefone.length in 10..13
        ) {
            "Telefone inválido"
        }

        require(
            !dataNascimento.isAfter(
                LocalDate.now()
            )
        ) {
            "Data de nascimento não pode estar no futuro"
        }
    }
}