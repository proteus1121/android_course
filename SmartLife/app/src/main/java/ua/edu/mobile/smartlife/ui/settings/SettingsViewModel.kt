package ua.edu.mobile.smartlife.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ua.edu.mobile.smartlife.data.settings.SettingsRepository
import ua.edu.mobile.smartlife.data.settings.ThemeMode
import ua.edu.mobile.smartlife.data.settings.UserSettings
import ua.edu.mobile.smartlife.notifications.NotificationHelper

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    val settings: StateFlow<UserSettings?> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setUserName(name: String) {
        viewModelScope.launch { settingsRepository.setUserName(name.trim()) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setWaterGoal(liters: Double) {
        viewModelScope.launch { settingsRepository.setWaterGoal(liters) }
    }

    fun sendTestNotification() {
        notificationHelper.showWaterReminder("Тестове нагадування: час випити склянку води 💧")
    }
}
