package com.linhavital.app.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.linhavital.app.data.model.AuthResponse
import kotlinx.coroutines.flow.first
import java.time.Instant

private val Context.dataStore by preferencesDataStore(
    name = "session"
)

class SessionManager(
    private val context: Context
) {

    companion object {

        private val KEY_USER_ID =
            longPreferencesKey("user_id")

        private val KEY_USER_NAME =
            stringPreferencesKey("user_name")

        private val KEY_USER_EMAIL =
            stringPreferencesKey("user_email")

        private val KEY_ACCESS_TOKEN =
            stringPreferencesKey("access_token")

        private val KEY_TOKEN_TYPE =
            stringPreferencesKey("token_type")

        private val KEY_TOKEN_EXPIRES_AT =
            stringPreferencesKey("token_expires_at")

        private val KEY_ONBOARDING_COMPLETED =
            booleanPreferencesKey("onboarding_completed")
    }

    suspend fun salvarSessao(
        authResponse: AuthResponse
    ) {

        context.dataStore.edit { prefs ->

            prefs[KEY_USER_ID] =
                authResponse.usuario.id

            prefs[KEY_USER_NAME] =
                authResponse.usuario.nome

            prefs[KEY_USER_EMAIL] =
                authResponse.usuario.email

            prefs[KEY_ACCESS_TOKEN] =
                authResponse.accessToken

            prefs[KEY_TOKEN_TYPE] =
                authResponse.tokenType

            prefs[KEY_TOKEN_EXPIRES_AT] =
                authResponse.expiresAt
        }
    }

    suspend fun getUserId(): Long? =
        context.dataStore
            .data
            .first()[KEY_USER_ID]

    suspend fun getUserName(): String? =
        context.dataStore
            .data
            .first()[KEY_USER_NAME]

    suspend fun getUserEmail(): String? =
        context.dataStore
            .data
            .first()[KEY_USER_EMAIL]

    suspend fun getAccessToken(): String? =
        context.dataStore
            .data
            .first()[KEY_ACCESS_TOKEN]

    suspend fun getTokenType(): String =
        context.dataStore
            .data
            .first()[KEY_TOKEN_TYPE]
            ?: "Bearer"

    suspend fun getTokenExpiresAt(): String? =
        context.dataStore
            .data
            .first()[KEY_TOKEN_EXPIRES_AT]

    suspend fun isLoggedIn(): Boolean {

        val preferences =
            context.dataStore
                .data
                .first()

        val userId =
            preferences[KEY_USER_ID]

        val token =
            preferences[KEY_ACCESS_TOKEN]

        val expiresAt =
            preferences[KEY_TOKEN_EXPIRES_AT]

        if (
            userId == null ||
            userId <= 0 ||
            token.isNullOrBlank() ||
            expiresAt.isNullOrBlank()
        ) {
            return false
        }

        val tokenValido =
            runCatching {

                Instant
                    .parse(expiresAt)
                    .isAfter(
                        Instant.now()
                    )

            }.getOrDefault(false)

        return tokenValido
    }

    suspend fun hasCompletedOnboarding(): Boolean =
        context.dataStore
            .data
            .first()[KEY_ONBOARDING_COMPLETED]
            ?: false

    suspend fun completeOnboarding() {

        context.dataStore.edit {
            it[KEY_ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun logout() {

        context.dataStore.edit { preferences ->

            val onboarding =
                preferences[KEY_ONBOARDING_COMPLETED]
                    ?: false

            preferences.clear()

            preferences[KEY_ONBOARDING_COMPLETED] =
                onboarding
        }
    }
}