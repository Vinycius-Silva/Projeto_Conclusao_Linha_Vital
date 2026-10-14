package com.linhavital.app.utils

sealed interface SosInteractionResult {

    data class Progresso(
        val interacoesRestantes: Int
    ) : SosInteractionResult

    data object Acionar :
        SosInteractionResult
}

class SosInteractionTracker(

    private val quantidadeNecessaria: Int = 3,

    private val janelaMillis: Long = 2_000L
) {

    private var quantidadeAtual =
        0

    private var primeiraInteracaoEm:
            Long? = null

    init {

        require(
            quantidadeNecessaria > 0
        ) {
            "A quantidade de interações deve ser maior que zero."
        }

        require(
            janelaMillis >= 0
        ) {
            "A janela de tempo não pode ser negativa."
        }
    }

    fun registrar(
        agora: Long =
            System.currentTimeMillis()
    ): SosInteractionResult {

        val primeira =
            primeiraInteracaoEm

        /*
         * Inicia uma nova sequência quando:
         *
         * - ainda não existe sequência;
         * - o relógio retornou um valor anterior;
         * - a janela total de tempo foi ultrapassada.
         *
         * Importante:
         *
         * A janela é medida sempre a partir da
         * PRIMEIRA interação, e não da interação
         * imediatamente anterior.
         */
        if (
            primeira == null ||
            agora < primeira ||
            agora - primeira > janelaMillis
        ) {

            quantidadeAtual =
                1

            primeiraInteracaoEm =
                agora

        } else {

            quantidadeAtual++
        }

        /*
         * Todas as interações necessárias aconteceram
         * dentro da mesma janela.
         */
        if (
            quantidadeAtual >=
            quantidadeNecessaria
        ) {

            reset()

            return SosInteractionResult.Acionar
        }

        return SosInteractionResult.Progresso(
            interacoesRestantes =
                quantidadeNecessaria -
                        quantidadeAtual
        )
    }

    fun reset() {

        quantidadeAtual =
            0

        primeiraInteracaoEm =
            null
    }
}