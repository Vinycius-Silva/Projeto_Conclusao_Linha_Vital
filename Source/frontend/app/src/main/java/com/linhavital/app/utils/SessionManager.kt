package com.linhavital.app.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {

    companion object {
        private val KEY_USER_ID = longPreferencesKey("user_id")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    suspend fun salvarSessao(id: Long, nome: String, email: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = id
            prefs[KEY_USER_NAME] = nome
            prefs[KEY_USER_EMAIL] = email
        }
    }

    suspend fun getUserId(): Long? = context.dataStore.data.first()[KEY_USER_ID]

    suspend fun getUserName(): String? = context.dataStore.data.first()[KEY_USER_NAME]

    suspend fun getUserEmail(): String? = context.dataStore.data.first()[KEY_USER_EMAIL]

    suspend fun isLoggedIn(): Boolean = getUserId() != null

    suspend fun hasCompletedOnboarding(): Boolean =
        context.dataStore.data.first()[KEY_ONBOARDING_COMPLETED] ?: false

    suspend fun completeOnboarding() {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = true }
    }

    suspend fun logout() {
        context.dataStore.edit { preferences ->
            val onboarding = preferences[KEY_ONBOARDING_COMPLETED] ?: false
            preferences.clear()
            preferences[KEY_ONBOARDING_COMPLETED] = onboarding
        }
    }
}
