package ua.edu.mobile.smartlife.ui.ble

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import ua.edu.mobile.smartlife.ble.BleConnectionState
import ua.edu.mobile.smartlife.ble.BleDevice
import ua.edu.mobile.smartlife.ble.BleManager
import ua.edu.mobile.smartlife.data.model.HealthRecord
import ua.edu.mobile.smartlife.data.model.RecordType
import ua.edu.mobile.smartlife.data.repository.RecordRepository

/** Стан сканування (те, чим керує сама ViewModel). */
data class ScanState(
    val isScanning: Boolean = false,
    val devices: List<BleDevice> = emptyList(),
    val bonded: List<BleDevice> = emptyList(),
    val connectedAddress: String? = null,
    val message: String? = null
)

/** Повний стан екрана = стан сканування + стан підключення з BleManager. */
data class BleUiState(
    val scan: ScanState = ScanState(),
    val connection: BleConnectionState = BleConnectionState.DISCONNECTED,
    val services: List<String> = emptyList(),
    val heartRate: Int? = null
)

class BleViewModel(
    private val bleManager: BleManager,
    private val recordRepository: RecordRepository
) : ViewModel() {

    private val scanState = MutableStateFlow(ScanState())
    private var scanJob: Job? = null

    val uiState: StateFlow<BleUiState> = combine(
        scanState, bleManager.connectionState, bleManager.services, bleManager.heartRate
    ) { scan, connection, services, heartRate ->
        BleUiState(scan, connection, services, heartRate)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BleUiState())

    val isSupported get() = bleManager.isSupported
    val isEnabled get() = bleManager.isEnabled

    fun startScan() {
        if (scanJob?.isActive == true) return
        scanState.update {
            it.copy(isScanning = true, devices = emptyList(), bonded = bleManager.bondedDevices(), message = null)
        }
        scanJob = viewModelScope.launch {
            try {
                // Скануємо не довше 10 секунд — сканування активно витрачає батарею
                withTimeoutOrNull(SCAN_DURATION_MS) {
                    bleManager.scan().collect { device -> addOrUpdate(device) }
                }
                scanState.update {
                    it.copy(message = if (it.devices.isEmpty()) "Пристроїв поблизу не знайдено" else null)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                scanState.update { it.copy(message = e.message) }
            } finally {
                scanState.update { it.copy(isScanning = false) }
            }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
    }

    private fun addOrUpdate(device: BleDevice) {
        scanState.update { state ->
            val others = state.devices.filterNot { it.address == device.address }
            // Сортуємо за силою сигналу: найближчі пристрої — вгорі
            state.copy(devices = (others + device).sortedByDescending { it.rssi })
        }
    }

    fun connect(device: BleDevice) {
        stopScan()
        scanState.update { it.copy(connectedAddress = device.address) }
        bleManager.connect(device.address)
    }

    fun disconnect() {
        bleManager.disconnect()
        scanState.update { it.copy(connectedAddress = null) }
    }

    /** Зберігає поточний пульс із BLE-датчика в журнал. */
    fun saveHeartRate() {
        val bpm = uiState.value.heartRate ?: return
        viewModelScope.launch {
            recordRepository.addRecord(
                HealthRecord(type = RecordType.PULSE, value = bpm.toDouble(), note = "BLE-пульсометр")
            )
            scanState.update { it.copy(message = "Пульс $bpm уд/хв збережено в журнал") }
        }
    }

    override fun onCleared() {
        bleManager.disconnect()
    }

    private companion object {
        const val SCAN_DURATION_MS = 10_000L
    }
}
