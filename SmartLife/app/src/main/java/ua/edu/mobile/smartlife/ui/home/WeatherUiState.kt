package ua.edu.mobile.smartlife.ui.home

import ua.edu.mobile.smartlife.data.model.Weather

/**
 * Три можливі стани мережевого запиту.
 * sealed interface гарантує, що when у UI обробить УСІ варіанти.
 */
sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    /** placeLabel — звідки взято координати: з GPS чи типові (Київ). */
    data class Success(val weather: Weather, val placeLabel: String) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}
