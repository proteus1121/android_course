package ua.edu.mobile.smartlife.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ua.edu.mobile.smartlife.MainActivity
import ua.edu.mobile.smartlife.R

/**
 * Усе про сповіщення в одному місці: канали, перевірка дозволу, показ сповіщень.
 */
class NotificationHelper(private val context: Context) {

    private val manager = NotificationManagerCompat.from(context)

    /**
     * Канали (Android 8+) — категорії сповіщень. Користувач може вимкнути
     * або налаштувати кожен канал окремо в системних налаштуваннях.
     * Створювати канал повторно безпечно — система просто проігнорує дублікат.
     */
    fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val system = context.getSystemService(NotificationManager::class.java)
        system.createNotificationChannel(
            NotificationChannel(CHANNEL_REMINDERS, "Нагадування", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Нагадування пити воду та записувати показники" }
        )
        system.createNotificationChannel(
            NotificationChannel(CHANNEL_ALERTS, "Попередження про здоров'я", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Сповіщення про незвичні показники, наприклад високий пульс" }
        )
    }

    /** Чи можна зараз показувати сповіщення (дозвіл + не вимкнені користувачем). */
    fun canPostNotifications(): Boolean {
        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        return permissionGranted && manager.areNotificationsEnabled()
    }

    /** Нагадування випити води з кнопкою дії «Випив склянку». */
    fun showWaterReminder(text: String = "Час випити склянку води 💧") {
        // Дія кнопки: широкомовне повідомлення (broadcast) до нашого WaterActionReceiver
        val drinkIntent = Intent(context, WaterActionReceiver::class.java)
            .setAction(WaterActionReceiver.ACTION_DRINK_WATER)
        val drinkPendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_DRINK, drinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Smart Life")
            .setContentText(text)
            .setContentIntent(openAppIntent())     // натискання на сповіщення відкриває застосунок
            .setAutoCancel(true)                   // і прибирає сповіщення
            .addAction(R.drawable.ic_notification, "Випив склянку", drinkPendingIntent)
            .build()
        notify(ID_WATER_REMINDER, notification)
    }

    /** Попередження з високим пріоритетом (показується як спливаюче). */
    fun showHighPulseAlert(bpm: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Високий пульс: $bpm уд/хв")
            .setContentText("Відпочиньте кілька хвилин і виміряйте пульс повторно.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Пульс у стані спокою понад 100 уд/хв. Відпочиньте кілька хвилин і виміряйте повторно. " +
                        "Якщо погано почуваєтеся — зверніться до лікаря."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openAppIntent())
            .setAutoCancel(true)
            .build()
        notify(ID_PULSE_ALERT, notification)
    }

    fun cancelWaterReminder() = manager.cancel(ID_WATER_REMINDER)

    private fun openAppIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(
            context, REQUEST_OPEN_APP, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun notify(id: Int, notification: Notification) {
        // Без дозволу виклик notify() нічого не покаже (а lint вимагає перевірки)
        if (!canPostNotifications()) return
        try {
            manager.notify(id, notification)
        } catch (e: SecurityException) {
            // Дозвіл могли відкликати між перевіркою та показом
        }
    }

    companion object {
        const val CHANNEL_REMINDERS = "reminders"
        const val CHANNEL_ALERTS = "health_alerts"
        const val ID_WATER_REMINDER = 1001
        const val ID_PULSE_ALERT = 1002
        private const val REQUEST_OPEN_APP = 1
        private const val REQUEST_DRINK = 2
        const val HIGH_PULSE_THRESHOLD = 100
    }
}
