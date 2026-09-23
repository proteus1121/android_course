package ua.edu.mobile.smartlife.ui.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.edu.mobile.smartlife.location.LocationClient
import ua.edu.mobile.smartlife.location.LocationData

data class LocationUiState(
    val isLoading: Boolean = false,
    val isTracking: Boolean = false,
    val location: LocationData? = null,
    val updatesCount: Int = 0,
    val error: String? = null
)

class LocationViewModel(
    private val locationClient: LocationClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    private var trackingJob: Job? = null

    /** Одноразове визначення поточного місцезнаходження. */
    fun requestCurrentLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val location = locationClient.getCurrentLocation()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        location = location ?: it.location,
                        error = if (location == null) "Не вдалося визначити місцезнаходження. Чи увімкнено геолокацію?" else null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /** Увімкнути/вимкнути безперервне відстеження. */
    fun toggleTracking() {
        if (trackingJob?.isActive == true) {
            trackingJob?.cancel()
            _uiState.update { it.copy(isTracking = false) }
            return
        }
        _uiState.update { it.copy(isTracking = true, updatesCount = 0, error = null) }
        trackingJob = viewModelScope.launch {
            locationClient.locationUpdates(intervalMs = 5_000)
                .catch { e -> _uiState.update { it.copy(isTracking = false, error = e.message) } }
                .collect { location ->
                    _uiState.update { it.copy(location = location, updatesCount = it.updatesCount + 1) }
                }
        }
    }
}
