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
import ua.edu.mobile.smartlife.location.LocationSource
import ua.edu.mobile.smartlife.notifications.HealthAlerts
import ua.edu.mobile.smartlife.notifications.NotificationHelper

/** ViewModel екрана «Журнал»: отримує дані з репозиторію, а не зберігає їх сама. */
class RecordsViewModel(
    private val recordRepository: RecordRepository,
    private val locationSource: LocationSource,
    private val healthAlerts: HealthAlerts
) : ViewModel() {

    val records: StateFlow<List<HealthRecord>> = recordRepository.observeRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addRecord(type: RecordType, value: Double, note: String) {
        viewModelScope.launch {
            // Прив'язуємо запис до місця (якщо користувач дозволив геолокацію)
            val location = runCatching { locationSource.getLastLocation() }.getOrNull()
            recordRepository.addRecord(
                HealthRecord(
                    type = type,
                    value = value,
                    note = note,
                    latitude = location?.latitude,
                    longitude = location?.longitude
                )
            )
            // Високий пульс — показуємо попередження
            if (type == RecordType.PULSE && value >= NotificationHelper.HIGH_PULSE_THRESHOLD) {
                healthAlerts.showHighPulseAlert(value.toInt())
            }
        }
    }
}
