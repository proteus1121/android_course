package ua.edu.mobile.smartlife.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.edu.mobile.smartlife.data.model.RecordType
import ua.edu.mobile.smartlife.ui.AppViewModelProvider
import ua.edu.mobile.smartlife.ui.components.RecordCard
import ua.edu.mobile.smartlife.ui.components.icon

/** Stateful-обгортка: отримує ViewModel і передає її стан у HomeContent. */
@Composable
fun HomeScreen(
    onRecordClick: (Long) -> Unit,
    onOpenRecords: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        uiState = uiState,
        onAddWater = viewModel::addWaterGlass,
        onRecordClick = onRecordClick,
        onOpenRecords = onOpenRecords,
        onOpenSettings = onOpenSettings,
        onLogout = onLogout
    )
}

/** Stateless-частина: лише малює те, що отримала в параметрах. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    uiState: HomeUiState,
    onAddWater: () -> Unit,
    onRecordClick: (Long) -> Unit,
    onOpenRecords: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    val summary = uiState.summary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Life") },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Меню")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Налаштування") },
                            leadingIcon = { Icon(Icons.Filled.Settings, null) },
                            onClick = { menuExpanded = false; onOpenSettings() }
                        )
                        DropdownMenuItem(
                            text = { Text("Про застосунок") },
                            leadingIcon = { Icon(Icons.Filled.Info, null) },
                            onClick = { menuExpanded = false; showAbout = true }
                        )
                        DropdownMenuItem(
                            text = { Text("Вийти") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null) },
                            onClick = { menuExpanded = false; onLogout() }
                        )
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
            item {
                Text(
                    text = "Вітаю, ${uiState.userName}!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ваші показники за сьогодні",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                WaterCard(
                    liters = summary.waterLiters,
                    goal = uiState.waterGoalLiters,
                    onAddWater = onAddWater
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard(RecordType.PULSE.icon, "Пульс", summary.lastPulse.format(""), Modifier.weight(1f))
                    SummaryCard(RecordType.SLEEP.icon, "Сон", summary.lastSleep.format("год"), Modifier.weight(1f))
                    SummaryCard(RecordType.WEIGHT.icon, "Вага", summary.lastWeight.format("кг"), Modifier.weight(1f))
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Останні записи", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = onOpenRecords) { Text("Усі записи") }
                }
            }
            items(uiState.recentRecords, key = { it.id }) { record ->
                RecordCard(record = record, onClick = { onRecordClick(record.id) })
            }
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            icon = { Icon(Icons.Filled.Info, contentDescription = null) },
            title = { Text("Про застосунок") },
            text = { Text("Smart Life — навчальний застосунок курсу «Мобільна розробка».") },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("Зрозуміло") }
            }
        )
    }
}

/** Картка води з прогресом до денної цілі та кнопкою швидкого додавання. */
@Composable
private fun WaterCard(liters: Double, goal: Double, onAddWater: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(RecordType.WATER.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Вода: ${"%.2f".format(liters)} з ${"%.2f".format(goal)} л",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
            }
            LinearProgressIndicator(
                progress = { (liters / goal).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )
            FilledTonalButton(onClick = onAddWater) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("Склянка води (0.25 л)", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

/** Невелика картка з одним показником. */
@Composable
fun SummaryCard(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

/** null -> «—», інакше число з одиницею. */
private fun Double?.format(unit: String): String =
    if (this == null) "—" else "${if (this % 1.0 == 0.0) toLong() else this} $unit".trim()
