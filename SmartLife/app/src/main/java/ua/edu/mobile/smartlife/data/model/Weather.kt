package ua.edu.mobile.smartlife.data.model

/** Модель погоди, яку використовує UI (не залежить від формату JSON). */
data class Weather(
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeedKmh: Double,
    val description: String,
    val weatherCode: Int,
    val latitude: Double,
    val longitude: Double
)

/** Розшифровка коду погоди WMO (https://open-meteo.com/en/docs, розділ "WMO Weather interpretation codes"). */
fun weatherCodeToText(code: Int): String = when (code) {
    0 -> "Ясно"
    1, 2 -> "Мінлива хмарність"
    3 -> "Хмарно"
    45, 48 -> "Туман"
    51, 53, 55, 56, 57 -> "Мряка"
    61, 63, 65, 66, 67 -> "Дощ"
    71, 73, 75, 77 -> "Сніг"
    80, 81, 82 -> "Злива"
    85, 86 -> "Снігопад"
    95, 96, 99 -> "Гроза"
    else -> "Невідомо"
}
