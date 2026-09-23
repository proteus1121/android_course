package ua.edu.mobile.smartlife.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ua.edu.mobile.smartlife.data.FakeData
import ua.edu.mobile.smartlife.data.model.DailySummary
import ua.edu.mobile.smartlife.data.model.HealthRecord
import ua.edu.mobile.smartlife.data.model.RecordType
import ua.edu.mobile.smartlife.data.model.toDailySummary
import ua.edu.mobile.smartlife.data.repository.RecordRepository

/** Усе, що потрібно головному екрану, в одному об'єкті. */
data class HomeUiState(
    val userName: String = "",
    val summary: DailySummary = DailySummary(),
    val recentRecords: List<HealthRecord> = emptyList()
)

class HomeViewModel(
    private val recordRepository: RecordRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = recordRepository.observeRecords()
        .map { records ->
            HomeUiState(
                userName = FakeData.userName,
                summary = records.toDailySummary(),
                recentRecords = records.take(3)
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
