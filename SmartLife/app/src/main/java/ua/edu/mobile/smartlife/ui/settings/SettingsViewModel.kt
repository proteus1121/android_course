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
import ua.edu.mobile.smartlife.work.ReminderScheduler
import ua.edu.mobile.smartlife.work.ReminderStatus

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val notificationHelper: NotificationHelper,
    private val reminderScheduler: ReminderScheduler
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

    /** Стан фонової задачі з WorkManager. */
    val reminderStatus: StateFlow<ReminderStatus?> = reminderScheduler.observeStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Увімкнути/вимкнути періодичні нагадування або змінити їх інтервал. */
    fun setReminders(enabled: Boolean, intervalMinutes: Long) {
        viewModelScope.launch {
            settingsRepository.setReminders(enabled, intervalMinutes)
            if (enabled) reminderScheduler.schedule(intervalMinutes) else reminderScheduler.cancel()
        }
    }

    fun runReminderNow() = reminderScheduler.runOnceNow()

    fun sendTestNotification() {
        notificationHelper.showWaterReminder("Тестове нагадування: час випити склянку води 💧")
    }
}
