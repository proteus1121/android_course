package ua.edu.mobile.smartlife.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/** Картка погоди: показує один із трьох станів — завантаження, помилку або дані. */
@Composable
fun WeatherCard(
    state: WeatherUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (state) {
                WeatherUiState.Loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                    Text("Завантаження погоди...", modifier = Modifier.padding(start = 16.dp))
                }

                is WeatherUiState.Error -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                    OutlinedButton(onClick = onRetry) { Text("Повторити") }
                }

                is WeatherUiState.Success -> {
                    val weather = state.weather
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            weatherIcon(weather.weatherCode),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)) {
                            Text(
                                text = "${weather.temperature.roundToInt()} °C · ${weather.description}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Відчувається як ${weather.feelsLike.roundToInt()} °C")
                            Text(
                                "Вологість ${weather.humidity}% · Вітер ${weather.windSpeedKmh.roundToInt()} км/год",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "${state.placeLabel}: ${"%.2f".format(weather.latitude)}, ${"%.2f".format(weather.longitude)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = onRetry) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Оновити погоду")
                        }
                    }
                }
            }
        }
    }
}

/** Іконка залежно від коду погоди. */
private fun weatherIcon(code: Int): ImageVector = when (code) {
    0, 1 -> Icons.Filled.WbSunny
    2, 3, 45, 48 -> Icons.Filled.Cloud
    in 51..67, in 80..82 -> Icons.Filled.Umbrella
    in 71..77, 85, 86 -> Icons.Filled.AcUnit
    in 95..99 -> Icons.Filled.Thunderstorm
    else -> Icons.Filled.Cloud
}
