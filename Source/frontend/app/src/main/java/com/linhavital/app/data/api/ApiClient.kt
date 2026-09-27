package com.linhavital.app.data.api

import android.content.Context
import com.linhavital.app.BuildConfig
import com.linhavital.app.utils.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    @Volatile
    private var applicationContext: Context? = null

    fun initialize(
        context: Context
    ) {

        applicationContext =
            context.applicationContext
    }

    private val authInterceptor =
        Interceptor { chain ->

            val requestOriginal =
                chain.request()

            val token =
                applicationContext
                    ?.let { context ->

                        runBlocking {

                            SessionManager(context)
                                .getAccessToken()
                        }
                    }

            val requestComAutenticacao =
                if (token.isNullOrBlank()) {

                    requestOriginal

                } else {

                    requestOriginal
                        .newBuilder()
                        .header(
                            "Authorization",
                            "Bearer $token"
                        )
                        .build()
                }

            chain.proceed(
                requestComAutenticacao
            )
        }

    private val loggingInterceptor =
        HttpLoggingInterceptor().apply {

            level =
                if (BuildConfig.DEBUG) {

                    HttpLoggingInterceptor.Level.BASIC

                } else {

                    HttpLoggingInterceptor.Level.NONE
                }
        }

    private val okHttpClient: OkHttpClient by lazy {

        OkHttpClient
            .Builder()
            .addInterceptor(
                authInterceptor
            )
            .addInterceptor(
                loggingInterceptor
            )
            .connectTimeout(
                ApiConfig.TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
            .readTimeout(
                ApiConfig.TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
            .writeTimeout(
                ApiConfig.TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            )
            .build()
    }

    val retrofit: Retrofit by lazy {

        Retrofit
            .Builder()
            .baseUrl(
                ApiConfig.BASE_URL
            )
            .client(
                okHttpClient
            )
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
    }

    inline fun <reified T> create(): T =
        retrofit.create(
            T::class.java
        )
}