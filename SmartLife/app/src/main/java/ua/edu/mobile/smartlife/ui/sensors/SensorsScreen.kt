package ua.edu.mobile.smartlife.ui.sensors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.edu.mobile.smartlife.sensors.SensorInfo
import ua.edu.mobile.smartlife.ui.AppViewModelProvider
import kotlin.math.log10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsScreen(
    onBack: () -> Unit,
    viewModel: SensorsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // collectAsStateWithLifecycle зупиняє збір (і сенсори), коли застосунок згорнуто
    val motion by viewModel.motion.collectAsStateWithLifecycle()
    val gyro by viewModel.gyroscope.collectAsStateWithLifecycle()
    val light by viewModel.light.collectAsStateWithLifecycle()
    val isNear by viewModel.isNear.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Сенсори") },
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
            SensorCard("Акселерометр", Icons.Filled.Vibration, viewModel.accelerometerInfo) {
                Axis("X", motion.x, "м/с²")
                Axis("Y", motion.y, "м/с²")
                Axis("Z", motion.z, "м/с²")
                Text("Модуль: %.2f м/с²".format(motion.magnitude))
                Text("Нахил: вперед/назад %.0f°, вліво/вправо %.0f°".format(motion.pitch, motion.roll))
                Text(
                    "Струшувань: ${motion.shakes}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            SensorCard("Гіроскоп", Icons.Filled.ScreenRotation, viewModel.gyroscopeInfo) {
                val values = gyro
                if (values == null) {
                    Text("Очікування даних...")
                } else {
                    Axis("X", values[0], "рад/с")
                    Axis("Y", values[1], "рад/с")
                    Axis("Z", values[2], "рад/с")
                }
            }

            SensorCard("Датчик освітленості", Icons.Filled.Lightbulb, viewModel.lightInfo) {
                val lux = light
                if (lux == null) {
                    Text("Очікування даних...")
                } else {
                    Text("%.0f lx — %s".format(lux, describeLight(lux)), style = MaterialTheme.typography.titleMedium)
                    // Логарифмічна шкала: від 1 до 100 000 люкс
                    LinearProgressIndicator(
                        progress = { (log10(lux.coerceAtLeast(1f)) / 5f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            SensorCard("Датчик наближення", Icons.Filled.Sensors, viewModel.proximityInfo) {
                Text(
                    text = when (isNear) {
                        null -> "Очікування даних..."
                        true -> "БЛИЗЬКО — об'єкт біля екрана"
                        false -> "Далеко"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/** Картка одного сенсора; якщо сенсора немає — повідомляємо про це. */
@Composable
private fun SensorCard(
    title: String,
    icon: ImageVector,
    info: SensorInfo?,
    content: @Composable () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 8.dp))
            }
            if (info == null) {
                Text("Сенсор відсутній на цьому пристрої", color = MaterialTheme.colorScheme.error)
            } else {
                Text(info.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                content()
            }
        }
    }
}

@Composable
private fun Axis(name: String, value: Float, unit: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(name, modifier = Modifier.weight(1f))
        Text("%+.2f %s".format(value, unit), fontFamily = FontFamily.Monospace)
    }
}

private fun describeLight(lux: Float): String = when {
    lux < 10 -> "темно"
    lux < 200 -> "приміщення, слабке світло"
    lux < 1_000 -> "добре освітлене приміщення"
    lux < 10_000 -> "похмурий день надворі"
    else -> "яскраве сонце"
}
