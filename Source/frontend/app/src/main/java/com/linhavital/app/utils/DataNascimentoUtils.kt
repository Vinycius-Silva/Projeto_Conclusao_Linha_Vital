package com.linhavital.app.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

object DataNascimentoUtils {

    /*
     * Utilizamos "uuuu", e não "yyyy",
     * para que o ResolverStyle.STRICT funcione
     * corretamente com LocalDate.
     */
    private val formatter =
        DateTimeFormatter
            .ofPattern(
                "dd/MM/uuuu"
            )
            .withResolverStyle(
                ResolverStyle.STRICT
            )

    /*
     * Converte DD/MM/AAAA para AAAA-MM-DD.
     *
     * Retorna null quando:
     *
     * - o formato é inválido;
     * - a data não existe;
     * - a data está no futuro.
     *
     * O parâmetro "hoje" também facilita
     * a execução dos testes unitários.
     */
    fun converterParaIso(
        dataTexto: String,
        hoje: LocalDate = LocalDate.now()
    ): String? {

        val data =
            try {

                LocalDate.parse(
                    dataTexto.trim(),
                    formatter
                )

            } catch (
                _: DateTimeParseException
            ) {

                return null
            }

        if (
            data.isAfter(
                hoje
            )
        ) {

            return null
        }

        return data.toString()
    }
}