package ua.edu.mobile.smartlife.ui.device

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/** Один пункт меню «Пристрій». */
private data class DeviceFeature(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

/** Екран-меню можливостей пристрою: кожен розділ курсу додає сюди свій пункт. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceHubScreen(
    onOpenBluetooth: () -> Unit,
    onOpenLocation: () -> Unit,
    onOpenSensors: () -> Unit
) {
    val features = listOf(
        DeviceFeature("Bluetooth LE", "Пошук і підключення пульсометра", Icons.Filled.Bluetooth, onOpenBluetooth),
        DeviceFeature("Геолокація", "Координати GPS у реальному часі", Icons.Filled.LocationOn, onOpenLocation),
        DeviceFeature("Сенсори", "Акселерометр, гіроскоп, світло, наближення", Icons.Filled.Speed, onOpenSensors)
    )

    Scaffold(topBar = { TopAppBar(title = { Text("Можливості пристрою") }) }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(features) { feature ->
                Card(onClick = feature.onClick, modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        leadingContent = {
                            Icon(feature.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        headlineContent = { Text(feature.title) },
                        supportingContent = { Text(feature.description) },
                        trailingContent = {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                        }
                    )
                }
            }
        }
    }
}
