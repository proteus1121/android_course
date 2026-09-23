package ua.edu.mobile.smartlife.ui.home

import ua.edu.mobile.smartlife.data.model.Weather

/**
 * Три можливі стани мережевого запиту.
 * sealed interface гарантує, що when у UI обробить УСІ варіанти.
 */
sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val weather: Weather) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}
