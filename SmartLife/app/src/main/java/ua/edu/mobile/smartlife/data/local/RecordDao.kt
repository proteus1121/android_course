package ua.edu.mobile.smartlife.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) — набір операцій з таблицею records.
 * Room сам згенерує реалізацію цих методів під час збірки (через KSP).
 */
@Dao
interface RecordDao {

    // READ: Flow автоматично надсилає новий список після кожної зміни таблиці
    @Query("SELECT * FROM records ORDER BY created_at DESC")
    fun observeAll(): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE id = :id")
    fun observeById(id: Long): Flow<RecordEntity?>

    // CREATE: повертає id нового рядка
    @Insert
    suspend fun insert(record: RecordEntity): Long

    // UPDATE: шукає рядок за первинним ключем (id)
    @Update
    suspend fun update(record: RecordEntity)

    // DELETE
    @Query("DELETE FROM records WHERE id = :id")
    suspend fun deleteById(id: Long)
}
