package ua.edu.mobile.smartlife.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ua.edu.mobile.smartlife.SmartLifeApplication
import ua.edu.mobile.smartlife.ui.home.HomeViewModel
import ua.edu.mobile.smartlife.ui.profile.ProfileViewModel
import ua.edu.mobile.smartlife.ui.records.RecordDetailsViewModel
import ua.edu.mobile.smartlife.ui.records.RecordsViewModel
import ua.edu.mobile.smartlife.ui.settings.SettingsViewModel

/**
 * Фабрика ViewModel: пояснює Android, ЯК створити кожну ViewModel
 * і які залежності (репозиторії) їй передати.
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val container = smartLifeApplication().container
            HomeViewModel(
                container.recordRepository,
                container.settingsRepository,
                container.weatherRepository
            )
        }
        initializer {
            RecordsViewModel(smartLifeApplication().container.recordRepository)
        }
        initializer {
            RecordDetailsViewModel(
                savedStateHandle = createSavedStateHandle(),
                recordRepository = smartLifeApplication().container.recordRepository
            )
        }
        initializer {
            SettingsViewModel(smartLifeApplication().container.settingsRepository)
        }
        initializer {
            ProfileViewModel(smartLifeApplication().container.settingsRepository)
        }
    }
}

/** Дістає наш клас Application з параметрів створення ViewModel. */
fun CreationExtras.smartLifeApplication(): SmartLifeApplication =
    this[AndroidViewModelFactory.APPLICATION_KEY] as SmartLifeApplication
