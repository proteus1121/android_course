package ua.edu.mobile.smartlife.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

/** Знайдений під час сканування BLE-пристрій. */
data class BleDevice(
    val name: String?,
    val address: String,   // MAC-адреса, наприклад "AA:BB:CC:DD:EE:FF"
    val rssi: Int          // рівень сигналу в dBm: -40 — дуже близько, -90 — далеко
)

enum class BleConnectionState(val title: String) {
    DISCONNECTED("Не підключено"),
    CONNECTING("Підключення..."),
    CONNECTED("Підключено")
}

/**
 * Обгортка над Android Bluetooth API.
 * Усі методи викликаються ЛИШЕ після того, як UI отримав дозволи
 * BLUETOOTH_SCAN / BLUETOOTH_CONNECT, тому попередження lint "MissingPermission" вимкнено.
 */
@SuppressLint("MissingPermission")
class BleManager(private val context: Context) {

    private val adapter: BluetoothAdapter?
        get() = context.getSystemService(BluetoothManager::class.java)?.adapter

    /** Чи є в пристрої модуль Bluetooth Low Energy. */
    val isSupported: Boolean
        get() = adapter != null &&
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

    /** Чи увімкнено Bluetooth у налаштуваннях телефону. */
    val isEnabled: Boolean
        get() = adapter?.isEnabled == true

    /** Раніше сполучені (paired) пристрої — і Classic, і BLE. */
    fun bondedDevices(): List<BleDevice> =
        adapter?.bondedDevices.orEmpty().map { BleDevice(it.name, it.address, rssi = 0) }

    /**
     * Сканування BLE. callbackFlow перетворює "callback-стиль" Android API на Flow:
     * сканування починається при collect і зупиняється, коли збирання скасовано.
     */
    fun scan(): Flow<BleDevice> = callbackFlow {
        val scanner = adapter?.bluetoothLeScanner
        if (scanner == null) {
            close(IllegalStateException("Bluetooth вимкнено"))
            return@callbackFlow
        }
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                trySend(
                    BleDevice(
                        name = result.scanRecord?.deviceName ?: result.device.name,
                        address = result.device.address,
                        rssi = result.rssi
                    )
                )
            }

            override fun onScanFailed(errorCode: Int) {
                close(IllegalStateException("Помилка сканування, код $errorCode"))
            }
        }
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        scanner.startScan(null, settings, callback)
        awaitClose { scanner.stopScan(callback) }
    }

    // ----- Підключення (GATT) -----

    private val _connectionState = MutableStateFlow(BleConnectionState.DISCONNECTED)
    val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    private val _services = MutableStateFlow<List<String>>(emptyList())
    val services: StateFlow<List<String>> = _services.asStateFlow()

    private val _heartRate = MutableStateFlow<Int?>(null)
    val heartRate: StateFlow<Int?> = _heartRate.asStateFlow()

    private var gatt: BluetoothGatt? = null

    fun connect(address: String) {
        val device = adapter?.getRemoteDevice(address) ?: return
        disconnect()
        _connectionState.value = BleConnectionState.CONNECTING
        gatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        _connectionState.value = BleConnectionState.DISCONNECTED
        _services.value = emptyList()
        _heartRate.value = null
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _connectionState.value = BleConnectionState.CONNECTED
                    gatt.discoverServices() // крок 2: дізнаємося, які сервіси має пристрій
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _connectionState.value = BleConnectionState.DISCONNECTED
                    gatt.close()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            _services.value = gatt.services.map { serviceName(it.uuid) }

            // Крок 3: якщо це пульсометр — підписуємося на сповіщення Heart Rate Measurement
            val characteristic = gatt.getService(HEART_RATE_SERVICE)
                ?.getCharacteristic(HEART_RATE_MEASUREMENT) ?: return
            gatt.setCharacteristicNotification(characteristic, true)
            val descriptor = characteristic.getDescriptor(CLIENT_CONFIG_DESCRIPTOR) ?: return
            val enable = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeDescriptor(descriptor, enable)
            } else {
                @Suppress("DEPRECATION")
                descriptor.value = enable
                @Suppress("DEPRECATION")
                gatt.writeDescriptor(descriptor)
            }
        }

        // Android 13+ : значення приходить параметром
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            if (characteristic.uuid == HEART_RATE_MEASUREMENT) {
                _heartRate.value = parseHeartRate(value)
            }
        }

        // Android 12 і старіші
        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
                characteristic.uuid == HEART_RATE_MEASUREMENT
            ) {
                @Suppress("DEPRECATION")
                _heartRate.value = parseHeartRate(characteristic.value)
            }
        }
    }

    companion object {
        // Стандартні UUID від Bluetooth SIG (https://www.bluetooth.com/specifications/assigned-numbers/)
        val HEART_RATE_SERVICE: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        val HEART_RATE_MEASUREMENT: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
        val CLIENT_CONFIG_DESCRIPTOR: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        /**
         * Розбір пакета Heart Rate Measurement:
         * байт 0 — прапорці; якщо біт 0 = 0, пульс займає 1 байт (UINT8), інакше 2 байти (UINT16).
         */
        fun parseHeartRate(bytes: ByteArray): Int? {
            if (bytes.size < 2) return null
            val flags = bytes[0].toInt()
            return if (flags and 0x01 == 0) {
                bytes[1].toInt() and 0xFF
            } else {
                if (bytes.size < 3) return null
                (bytes[1].toInt() and 0xFF) or ((bytes[2].toInt() and 0xFF) shl 8)
            }
        }

        /** Людська назва для відомих сервісів. */
        fun serviceName(uuid: UUID): String = when (uuid.toString().substring(4, 8)) {
            "1800" -> "Generic Access"
            "1801" -> "Generic Attribute"
            "180a" -> "Device Information"
            "180d" -> "Heart Rate (пульс)"
            "180f" -> "Battery (батарея)"
            else -> uuid.toString()
        }
    }
}
