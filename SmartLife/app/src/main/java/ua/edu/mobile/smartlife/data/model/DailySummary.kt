package ua.edu.mobile.smartlife.data.model

import java.util.Calendar

/** Підсумок показників за сьогодні для головного екрана. */
data class DailySummary(
    val waterLiters: Double = 0.0,
    val lastPulse: Double? = null,
    val lastSleep: Double? = null,
    val lastWeight: Double? = null
)

/** Обчислює підсумок зі списку записів. now передаємо параметром — так функцію легко тестувати. */
fun List<HealthRecord>.toDailySummary(now: Long = System.currentTimeMillis()): DailySummary {
    val startOfDay = Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val sorted = sortedByDescending { it.timestamp }
    fun latest(type: RecordType) = sorted.firstOrNull { it.type == type }?.value

    return DailySummary(
        waterLiters = filter { it.type == RecordType.WATER && it.timestamp >= startOfDay }
            .sumOf { it.value },
        lastPulse = latest(RecordType.PULSE),
        lastSleep = latest(RecordType.SLEEP),
        lastWeight = latest(RecordType.WEIGHT)
    )
}
