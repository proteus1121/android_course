package ua.edu.mobile.smartlife.di

import android.content.Context
import androidx.room.Room
import ua.edu.mobile.smartlife.data.local.AppDatabase
import ua.edu.mobile.smartlife.data.repository.RecordRepository
import ua.edu.mobile.smartlife.data.repository.RoomRecordRepository
import ua.edu.mobile.smartlife.data.settings.SettingsRepository

/**
 * Контейнер залежностей (ручний Dependency Injection):
 * тут в одному місці створюються всі репозиторії застосунку.
 */
class AppContainer(private val context: Context) {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "smart_life.db").build()
    }

    // by lazy — об'єкт створюється при першому зверненні і далі перевикористовується
    val recordRepository: RecordRepository by lazy {
        RoomRecordRepository(database.recordDao())
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context)
    }
}
