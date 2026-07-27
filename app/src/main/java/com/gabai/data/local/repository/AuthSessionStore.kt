package com.gabai.data.local.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gabai.data.remote.models.AuthResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "auth_session")

private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
private val KEY_EXPIRES_AT = longPreferencesKey("expires_at")
private val KEY_USER_ID = stringPreferencesKey("user_id")
private val KEY_EMAIL = stringPreferencesKey("email")
private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")

data class StoredSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val email: String,
    val displayName: String
)

class AuthSessionStore(private val context: Context) {

    suspend fun saveSession(auth: AuthResponse) {
        context.authDataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = auth.accessToken
            prefs[KEY_REFRESH_TOKEN] = auth.refreshToken
            prefs[KEY_EXPIRES_AT] = auth.expiresAt ?: 0L
            prefs[KEY_USER_ID] = auth.user.id
            prefs[KEY_EMAIL] = auth.user.email
            prefs[KEY_DISPLAY_NAME] = auth.user.displayName
        }
    }

    suspend fun clearSession() {
        context.authDataStore.edit { it.clear() }
    }

    val session: Flow<StoredSession?> = context.authDataStore.data.map { prefs ->
        val accessToken = prefs[KEY_ACCESS_TOKEN] ?: return@map null
        val refreshToken = prefs[KEY_REFRESH_TOKEN] ?: return@map null
        StoredSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = prefs[KEY_USER_ID].orEmpty(),
            email = prefs[KEY_EMAIL].orEmpty(),
            displayName = prefs[KEY_DISPLAY_NAME].orEmpty()
        )
    }

    suspend fun accessTokenOnce(): String? = session.first()?.accessToken
}
