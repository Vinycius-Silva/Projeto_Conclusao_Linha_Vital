package com.linhavital.app.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocalizacaoProvider(
    context: Context
) {

    private val appContext =
        context.applicationContext

    private val fusedLocationClient =
        LocationServices
            .getFusedLocationProviderClient(
                appContext
            )

    fun possuiPermissao(): Boolean {

        val permissaoPrecisa =
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val permissaoAproximada =
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        return permissaoPrecisa ||
                permissaoAproximada
    }

    @SuppressLint("MissingPermission")
    suspend fun obterLocalizacaoAtual():
            Result<Coordenadas> {

        if (!possuiPermissao()) {

            return Result.failure(
                SecurityException(
                    "Permissão de localização não concedida."
                )
            )
        }

        return suspendCancellableCoroutine { continuation ->

            val cancellationTokenSource =
                CancellationTokenSource()

            continuation.invokeOnCancellation {

                cancellationTokenSource
                    .cancel()
            }

            fusedLocationClient
                .getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                )
                .addOnSuccessListener { location ->

                    if (!continuation.isActive) {
                        return@addOnSuccessListener
                    }

                    if (location != null) {

                        continuation.resume(
                            Result.success(
                                Coordenadas(
                                    latitude =
                                        location.latitude,

                                    longitude =
                                        location.longitude
                                )
                            )
                        )

                    } else {

                        continuation.resume(
                            Result.failure(
                                IllegalStateException(
                                    "Não foi possível obter a localização atual. Verifique se a localização do aparelho está ativada."
                                )
                            )
                        )
                    }
                }
                .addOnFailureListener { exception ->

                    if (!continuation.isActive) {
                        return@addOnFailureListener
                    }

                    continuation.resume(
                        Result.failure(
                            exception
                        )
                    )
                }
        }
    }
}

data class Coordenadas(
    val latitude: Double,
    val longitude: Double
)