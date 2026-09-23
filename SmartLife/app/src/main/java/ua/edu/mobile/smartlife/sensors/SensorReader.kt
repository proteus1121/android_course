package ua.edu.mobile.smartlife.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow

/** Короткий опис апаратного сенсора. */
data class SensorInfo(
    val name: String,
    val vendor: String,
    val maxRange: Float,
    val resolution: Float
)

/**
 * Обгортка над SensorManager: перетворює події сенсора на Flow.
 * Поки Flow збирається — сенсор увімкнено; після скасування — вимкнено (економія батареї).
 */
class SensorReader(context: Context) {

    private val sensorManager = context.getSystemService(SensorManager::class.java)

    fun info(type: Int): SensorInfo? = sensorManager.getDefaultSensor(type)?.let {
        SensorInfo(it.name, it.vendor, it.maximumRange, it.resolution)
    }

    /**
     * Потік значень сенсора. values залежать від типу:
     * акселерометр/гіроскоп — [x, y, z]; освітленість — [люкси]; наближення — [сантиметри].
     */
    fun readings(type: Int, delay: Int = SensorManager.SENSOR_DELAY_UI): Flow<FloatArray> {
        val sensor = sensorManager.getDefaultSensor(type) ?: return emptyFlow()
        return callbackFlow {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    // clone() — система перевикористовує масив values для наступних подій
                    trySend(event.values.clone())
                }

                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
            }
            sensorManager.registerListener(listener, sensor, delay)
            awaitClose { sensorManager.unregisterListener(listener) }
        }
    }
}
