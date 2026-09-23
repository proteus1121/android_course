package ua.edu.mobile.smartlife.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Сесія користувача: хто увійшов і якими токенами підтверджує запити. */
data class Session(
    val userId: Long,
    val username: String,
    val fullName: String,
    val email: String,
    val accessToken: String,
    val refreshToken: String
)

// Окремий файл DataStore для сесії (не змішуємо з налаштуваннями)
private val Context.sessionStore: DataStore<Preferences> by preferencesDataStore(name = "session")

/**
 * Збереження сесії між запусками застосунку.
 * УВАГА: у цій версії токени лежать у відкритому вигляді — у розділі 16 ми їх зашифруємо.
 */
class SessionStorage(private val context: Context) {

    private object Keys {
        val USER_ID = longPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val FULL_NAME = stringPreferencesKey("full_name")
        val EMAIL = stringPreferencesKey("email")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    /** null — користувач не увійшов. */
    val session: Flow<Session?> = context.sessionStore.data.map { prefs ->
        val access = prefs[Keys.ACCESS_TOKEN] ?: return@map null
        Session(
            userId = prefs[Keys.USER_ID] ?: 0,
            username = prefs[Keys.USERNAME].orEmpty(),
            fullName = prefs[Keys.FULL_NAME].orEmpty(),
            email = prefs[Keys.EMAIL].orEmpty(),
            accessToken = access,
            refreshToken = prefs[Keys.REFRESH_TOKEN].orEmpty()
        )
    }

    suspend fun save(session: Session) {
        context.sessionStore.edit {
            it[Keys.USER_ID] = session.userId
            it[Keys.USERNAME] = session.username
            it[Keys.FULL_NAME] = session.fullName
            it[Keys.EMAIL] = session.email
            it[Keys.ACCESS_TOKEN] = session.accessToken
            it[Keys.REFRESH_TOKEN] = session.refreshToken
        }
    }

    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        context.sessionStore.edit {
            it[Keys.ACCESS_TOKEN] = accessToken
            it[Keys.REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun clear() {
        context.sessionStore.edit { it.clear() }
    }
}
