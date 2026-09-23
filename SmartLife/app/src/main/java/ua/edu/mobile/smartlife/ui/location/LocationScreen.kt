package ua.edu.mobile.smartlife.ui.location

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.edu.mobile.smartlife.location.LocationData
import ua.edu.mobile.smartlife.ui.AppViewModelProvider
import ua.edu.mobile.smartlife.ui.components.PermissionCard
import ua.edu.mobile.smartlife.ui.components.formatDate
import ua.edu.mobile.smartlife.ui.components.rememberPermissionsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    onBack: () -> Unit,
    viewModel: LocationViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Запитуємо обидва дозволи; користувач може надати лише "приблизне" місцезнаходження
    val permissions = rememberPermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
        requireAll = false
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Геолокація") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!permissions.granted) {
                PermissionCard(
                    icon = Icons.Filled.LocationOn,
                    title = "Потрібен доступ до геолокації",
                    rationale = "Координати використовуються для погоди у вашому місті та щоб " +
                        "позначати на карті, де зроблено запис.",
                    state = permissions
                )
                return@Column
            }

            LocationCard(location = uiState.location)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = viewModel::requestCurrentLocation, enabled = !uiState.isLoading) {
                    Icon(Icons.Filled.MyLocation, contentDescription = null)
                    Text("Де я?", modifier = Modifier.padding(start = 8.dp))
                }
                OutlinedButton(onClick = viewModel::toggleTracking) {
                    Text(if (uiState.isTracking) "Зупинити стеження" else "Стежити")
                }
                if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
            }
            if (uiState.isTracking) {
                Text("Отримано оновлень: ${uiState.updatesCount} (кожні ~5 с)")
            }
            uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun LocationCard(location: LocationData?) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Поточне місцезнаходження", style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 8.dp))
            }
            if (location == null) {
                Text("Натисніть «Де я?», щоб визначити координати")
                return@Column
            }
            Coordinate("Широта (latitude)", "%.6f°".format(location.latitude))
            Coordinate("Довгота (longitude)", "%.6f°".format(location.longitude))
            Coordinate("Точність", "±%.0f м".format(location.accuracyMeters))
            location.altitude?.let { Coordinate("Висота", "%.0f м".format(it)) }
            location.speedMps?.let { Coordinate("Швидкість", "%.1f км/год".format(it * 3.6)) }
            Coordinate("Час визначення", formatDate(location.time))
        }
    }
}

@Composable
private fun Coordinate(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}
