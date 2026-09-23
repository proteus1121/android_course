package ua.edu.mobile.smartlife.di

import android.content.Context
import androidx.room.Room
import ua.edu.mobile.smartlife.ble.BleManager
import ua.edu.mobile.smartlife.data.local.AppDatabase
import ua.edu.mobile.smartlife.data.profile.ProfilePhotoStorage
import ua.edu.mobile.smartlife.data.remote.NetworkModule
import ua.edu.mobile.smartlife.data.remote.WeatherApi
import ua.edu.mobile.smartlife.data.repository.NetworkWeatherRepository
import ua.edu.mobile.smartlife.data.repository.RecordRepository
import ua.edu.mobile.smartlife.data.repository.RoomRecordRepository
import ua.edu.mobile.smartlife.data.repository.WeatherRepository
import ua.edu.mobile.smartlife.data.settings.SettingsRepository
import ua.edu.mobile.smartlife.location.LocationClient
import ua.edu.mobile.smartlife.notifications.NotificationHelper
import ua.edu.mobile.smartlife.sensors.SensorReader

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

    val weatherRepository: WeatherRepository by lazy {
        val api = NetworkModule.createRetrofit(WEATHER_BASE_URL).create(WeatherApi::class.java)
        NetworkWeatherRepository(api)
    }

    val bleManager: BleManager by lazy {
        BleManager(context)
    }

    val locationClient: LocationClient by lazy {
        LocationClient(context)
    }

    val profilePhotoStorage: ProfilePhotoStorage by lazy {
        ProfilePhotoStorage(context)
    }

    val sensorReader: SensorReader by lazy {
        SensorReader(context)
    }

    val notificationHelper: NotificationHelper by lazy {
        NotificationHelper(context)
    }

    private companion object {
        const val WEATHER_BASE_URL = "https://api.open-meteo.com/"
    }
}
