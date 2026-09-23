package ua.edu.mobile.smartlife.ui.sensors

import android.hardware.Sensor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import ua.edu.mobile.smartlife.sensors.SensorInfo
import ua.edu.mobile.smartlife.sensors.SensorReader
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

/** Оброблені дані акселерометра. */
data class MotionState(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val magnitude: Float = 0f,  // модуль вектора прискорення, м/с²
    val pitch: Float = 0f,      // нахил вперед/назад, градуси
    val roll: Float = 0f,       // нахил вліво/вправо, градуси
    val shakes: Int = 0         // скільки разів пристрій струснули
)

class SensorsViewModel(
    private val sensorReader: SensorReader
) : ViewModel() {

    val accelerometerInfo: SensorInfo? = sensorReader.info(Sensor.TYPE_ACCELEROMETER)
    val gyroscopeInfo: SensorInfo? = sensorReader.info(Sensor.TYPE_GYROSCOPE)
    val lightInfo: SensorInfo? = sensorReader.info(Sensor.TYPE_LIGHT)
    val proximityInfo: SensorInfo? = sensorReader.info(Sensor.TYPE_PROXIMITY)

    /** Акселерометр + обчислення нахилу та підрахунок струшувань. */
    val motion: StateFlow<MotionState> = sensorReader.readings(Sensor.TYPE_ACCELEROMETER)
        .runningFold(MotionState()) { previous, v -> computeMotion(previous, v[0], v[1], v[2]) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1_000), MotionState())

    /** Кутова швидкість обертання навколо осей x, y, z (рад/с). */
    val gyroscope: StateFlow<FloatArray?> = sensorReader.readings(Sensor.TYPE_GYROSCOPE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1_000), null)

    /** Освітленість у люксах (lx). */
    val light: StateFlow<Float?> = sensorReader.readings(Sensor.TYPE_LIGHT)
        .map<FloatArray, Float?> { it[0] }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1_000), null)

    /** Датчик наближення: true — об'єкт близько (наприклад, телефон біля вуха). */
    val isNear: StateFlow<Boolean?> = sensorReader.readings(Sensor.TYPE_PROXIMITY)
        .map<FloatArray, Boolean?> { it[0] < (proximityInfo?.maxRange ?: 5f) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1_000), null)

    companion object {
        private const val GRAVITY = 9.81f
        private const val SHAKE_THRESHOLD = 6f // на скільки м/с² прискорення має перевищити g

        /** Чиста функція (без Android-залежностей) — зручно тестувати в розділі 17. */
        fun computeMotion(previous: MotionState, x: Float, y: Float, z: Float): MotionState {
            val magnitude = sqrt(x * x + y * y + z * z)
            val pitch = Math.toDegrees(atan2(y.toDouble(), z.toDouble())).toFloat()
            val roll = Math.toDegrees(atan2(-x.toDouble(), sqrt((y * y + z * z).toDouble()))).toFloat()
            // Струшування = різкий стрибок прискорення, якого не було в попередньому значенні
            val isShake = abs(magnitude - GRAVITY) > SHAKE_THRESHOLD &&
                abs(previous.magnitude - GRAVITY) <= SHAKE_THRESHOLD
            return MotionState(
                x = x, y = y, z = z,
                magnitude = magnitude,
                pitch = pitch,
                roll = roll,
                shakes = previous.shakes + if (isShake) 1 else 0
            )
        }
    }
}
