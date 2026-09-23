package ua.edu.mobile.smartlife.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ua.edu.mobile.smartlife.data.model.HealthRecord
import ua.edu.mobile.smartlife.data.model.RecordType
import ua.edu.mobile.smartlife.data.repository.RecordRepository

/** ViewModel екрана «Журнал»: отримує дані з репозиторію, а не зберігає їх сама. */
class RecordsViewModel(
    private val recordRepository: RecordRepository
) : ViewModel() {

    val records: StateFlow<List<HealthRecord>> = recordRepository.observeRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addRecord(type: RecordType, value: Double, note: String) {
        viewModelScope.launch {
            recordRepository.addRecord(HealthRecord(type = type, value = value, note = note))
        }
    }
}
