package ua.edu.mobile.smartlife.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ua.edu.mobile.smartlife.SmartLifeApplication
import ua.edu.mobile.smartlife.ui.auth.LoginViewModel
import ua.edu.mobile.smartlife.ui.auth.RegisterViewModel
import ua.edu.mobile.smartlife.ui.ble.BleViewModel
import ua.edu.mobile.smartlife.ui.home.HomeViewModel
import ua.edu.mobile.smartlife.ui.location.LocationViewModel
import ua.edu.mobile.smartlife.ui.map.MapViewModel
import ua.edu.mobile.smartlife.ui.profile.ProfileViewModel
import ua.edu.mobile.smartlife.ui.records.RecordDetailsViewModel
import ua.edu.mobile.smartlife.ui.records.RecordsViewModel
import ua.edu.mobile.smartlife.ui.sensors.SensorsViewModel
import ua.edu.mobile.smartlife.ui.settings.SettingsViewModel

/**
 * Фабрика ViewModel: пояснює Android, ЯК створити кожну ViewModel
 * і які залежності (репозиторії) їй передати.
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            SessionViewModel(smartLifeApplication().container.authRepository)
        }
        initializer {
            val container = smartLifeApplication().container
            LoginViewModel(container.authRepository, container.settingsRepository)
        }
        initializer {
            RegisterViewModel(smartLifeApplication().container.authRepository)
        }
        initializer {
            val container = smartLifeApplication().container
            HomeViewModel(
                container.recordRepository,
                container.settingsRepository,
                container.weatherRepository,
                container.locationClient
            )
        }
        initializer {
            val container = smartLifeApplication().container
            RecordsViewModel(container.recordRepository, container.locationClient, container.notificationHelper)
        }
        initializer {
            RecordDetailsViewModel(
                savedStateHandle = createSavedStateHandle(),
                recordRepository = smartLifeApplication().container.recordRepository
            )
        }
        initializer {
            val container = smartLifeApplication().container
            BleViewModel(container.bleManager, container.recordRepository)
        }
        initializer {
            val container = smartLifeApplication().container
            MapViewModel(container.recordRepository, container.locationClient)
        }
        initializer {
            LocationViewModel(smartLifeApplication().container.locationClient)
        }
        initializer {
            SensorsViewModel(smartLifeApplication().container.sensorReader)
        }
        initializer {
            val container = smartLifeApplication().container
            SettingsViewModel(
                container.settingsRepository,
                container.notificationHelper,
                container.reminderScheduler
            )
        }
        initializer {
            val container = smartLifeApplication().container
            ProfileViewModel(container.settingsRepository, container.profilePhotoStorage, container.authRepository)
        }
    }
}

/** Дістає наш клас Application з параметрів створення ViewModel. */
fun CreationExtras.smartLifeApplication(): SmartLifeApplication =
    this[AndroidViewModelFactory.APPLICATION_KEY] as SmartLifeApplication
