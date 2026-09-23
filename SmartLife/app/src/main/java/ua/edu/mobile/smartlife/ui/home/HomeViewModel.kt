package ua.edu.mobile.smartlife.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ua.edu.mobile.smartlife.data.model.DailySummary
import ua.edu.mobile.smartlife.data.model.HealthRecord
import ua.edu.mobile.smartlife.data.model.RecordType
import ua.edu.mobile.smartlife.data.model.toDailySummary
import ua.edu.mobile.smartlife.data.repository.RecordRepository
import ua.edu.mobile.smartlife.data.settings.SettingsRepository

/** Усе, що потрібно головному екрану, в одному об'єкті. */
data class HomeUiState(
    val userName: String = "",
    val summary: DailySummary = DailySummary(),
    val recentRecords: List<HealthRecord> = emptyList(),
    val waterGoalLiters: Double = 2.0
)

class HomeViewModel(
    private val recordRepository: RecordRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    // combine об'єднує два потоки: новий стан з'являється, коли змінюється БУДЬ-ЯКИЙ з них
    val uiState: StateFlow<HomeUiState> = combine(
        recordRepository.observeRecords(),
        settingsRepository.settings
    ) { records, settings ->
        HomeUiState(
            userName = settings.userName,
            summary = records.toDailySummary(),
            recentRecords = records.take(3),
            waterGoalLiters = settings.waterGoalLiters
        )
    }
        // Перетворюємо Flow на StateFlow, який "живе" поки на екран хтось дивиться (+5 с запасу)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun addWaterGlass() {
        viewModelScope.launch {
            recordRepository.addRecord(
                HealthRecord(type = RecordType.WATER, value = 0.25, note = "Склянка води")
            )
        }
    }
}
