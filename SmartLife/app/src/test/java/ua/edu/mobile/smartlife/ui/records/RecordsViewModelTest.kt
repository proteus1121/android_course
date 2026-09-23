package ua.edu.mobile.smartlife.ui.records

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import ua.edu.mobile.smartlife.MainDispatcherRule
import ua.edu.mobile.smartlife.data.model.RecordType
import ua.edu.mobile.smartlife.data.repository.InMemoryRecordRepository
import ua.edu.mobile.smartlife.location.LocationData
import ua.edu.mobile.smartlife.location.LocationSource
import ua.edu.mobile.smartlife.notifications.HealthAlerts

/** Фейк GPS: повертає заздалегідь задані координати. */
private class FakeLocationSource(private val location: LocationData?) : LocationSource {
    override suspend fun getLastLocation(): LocationData? = location
}

/** Фейк сповіщень: не показує нічого, лише запам'ятовує виклики. */
private class FakeHealthAlerts : HealthAlerts {
    val shownPulses = mutableListOf<Int>()
    override fun showHighPulseAlert(bpm: Int) {
        shownPulses += bpm
    }
}

class RecordsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val lviv = LocationData(49.8397, 24.0297, 5f, null, null, 0L)

    @Test
    fun `added record is saved with current location`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = InMemoryRecordRepository()
        val viewModel = RecordsViewModel(repository, FakeLocationSource(lviv), FakeHealthAlerts())

        viewModel.addRecord(RecordType.WATER, 0.5, "тест")
        advanceUntilIdle() // чекаємо завершення корутини у viewModelScope

        val saved = repository.observeRecords().first().single()
        assertEquals(RecordType.WATER, saved.type)
        assertEquals(0.5, saved.value, 0.0)
        assertEquals(49.8397, saved.latitude!!, 0.0001)
    }

    @Test
    fun `record without location permission has no coordinates`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = InMemoryRecordRepository()
        val viewModel = RecordsViewModel(repository, FakeLocationSource(null), FakeHealthAlerts())

        viewModel.addRecord(RecordType.SLEEP, 7.5, "")
        advanceUntilIdle()

        assertNull(repository.observeRecords().first().single().latitude)
    }

    @Test
    fun `high pulse shows alert, normal pulse does not`() = runTest(mainDispatcherRule.testDispatcher) {
        val alerts = FakeHealthAlerts()
        val viewModel = RecordsViewModel(InMemoryRecordRepository(), FakeLocationSource(null), alerts)

        viewModel.addRecord(RecordType.PULSE, 72.0, "")
        viewModel.addRecord(RecordType.PULSE, 118.0, "")
        advanceUntilIdle()

        assertEquals(listOf(118), alerts.shownPulses)
    }

    @Test
    fun `records state flow emits repository data`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = InMemoryRecordRepository()
        val viewModel = RecordsViewModel(repository, FakeLocationSource(null), FakeHealthAlerts())
        // stateIn(WhileSubscribed) починає працювати лише коли є підписник
        val collected = mutableListOf<Int>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.records.collect { collected += it.size } }

        viewModel.addRecord(RecordType.MOOD, 5.0, "")
        advanceUntilIdle()

        assertEquals(1, collected.last())
    }
}
