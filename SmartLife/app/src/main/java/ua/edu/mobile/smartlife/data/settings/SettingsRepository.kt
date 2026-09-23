package ua.edu.mobile.smartlife.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Режим теми оформлення. */
enum class ThemeMode(val title: String) {
    SYSTEM("Як у системі"),
    LIGHT("Світла"),
    DARK("Темна")
}

/** Налаштування користувача, які зберігаються між запусками. */
data class UserSettings(
    val userName: String = "Користувач",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val waterGoalLiters: Double = 2.0,
    val profilePhotoPath: String? = null,
    val remindersEnabled: Boolean = false,
    val reminderIntervalMinutes: Long = 60
)

// Один DataStore на файл: створюємо як розширення Context (файл settings.preferences_pb)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** Репозиторій налаштувань на базі Jetpack DataStore (Preferences). */
class SettingsRepository(private val context: Context) {

    // Ключі — "назви полів" у сховищі
    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val WATER_GOAL = doublePreferencesKey("water_goal")
        val PROFILE_PHOTO = stringPreferencesKey("profile_photo_path")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val REMINDER_INTERVAL = longPreferencesKey("reminder_interval_minutes")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            userName = prefs[Keys.USER_NAME] ?: UserSettings().userName,
            themeMode = prefs[Keys.THEME_MODE]?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM,
            waterGoalLiters = prefs[Keys.WATER_GOAL] ?: UserSettings().waterGoalLiters,
            profilePhotoPath = prefs[Keys.PROFILE_PHOTO],
            remindersEnabled = prefs[Keys.REMINDERS_ENABLED] ?: false,
            reminderIntervalMinutes = prefs[Keys.REMINDER_INTERVAL] ?: UserSettings().reminderIntervalMinutes
        )
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[Keys.USER_NAME] = name }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setWaterGoal(liters: Double) {
        context.dataStore.edit { it[Keys.WATER_GOAL] = liters }
    }

    suspend fun setProfilePhotoPath(path: String?) {
        context.dataStore.edit { prefs ->
            if (path == null) prefs.remove(Keys.PROFILE_PHOTO) else prefs[Keys.PROFILE_PHOTO] = path
        }
    }

    suspend fun setReminders(enabled: Boolean, intervalMinutes: Long) {
        context.dataStore.edit {
            it[Keys.REMINDERS_ENABLED] = enabled
            it[Keys.REMINDER_INTERVAL] = intervalMinutes
        }
    }
}
