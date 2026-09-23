package ua.edu.mobile.smartlife.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** Координати та супутня інформація, отримана від Location API. */
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val altitude: Double?,
    val speedMps: Float?,
    val time: Long
)

private fun Location.toLocationData() = LocationData(
    latitude = latitude,
    longitude = longitude,
    accuracyMeters = accuracy,
    altitude = if (hasAltitude()) altitude else null,
    speedMps = if (hasSpeed()) speed else null,
    time = time
)

/**
 * Обгортка над Fused Location Provider (Google Play services).
 * "Fused" = "злитий": сервіс сам комбінує GPS, Wi-Fi та мобільну мережу.
 */
@SuppressLint("MissingPermission") // перед викликом методів перевіряємо hasPermission()
class LocationClient(private val context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    /** Чи дозволив користувач доступ хоча б до приблизного місцезнаходження. */
    fun hasPermission(): Boolean =
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).any {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    /** Останнє відоме місцезнаходження — швидко, але може бути застарілим або null. */
    suspend fun getLastLocation(): LocationData? {
        if (!hasPermission()) return null
        return fusedClient.lastLocation.await()?.toLocationData()
    }

    /** Свіже місцезнаходження: сервіс увімкне GPS і дочекається нового значення. */
    suspend fun getCurrentLocation(): LocationData? {
        if (!hasPermission()) return null
        val tokenSource = CancellationTokenSource()
        return fusedClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
            .await(tokenSource)
            ?.toLocationData()
    }

    /** Безперервні оновлення координат, поки Flow збирається (collect). */
    fun locationUpdates(intervalMs: Long = 5_000): Flow<LocationData> = callbackFlow {
        if (!hasPermission()) {
            close(SecurityException("Немає дозволу на геолокацію"))
            return@callbackFlow
        }
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it.toLocationData()) }
            }
        }
        fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        // Обов'язково відписуємося, інакше GPS працюватиме й після закриття екрана
        awaitClose { fusedClient.removeLocationUpdates(callback) }
    }
}
