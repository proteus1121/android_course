package ua.edu.mobile.smartlife.di

import android.content.Context
import ua.edu.mobile.smartlife.data.FakeData
import ua.edu.mobile.smartlife.data.repository.InMemoryRecordRepository
import ua.edu.mobile.smartlife.data.repository.RecordRepository

/**
 * Контейнер залежностей (ручний Dependency Injection):
 * тут в одному місці створюються всі репозиторії застосунку.
 */
class AppContainer(private val context: Context) {

    // by lazy — об'єкт створюється при першому зверненні і далі перевикористовується
    val recordRepository: RecordRepository by lazy {
        InMemoryRecordRepository(FakeData.records)
    }
}
