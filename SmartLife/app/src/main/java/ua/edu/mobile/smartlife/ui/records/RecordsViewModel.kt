package ua.edu.mobile.smartlife.ui.records

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ua.edu.mobile.smartlife.data.FakeData
import ua.edu.mobile.smartlife.data.model.HealthRecord
import ua.edu.mobile.smartlife.data.model.RecordType

/**
 * ViewModel зберігає стан списку записів.
 * Вона переживає поворот екрана та спільна для всіх екранів (розділ 4).
 */
class RecordsViewModel : ViewModel() {

    // Приватний змінний стан — змінювати його може лише ViewModel
    private val _records = MutableStateFlow(FakeData.records)

    // Публічний стан лише для читання — UI підписується на нього
    val records: StateFlow<List<HealthRecord>> = _records.asStateFlow()

    fun addRecord(type: RecordType, value: Double, note: String) {
        _records.update { current ->
            val newId = (current.maxOfOrNull { it.id } ?: 0) + 1
            listOf(HealthRecord(id = newId, type = type, value = value, note = note)) + current
        }
    }

    /** Швидка дія з головного екрана: +0.25 л води. */
    fun addWaterGlass() = addRecord(RecordType.WATER, 0.25, "Склянка води")

    fun deleteRecord(id: Long) {
        _records.update { current -> current.filterNot { it.id == id } }
    }
}
