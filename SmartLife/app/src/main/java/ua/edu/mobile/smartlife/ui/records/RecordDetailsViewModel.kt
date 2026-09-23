package ua.edu.mobile.smartlife.ui.records

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ua.edu.mobile.smartlife.data.model.HealthRecord
import ua.edu.mobile.smartlife.data.model.RecordType
import ua.edu.mobile.smartlife.data.repository.RecordRepository

data class RecordDetailsUiState(
    val isLoading: Boolean = true,
    val record: HealthRecord? = null
)

class RecordDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val recordRepository: RecordRepository
) : ViewModel() {

    // Аргумент навігації RecordDetailsRoute(recordId) автоматично потрапляє в SavedStateHandle
    private val recordId: Long = checkNotNull(savedStateHandle["recordId"])

    val uiState: StateFlow<RecordDetailsUiState> = recordRepository.observeRecord(recordId)
        .map { RecordDetailsUiState(isLoading = false, record = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecordDetailsUiState())

    fun updateRecord(type: RecordType, value: Double, note: String) {
        val current = uiState.value.record ?: return
        viewModelScope.launch {
            recordRepository.updateRecord(current.copy(type = type, value = value, note = note))
        }
    }

    fun deleteRecord(onDeleted: () -> Unit) {
        viewModelScope.launch {
            recordRepository.deleteRecord(recordId)
            onDeleted()
        }
    }
}
