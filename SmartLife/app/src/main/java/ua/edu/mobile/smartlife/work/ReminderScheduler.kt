package ua.edu.mobile.smartlife.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

/** Стан періодичної задачі для відображення в UI. */
data class ReminderStatus(
    val state: WorkInfo.State?,        // ENQUEUED, RUNNING, CANCELLED, ...
    val nextRunAt: Long?               // коли WorkManager планує наступний запуск
)

/** Планування та скасування фонових нагадувань через WorkManager. */
class ReminderScheduler(context: Context) {

    private val workManager = WorkManager.getInstance(context)

    /**
     * Періодична задача. Мінімальний інтервал, дозволений Android, — 15 хвилин.
     * enqueueUniquePeriodicWork гарантує, що задача з таким ім'ям існує лише в одному екземплярі.
     */
    fun schedule(intervalMinutes: Long) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true) // не турбуємо, коли батарея майже розряджена
            .build()
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(intervalMinutes, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .addTag(ReminderWorker.TAG)
            .build()
        workManager.enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE, // якщо вже є — оновити параметри
            request
        )
    }

    fun cancel() {
        workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    /** Одноразовий запуск тієї ж задачі — зручно для перевірки, не чекаючи 15 хвилин. */
    fun runOnceNow() {
        workManager.enqueue(OneTimeWorkRequestBuilder<ReminderWorker>().build())
    }

    /** Стан задачі як Flow: UI оновлюється сам, коли WorkManager змінює стан. */
    fun observeStatus(): Flow<ReminderStatus> =
        workManager.getWorkInfosForUniqueWorkFlow(UNIQUE_WORK_NAME).map { infos ->
            val info = infos.firstOrNull()
            ReminderStatus(
                state = info?.state,
                nextRunAt = info?.nextScheduleTimeMillis?.takeIf { it != Long.MAX_VALUE }
            )
        }

    companion object {
        const val UNIQUE_WORK_NAME = "water_reminder"
    }
}
