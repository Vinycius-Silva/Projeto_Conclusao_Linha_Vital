package com.linhavital.app.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {

    companion object {
        val KEY_USER_ID = longPreferencesKey("user_id")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
    }

    suspend fun salvarSessao(
        id: Long,
        nome: String,
        email: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = id
            prefs[KEY_USER_NAME] = nome
            prefs[KEY_USER_EMAIL] = email
        }
    }

    suspend fun getUserId(): Long? {
        return context.dataStore.data.first()[KEY_USER_ID]
    }

    suspend fun getUserName(): String? {
        return context.dataStore.data.first()[KEY_USER_NAME]
    }

    suspend fun isLoggedIn(): Boolean {
        return context.dataStore.data.first()[KEY_USER_ID] != null
    }

    suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}