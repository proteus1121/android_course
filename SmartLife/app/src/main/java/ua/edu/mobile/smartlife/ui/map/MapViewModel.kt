package ua.edu.mobile.smartlife.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ua.edu.mobile.smartlife.data.model.HealthRecord
import ua.edu.mobile.smartlife.data.repository.RecordRepository
import ua.edu.mobile.smartlife.location.LocationClient

/** Точка на карті (незалежно від бібліотеки карт). */
data class MapPoint(val latitude: Double, val longitude: Double)

class MapViewModel(
    recordRepository: RecordRepository,
    private val locationClient: LocationClient
) : ViewModel() {

    /** Лише ті записи, для яких відомі координати. */
    val recordsWithLocation: StateFlow<List<HealthRecord>> = recordRepository.observeRecords()
        .map { list -> list.filter { it.latitude != null && it.longitude != null } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _myLocation = MutableStateFlow<MapPoint?>(null)
    val myLocation: StateFlow<MapPoint?> = _myLocation.asStateFlow()

    /** Точка, яку користувач обрав дотиком до карти. */
    private val _selectedPoint = MutableStateFlow<MapPoint?>(null)
    val selectedPoint: StateFlow<MapPoint?> = _selectedPoint.asStateFlow()

    fun hasLocationPermission() = locationClient.hasPermission()

    fun refreshMyLocation() {
        viewModelScope.launch {
            val location = runCatching {
                locationClient.getCurrentLocation() ?: locationClient.getLastLocation()
            }.getOrNull()
            if (location != null) _myLocation.value = MapPoint(location.latitude, location.longitude)
        }
    }

    fun selectPoint(point: MapPoint?) {
        _selectedPoint.value = point
    }
}
