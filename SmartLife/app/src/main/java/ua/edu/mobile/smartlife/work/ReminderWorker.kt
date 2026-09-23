package ua.edu.mobile.smartlife.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import ua.edu.mobile.smartlife.SmartLifeApplication
import ua.edu.mobile.smartlife.data.model.toDailySummary

/**
 * Фонова задача, яку WorkManager запускає періодично — навіть коли застосунок закрито.
 * CoroutineWorker дозволяє писати звичайний suspend-код у doWork().
 */
class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as SmartLifeApplication).container
        return try {
            // first() — беремо поточне значення з Flow і не чекаємо наступних
            val records = container.recordRepository.observeRecords().first()
            val settings = container.settingsRepository.settings.first()
            val drunk = records.toDailySummary().waterLiters
            val goal = settings.waterGoalLiters

            Log.d(TAG, "Перевірка води: $drunk з $goal л")
            if (drunk < goal) {
                container.notificationHelper.showWaterReminder(
                    "Сьогодні випито %.2f з %.2f л. Час випити склянку води 💧".format(drunk, goal)
                )
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Помилка фонової задачі", e)
            // retry() — WorkManager повторить спробу пізніше (з експоненційною затримкою)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val TAG = "ReminderWorker"
    }
}
