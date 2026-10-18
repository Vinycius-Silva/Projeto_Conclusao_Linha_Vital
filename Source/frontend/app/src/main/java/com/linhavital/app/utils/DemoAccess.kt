package com.linhavital.app.utils

import com.linhavital.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object DemoAccess {

    const val BLOCK_MESSAGE =
        "Esta versão de demonstração está disponível somente em 21/10/2026."

    const val VALIDATION_ERROR_MESSAGE =
        "Não foi possível validar o acesso à versão de demonstração. Verifique sua conexão com a internet e tente novamente."

    suspend fun validarComServidor(): DemoValidationResult {

        if (!BuildConfig.DEMO_MODE) {
            return DemoValidationResult.Allowed
        }

        return withContext(
            Dispatchers.IO
        ) {

            var connection:
                    HttpURLConnection? =
                null

            try {

                val baseUrl =
                    BuildConfig.API_BASE_URL
                        .trimEnd('/')

                val url =
                    URL(
                        "$baseUrl/teste"
                    )

                connection =
                    url.openConnection()
                            as HttpURLConnection

                connection.requestMethod =
                    "GET"

                connection.connectTimeout =
                    10_000

                connection.readTimeout =
                    10_000

                connection.useCaches =
                    false

                connection.setRequestProperty(
                    BuildConfig.DEMO_HEADER_NAME,
                    BuildConfig.DEMO_HEADER_VALUE
                )

                when (
                    connection.responseCode
                ) {

                    HttpURLConnection.HTTP_OK -> {

                        DemoValidationResult.Allowed
                    }

                    HttpURLConnection.HTTP_FORBIDDEN -> {

                        DemoValidationResult.Blocked
                    }

                    else -> {

                        DemoValidationResult.Error(
                            VALIDATION_ERROR_MESSAGE
                        )
                    }
                }

            } catch (
                _: Exception
            ) {

                DemoValidationResult.Error(
                    VALIDATION_ERROR_MESSAGE
                )

            } finally {

                connection
                    ?.disconnect()
            }
        }
    }
}

sealed class DemoValidationResult {

    object Allowed :
        DemoValidationResult()

    object Blocked :
        DemoValidationResult()

    data class Error(
        val message: String
    ) : DemoValidationResult()
}