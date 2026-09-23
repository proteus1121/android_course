package ua.edu.mobile.smartlife.ui.records

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ua.edu.mobile.smartlife.MainDispatcherRule
import ua.edu.mobile.smartlife.data.model.HealthRecord
import ua.edu.mobile.smartlife.data.model.RecordType
import ua.edu.mobile.smartlife.data.repository.InMemoryRecordRepository

class RecordDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun repositoryWithOneRecord() = InMemoryRecordRepository(
        listOf(HealthRecord(id = 1, type = RecordType.PULSE, value = 70.0, note = "ранок"))
    )

    /** Так навігація передає аргумент маршруту RecordDetailsRoute(recordId = 1). */
    private fun handleFor(id: Long) = SavedStateHandle(mapOf("recordId" to id))

    @Test
    fun `loads record by id from navigation argument`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = RecordDetailsViewModel(handleFor(1), repositoryWithOneRecord())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(70.0, state.record!!.value, 0.0)
    }

    @Test
    fun `unknown id shows not found`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = RecordDetailsViewModel(handleFor(42), repositoryWithOneRecord())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.record)
    }

    @Test
    fun `update changes value and keeps id`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = repositoryWithOneRecord()
        val viewModel = RecordDetailsViewModel(handleFor(1), repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.updateRecord(RecordType.PULSE, 64.0, "після відпочинку")
        advanceUntilIdle()

        val updated = repository.observeRecord(1).first()!!
        assertEquals(64.0, updated.value, 0.0)
        assertEquals("після відпочинку", updated.note)
    }

    @Test
    fun `delete removes record and calls callback`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = repositoryWithOneRecord()
        val viewModel = RecordDetailsViewModel(handleFor(1), repository)
        var deletedCallbackCalled = false

        viewModel.deleteRecord { deletedCallbackCalled = true }
        advanceUntilIdle()

        assertTrue(deletedCallbackCalled)
        assertTrue(repository.observeRecords().first().isEmpty())
    }
}
