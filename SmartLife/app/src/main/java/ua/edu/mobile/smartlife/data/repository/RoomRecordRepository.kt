package ua.edu.mobile.smartlife.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ua.edu.mobile.smartlife.data.local.RecordDao
import ua.edu.mobile.smartlife.data.local.toEntity
import ua.edu.mobile.smartlife.data.local.toModel
import ua.edu.mobile.smartlife.data.model.HealthRecord

/** Реалізація репозиторію на базі Room: дані зберігаються між запусками. */
class RoomRecordRepository(
    private val dao: RecordDao
) : RecordRepository {

    override fun observeRecords(): Flow<List<HealthRecord>> =
        dao.observeAll().map { list -> list.map { it.toModel() } }

    override fun observeRecord(id: Long): Flow<HealthRecord?> =
        dao.observeById(id).map { it?.toModel() }

    override suspend fun addRecord(record: HealthRecord): Long =
        dao.insert(record.copy(id = 0).toEntity())

    override suspend fun updateRecord(record: HealthRecord) =
        dao.update(record.toEntity())

    override suspend fun deleteRecord(id: Long) =
        dao.deleteById(id)
}
