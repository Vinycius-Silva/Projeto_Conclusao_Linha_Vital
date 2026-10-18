package com.linhavital.app.utils

object TelefoneUtils {

    private val dddsValidos =
        setOf(
            11, 12, 13, 14, 15, 16, 17, 18, 19,
            21, 22, 24,
            27, 28,
            31, 32, 33, 34, 35, 37, 38,
            41, 42, 43, 44, 45, 46, 47, 48, 49,
            51, 53, 54, 55,
            61, 62, 63, 64, 65, 66, 67, 68, 69,
            71, 73, 74, 75, 77, 79,
            81, 82, 83, 84, 85, 86, 87, 88, 89,
            91, 92, 93, 94, 95, 96, 97, 98, 99
        )

    fun somenteDigitos(
        telefone: String
    ): String {
        return telefone.filter(
            Char::isDigit
        )
    }

    fun normalizarBrasil(
        telefone: String
    ): String {

        var numeros =
            somenteDigitos(
                telefone
            )

        // Aceita também números informados com o código do Brasil:
        // +55 (11) 97589-2953
        if (
            numeros.startsWith("55") &&
            numeros.length in 12..13
        ) {
            numeros =
                numeros.substring(2)
        }

        return numeros
    }

    fun validar(
        telefone: String
    ): Boolean {

        val numeros =
            normalizarBrasil(
                telefone
            )

        // Fixo: DDD + 8 dígitos
        // Celular: DDD + 9 dígitos
        if (
            numeros.length != 10 &&
            numeros.length != 11
        ) {
            return false
        }

        // Evita números evidentemente falsos.
        if (
            numeros.all {
                it == numeros.first()
            }
        ) {
            return false
        }

        val ddd =
            numeros
                .substring(
                    0,
                    2
                )
                .toIntOrNull()
                ?: return false

        if (
            ddd !in dddsValidos
        ) {
            return false
        }

        val numeroLocal =
            numeros.substring(2)

        // Celular brasileiro:
        // 9 dígitos começando com 9.
        if (
            numeros.length == 11
        ) {
            return numeroLocal.length == 9 &&
                    numeroLocal.startsWith("9")
        }

        // Telefone fixo:
        // 8 dígitos, normalmente começando entre 2 e 5.
        return numeroLocal.length == 8 &&
                numeroLocal.first() in '2'..'5'
    }
}