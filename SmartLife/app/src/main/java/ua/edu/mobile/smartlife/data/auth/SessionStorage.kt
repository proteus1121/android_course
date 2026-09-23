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
import ua.edu.mobile.smartlife.data.security.TokenCipher

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
 * Токени зберігаються ЗАШИФРОВАНИМИ ключем з Android Keystore (розділ 16).
 */
class SessionStorage(
    private val context: Context,
    private val cipher: TokenCipher
) {

    private object Keys {
        val USER_ID = longPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val FULL_NAME = stringPreferencesKey("full_name")
        val EMAIL = stringPreferencesKey("email")
        val ACCESS_TOKEN = stringPreferencesKey("access_token_encrypted")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token_encrypted")
    }

    /** null — користувач не увійшов. */
    val session: Flow<Session?> = context.sessionStore.data.map { prefs ->
        // Не вдалося розшифрувати (ключ втрачено, дані пошкоджено) — вважаємо, що входу немає
        val access = prefs[Keys.ACCESS_TOKEN]?.let(cipher::decrypt) ?: return@map null
        Session(
            userId = prefs[Keys.USER_ID] ?: 0,
            username = prefs[Keys.USERNAME].orEmpty(),
            fullName = prefs[Keys.FULL_NAME].orEmpty(),
            email = prefs[Keys.EMAIL].orEmpty(),
            accessToken = access,
            refreshToken = prefs[Keys.REFRESH_TOKEN]?.let(cipher::decrypt).orEmpty()
        )
    }

    suspend fun save(session: Session) {
        context.sessionStore.edit {
            it[Keys.USER_ID] = session.userId
            it[Keys.USERNAME] = session.username
            it[Keys.FULL_NAME] = session.fullName
            it[Keys.EMAIL] = session.email
            it[Keys.ACCESS_TOKEN] = cipher.encrypt(session.accessToken)
            it[Keys.REFRESH_TOKEN] = cipher.encrypt(session.refreshToken)
        }
    }

    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        context.sessionStore.edit {
            it[Keys.ACCESS_TOKEN] = cipher.encrypt(accessToken)
            it[Keys.REFRESH_TOKEN] = cipher.encrypt(refreshToken)
        }
    }

    suspend fun clear() {
        context.sessionStore.edit { it.clear() }
    }
}
