package ua.edu.mobile.smartlife.data.model

/** Тип показника, який користувач записує у щоденник. */
enum class RecordType(val title: String, val unit: String) {
    WATER("Вода", "л"),
    PULSE("Пульс", "уд/хв"),
    WEIGHT("Вага", "кг"),
    SLEEP("Сон", "год"),
    MOOD("Настрій", "бал")
}

/**
 * Один запис щоденника здоров'я.
 * latitude/longitude — місце, де зроблено запис (знадобиться в розділах 9–10).
 */
data class HealthRecord(
    val id: Long = 0,
    val type: RecordType,
    val value: Double,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null
)
