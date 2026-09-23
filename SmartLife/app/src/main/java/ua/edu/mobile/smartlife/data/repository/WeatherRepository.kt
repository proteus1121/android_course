package ua.edu.mobile.smartlife.data.repository

import ua.edu.mobile.smartlife.data.model.Weather
import ua.edu.mobile.smartlife.data.model.weatherCodeToText
import ua.edu.mobile.smartlife.data.remote.WeatherApi

interface WeatherRepository {
    /** Кидає виняток (IOException, HttpException), якщо запит не вдався. */
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): Weather
}

class NetworkWeatherRepository(
    private val api: WeatherApi
) : WeatherRepository {

    override suspend fun getCurrentWeather(latitude: Double, longitude: Double): Weather {
        val response = api.getCurrentWeather(latitude, longitude)
        // Перетворюємо DTO на модель застосунку
        return Weather(
            temperature = response.current.temperature,
            feelsLike = response.current.feelsLike,
            humidity = response.current.humidity,
            windSpeedKmh = response.current.windSpeed,
            description = weatherCodeToText(response.current.weatherCode),
            weatherCode = response.current.weatherCode,
            latitude = response.latitude,
            longitude = response.longitude
        )
    }
}
