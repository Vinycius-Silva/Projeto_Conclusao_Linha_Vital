package com.linhavital.app.utils

import com.linhavital.app.BuildConfig
import java.time.LocalDate
import java.time.ZoneId

object DemoAccess {

    private val zoneId =
        ZoneId.of(
            "America/Sao_Paulo"
        )

    const val BLOCK_MESSAGE =
        "Esta versão de demonstração está disponível somente em 21/10/2026."

    fun isAvailableNow(): Boolean {

        if (!BuildConfig.DEMO_MODE) {
            return true
        }

        val validDate =
            runCatching {
                LocalDate.parse(
                    BuildConfig.DEMO_VALID_DATE
                )
            }.getOrNull()
                ?: return false

        val today =
            LocalDate.now(
                zoneId
            )

        return today == validDate
    }
}
