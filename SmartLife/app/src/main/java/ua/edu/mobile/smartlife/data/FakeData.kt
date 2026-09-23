package ua.edu.mobile.smartlife.data

import ua.edu.mobile.smartlife.data.model.HealthRecord
import ua.edu.mobile.smartlife.data.model.RecordType

/** Тимчасові демонстраційні дані для прототипу інтерфейсу (розділ 3). */
object FakeData {
    private const val HOUR = 60 * 60 * 1000L
    private val now = System.currentTimeMillis()

    val userName = "Олена"
    val userEmail = "olena@example.com"

    val records = listOf(
        HealthRecord(1, RecordType.WATER, 0.5, "Після пробіжки", now - 1 * HOUR),
        HealthRecord(2, RecordType.PULSE, 72.0, "У стані спокою", now - 2 * HOUR),
        HealthRecord(3, RecordType.SLEEP, 7.5, "", now - 8 * HOUR),
        HealthRecord(4, RecordType.WEIGHT, 64.2, "Ранкове зважування", now - 9 * HOUR),
        HealthRecord(5, RecordType.MOOD, 4.0, "Гарний день", now - 26 * HOUR),
        HealthRecord(6, RecordType.WATER, 0.3, "", now - 27 * HOUR)
    )
}
