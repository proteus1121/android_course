package ua.edu.mobile.smartlife.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class DailySummaryTest {

    // Фіксований "зараз": 23.09.2026 12:00 — тест не залежить від реального часу
    private val now = Calendar.getInstance().apply {
        set(2026, Calendar.SEPTEMBER, 23, 12, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    private val hour = 60 * 60 * 1000L

    @Test
    fun `water is summed only for today`() {
        val records = listOf(
            HealthRecord(1, RecordType.WATER, 0.5, timestamp = now - 1 * hour),
            HealthRecord(2, RecordType.WATER, 0.25, timestamp = now - 2 * hour),
            HealthRecord(3, RecordType.WATER, 1.0, timestamp = now - 20 * hour) // учора
        )

        val summary = records.toDailySummary(now)

        assertEquals(0.75, summary.waterLiters, 0.0001)
    }

    @Test
    fun `latest pulse is taken from the newest record`() {
        val records = listOf(
            HealthRecord(1, RecordType.PULSE, 70.0, timestamp = now - 5 * hour),
            HealthRecord(2, RecordType.PULSE, 82.0, timestamp = now - 1 * hour),
            HealthRecord(3, RecordType.PULSE, 64.0, timestamp = now - 30 * hour)
        )

        assertEquals(82.0, records.toDailySummary(now).lastPulse)
    }

    @Test
    fun `empty list gives empty summary`() {
        val summary = emptyList<HealthRecord>().toDailySummary(now)

        assertEquals(0.0, summary.waterLiters, 0.0)
        assertNull(summary.lastPulse)
        assertNull(summary.lastSleep)
        assertNull(summary.lastWeight)
    }
}
