package ua.edu.mobile.smartlife.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Головний клас бази даних. Перелічуємо всі таблиці (entities) та версію схеми.
 * Екземпляр створюється в AppContainer через Room.databaseBuilder.
 */
@Database(entities = [RecordEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao
}
