package com.linhavital.app

import com.linhavital.app.utils.DataNascimentoUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class DataNascimentoUtilsTest {

    private val hoje =
        LocalDate.of(
            2026,
            10,
            14
        )

    @Test
    fun `data valida deve ser convertida para iso`() {

        val resultado =
            DataNascimentoUtils
                .converterParaIso(
                    "12/09/2004",
                    hoje
                )

        assertEquals(
            "2004-09-12",
            resultado
        )
    }

    @Test
    fun `ano bissexto valido deve ser aceito`() {

        val resultado =
            DataNascimentoUtils
                .converterParaIso(
                    "29/02/2024",
                    hoje
                )

        assertEquals(
            "2024-02-29",
            resultado
        )
    }

    @Test
    fun `31 de fevereiro deve ser rejeitado`() {

        val resultado =
            DataNascimentoUtils
                .converterParaIso(
                    "31/02/2026",
                    hoje
                )

        assertNull(
            resultado
        )
    }

    @Test
    fun `29 de fevereiro em ano nao bissexto deve ser rejeitado`() {

        val resultado =
            DataNascimentoUtils
                .converterParaIso(
                    "29/02/2025",
                    hoje
                )

        assertNull(
            resultado
        )
    }

    @Test
    fun `dia zero deve ser rejeitado`() {

        val resultado =
            DataNascimentoUtils
                .converterParaIso(
                    "00/10/2004",
                    hoje
                )

        assertNull(
            resultado
        )
    }

    @Test
    fun `mes treze deve ser rejeitado`() {

        val resultado =
            DataNascimentoUtils
                .converterParaIso(
                    "10/13/2004",
                    hoje
                )

        assertNull(
            resultado
        )
    }

    @Test
    fun `data futura deve ser rejeitada`() {

        val resultado =
            DataNascimentoUtils
                .converterParaIso(
                    "15/10/2026",
                    hoje
                )

        assertNull(
            resultado
        )
    }

    @Test
    fun `data de hoje deve ser aceita`() {

        val resultado =
            DataNascimentoUtils
                .converterParaIso(
                    "14/10/2026",
                    hoje
                )

        assertEquals(
            "2026-10-14",
            resultado
        )
    }
}