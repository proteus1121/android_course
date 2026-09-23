package ua.edu.mobile.smartlife.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity — опис таблиці бази даних. Кожна властивість — окремий стовпець.
 * Таблиця: records
 */
@Entity(tableName = "records")
data class RecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,               // назва RecordType, наприклад "WATER"
    val value: Double,
    val note: String,
    @ColumnInfo(name = "created_at") // ім'я стовпця може відрізнятися від імені властивості
    val timestamp: Long,
    val latitude: Double?,          // координати знадобляться в розділі 10 (карта)
    val longitude: Double?
)
