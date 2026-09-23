package ua.edu.mobile.smartlife.ui.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.edu.mobile.smartlife.ble.BleConnectionState
import ua.edu.mobile.smartlife.ble.BleDevice
import ua.edu.mobile.smartlife.ui.AppViewModelProvider
import ua.edu.mobile.smartlife.ui.components.PermissionCard
import ua.edu.mobile.smartlife.ui.components.rememberPermissionsState

/** Дозволи для BLE залежать від версії Android. */
private val blePermissions: List<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        // До Android 12 сканування BLE вимагало дозволу на геолокацію
        listOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleScreen(
    onBack: () -> Unit,
    viewModel: BleViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissions = rememberPermissionsState(blePermissions)
    var bluetoothEnabled by remember { mutableStateOf(viewModel.isEnabled) }

    // Системний діалог "Дозволити увімкнути Bluetooth?"
    val enableLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { bluetoothEnabled = viewModel.isEnabled }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bluetooth LE") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                !viewModel.isSupported -> item {
                    InfoCard("Цей пристрій не підтримує Bluetooth Low Energy.")
                }

                !permissions.granted -> item {
                    PermissionCard(
                        icon = Icons.Filled.Bluetooth,
                        title = "Потрібен доступ до Bluetooth",
                        rationale = "Дозвіл потрібен, щоб знайти поблизу фітнес-браслет або пульсометр " +
                            "і отримувати з нього дані.",
                        state = permissions
                    )
                }

                !bluetoothEnabled -> item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        ListItem(
                            leadingContent = { Icon(Icons.Filled.BluetoothDisabled, contentDescription = null) },
                            headlineContent = { Text("Bluetooth вимкнено") },
                            supportingContent = { Text("Увімкніть Bluetooth, щоб почати пошук") }
                        )
                        Button(
                            onClick = { enableLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)) },
                            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
                        ) { Text("Увімкнути") }
                    }
                }

                else -> {
                    item {
                        ConnectionCard(
                            state = uiState,
                            onDisconnect = viewModel::disconnect,
                            onSave = viewModel::saveHeartRate
                        )
                    }
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (uiState.scan.isScanning) {
                                OutlinedButton(onClick = viewModel::stopScan) { Text("Зупинити") }
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                                Text("Пошук пристроїв...")
                            } else {
                                Button(onClick = viewModel::startScan) { Text("Почати пошук") }
                            }
                        }
                    }
                    uiState.scan.message?.let { message -> item { InfoCard(message) } }
                    if (uiState.scan.devices.isNotEmpty()) {
                        item { Text("Знайдені пристрої (BLE)", style = MaterialTheme.typography.titleMedium) }
                        items(uiState.scan.devices, key = { it.address }) { device ->
                            DeviceItem(device = device, onConnect = { viewModel.connect(device) })
                        }
                    }
                    if (uiState.scan.bonded.isNotEmpty()) {
                        item { Text("Сполучені пристрої", style = MaterialTheme.typography.titleMedium) }
                        items(uiState.scan.bonded, key = { "bonded-" + it.address }) { device ->
                            DeviceItem(device = device, onConnect = { viewModel.connect(device) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionCard(
    state: BleUiState,
    onDisconnect: () -> Unit,
    onSave: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            leadingContent = {
                Icon(
                    if (state.connection == BleConnectionState.CONNECTED) Icons.Filled.BluetoothConnected
                    else Icons.Filled.Bluetooth,
                    contentDescription = null
                )
            },
            headlineContent = { Text(state.connection.title) },
            supportingContent = { Text(state.scan.connectedAddress ?: "Оберіть пристрій зі списку") }
        )
        if (state.connection != BleConnectionState.DISCONNECTED) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.heartRate?.let { bpm ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Text(
                            "$bpm уд/хв",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    FilledTonalButton(onClick = onSave) { Text("Зберегти в журнал") }
                }
                if (state.services.isNotEmpty()) {
                    Text("Сервіси GATT:", style = MaterialTheme.typography.labelLarge)
                    state.services.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                }
                OutlinedButton(onClick = onDisconnect) { Text("Відключитися") }
            }
        }
    }
}

@Composable
private fun DeviceItem(device: BleDevice, onConnect: () -> Unit) {
    Card(onClick = onConnect, modifier = Modifier.fillMaxWidth()) {
        ListItem(
            leadingContent = { Icon(Icons.Filled.Bluetooth, contentDescription = null) },
            headlineContent = { Text(device.name ?: "Невідомий пристрій") },
            supportingContent = { Text(device.address) },
            trailingContent = { if (device.rssi != 0) Text("${device.rssi} dBm") }
        )
    }
}

@Composable
private fun InfoCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Text(text, modifier = Modifier.padding(16.dp))
    }
}
