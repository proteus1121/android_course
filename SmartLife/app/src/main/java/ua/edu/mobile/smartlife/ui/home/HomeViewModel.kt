package ua.edu.mobile.smartlife.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ua.edu.mobile.smartlife.data.model.DailySummary
import ua.edu.mobile.smartlife.data.model.HealthRecord
import ua.edu.mobile.smartlife.data.model.RecordType
import ua.edu.mobile.smartlife.data.model.toDailySummary
import ua.edu.mobile.smartlife.data.remote.toUserMessage
import ua.edu.mobile.smartlife.data.repository.RecordRepository
import ua.edu.mobile.smartlife.data.repository.WeatherRepository
import ua.edu.mobile.smartlife.data.settings.SettingsRepository
import ua.edu.mobile.smartlife.location.LocationClient

/** Усе, що потрібно головному екрану, в одному об'єкті. */
data class HomeUiState(
    val userName: String = "",
    val summary: DailySummary = DailySummary(),
    val recentRecords: List<HealthRecord> = emptyList(),
    val waterGoalLiters: Double = 2.0
)

class HomeViewModel(
    private val recordRepository: RecordRepository,
    settingsRepository: SettingsRepository,
    private val weatherRepository: WeatherRepository,
    private val locationClient: LocationClient
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

    // Стан погоди: Loading -> Success або Error
    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    init {
        loadWeather()
    }

    fun loadWeather() {
        viewModelScope.launch {
            _weatherState.value = WeatherUiState.Loading
            _weatherState.value = try {
                // Якщо є дозвіл — беремо координати пристрою, інакше типові (Київ)
                val location = runCatching {
                    locationClient.getLastLocation() ?: locationClient.getCurrentLocation()
                }.getOrNull()
                val weather = weatherRepository.getCurrentWeather(
                    latitude = location?.latitude ?: KYIV_LATITUDE,
                    longitude = location?.longitude ?: KYIV_LONGITUDE
                )
                WeatherUiState.Success(
                    weather = weather,
                    placeLabel = if (location != null) "Ваше місце" else "Київ (типово)"
                )
            } catch (e: CancellationException) {
                throw e // скасування корутини не є помилкою — передаємо його далі
            } catch (e: Exception) {
                WeatherUiState.Error(e.toUserMessage())
            }
        }
    }

    fun addWaterGlass() {
        viewModelScope.launch {
            val location = runCatching { locationClient.getLastLocation() }.getOrNull()
            recordRepository.addRecord(
                HealthRecord(
                    type = RecordType.WATER,
                    value = 0.25,
                    note = "Склянка води",
                    latitude = location?.latitude,
                    longitude = location?.longitude
                )
            )
        }
    }
}

private const val KYIV_LATITUDE = 50.4501
private const val KYIV_LONGITUDE = 30.5234
