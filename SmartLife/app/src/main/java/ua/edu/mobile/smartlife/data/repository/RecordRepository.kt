package ua.edu.mobile.smartlife.data.repository

import kotlinx.coroutines.flow.Flow
import ua.edu.mobile.smartlife.data.model.HealthRecord

/**
 * Репозиторій — єдине джерело даних про записи для решти застосунку.
 * ViewModel не знає, ЗВІДКИ беруться дані (пам'ять, база даних, сервер) — лише ЩО можна зробити.
 */
interface RecordRepository {
    /** Усі записи, від найновішого до найстарішого. Flow сам повідомляє про зміни. */
    fun observeRecords(): Flow<List<HealthRecord>>

    fun observeRecord(id: Long): Flow<HealthRecord?>

    suspend fun addRecord(record: HealthRecord): Long

    suspend fun updateRecord(record: HealthRecord)

    suspend fun deleteRecord(id: Long)
}
