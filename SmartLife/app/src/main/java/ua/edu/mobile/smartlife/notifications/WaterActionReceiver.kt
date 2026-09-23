package ua.edu.mobile.smartlife.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ua.edu.mobile.smartlife.SmartLifeApplication
import ua.edu.mobile.smartlife.data.model.HealthRecord
import ua.edu.mobile.smartlife.data.model.RecordType

/**
 * Отримує натискання кнопки «Випив склянку» у сповіщенні.
 * Працює навіть тоді, коли застосунок закрито: Android запустить процес лише для цього виклику.
 */
class WaterActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DRINK_WATER) return
        val container = (context.applicationContext as SmartLifeApplication).container

        // goAsync() дозволяє завершити роботу у фоні (до ~10 с), не блокуючи головний потік
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                container.recordRepository.addRecord(
                    HealthRecord(type = RecordType.WATER, value = 0.25, note = "Зі сповіщення")
                )
                container.notificationHelper.cancelWaterReminder()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_DRINK_WATER = "ua.edu.mobile.smartlife.action.DRINK_WATER"
    }
}
