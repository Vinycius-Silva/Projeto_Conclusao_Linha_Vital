package com.linhavital.app

import com.linhavital.app.utils.SosInteractionResult
import com.linhavital.app.utils.SosInteractionTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SosInteractionTrackerTest {

    @Test
    fun `primeira interacao deve pedir mais duas`() {

        val tracker =
            SosInteractionTracker()

        val resultado =
            tracker.registrar(
                agora = 0L
            )

        assertEquals(
            SosInteractionResult.Progresso(
                interacoesRestantes = 2
            ),
            resultado
        )
    }

    @Test
    fun `segunda interacao deve pedir mais uma`() {

        val tracker =
            SosInteractionTracker()

        tracker.registrar(
            agora = 0L
        )

        val resultado =
            tracker.registrar(
                agora = 500L
            )

        assertEquals(
            SosInteractionResult.Progresso(
                interacoesRestantes = 1
            ),
            resultado
        )
    }

    @Test
    fun `tres interacoes dentro de dois segundos devem acionar SOS`() {

        val tracker =
            SosInteractionTracker()

        tracker.registrar(
            agora = 0L
        )

        tracker.registrar(
            agora = 700L
        )

        val resultado =
            tracker.registrar(
                agora = 1_400L
            )

        assertTrue(
            resultado is
                    SosInteractionResult.Acionar
        )
    }

    @Test
    fun `tres interacoes exatamente em dois segundos devem acionar SOS`() {

        val tracker =
            SosInteractionTracker()

        tracker.registrar(
            agora = 0L
        )

        tracker.registrar(
            agora = 1_000L
        )

        val resultado =
            tracker.registrar(
                agora = 2_000L
            )

        assertTrue(
            resultado is
                    SosInteractionResult.Acionar
        )
    }

    @Test
    fun `tres interacoes acima da janela total nao devem acionar SOS`() {

        val tracker =
            SosInteractionTracker()

        tracker.registrar(
            agora = 0L
        )

        tracker.registrar(
            agora = 1_900L
        )

        val resultado =
            tracker.registrar(
                agora = 3_800L
            )

        /*
         * O terceiro toque ocorreu 3,8 segundos
         * depois do primeiro.
         *
         * Portanto ele passa a ser considerado
         * a primeira interação de uma nova sequência.
         */
        assertEquals(
            SosInteractionResult.Progresso(
                interacoesRestantes = 2
            ),
            resultado
        )
    }

    @Test
    fun `intervalo grande deve iniciar nova sequencia`() {

        val tracker =
            SosInteractionTracker()

        tracker.registrar(
            agora = 0L
        )

        val resultado =
            tracker.registrar(
                agora = 2_500L
            )

        assertEquals(
            SosInteractionResult.Progresso(
                interacoesRestantes = 2
            ),
            resultado
        )
    }

    @Test
    fun `apos acionar SOS deve ser possivel iniciar nova sequencia`() {

        val tracker =
            SosInteractionTracker()

        tracker.registrar(
            agora = 0L
        )

        tracker.registrar(
            agora = 500L
        )

        tracker.registrar(
            agora = 1_000L
        )

        val novaSequencia =
            tracker.registrar(
                agora = 3_000L
            )

        assertEquals(
            SosInteractionResult.Progresso(
                interacoesRestantes = 2
            ),
            novaSequencia
        )
    }
}