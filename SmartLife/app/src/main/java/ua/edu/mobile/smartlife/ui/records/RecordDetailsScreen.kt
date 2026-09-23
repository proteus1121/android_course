package ua.edu.mobile.smartlife.ui.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ua.edu.mobile.smartlife.data.model.HealthRecord
import ua.edu.mobile.smartlife.ui.components.formatDate
import ua.edu.mobile.smartlife.ui.components.formattedValue
import ua.edu.mobile.smartlife.ui.components.icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDetailsScreen(
    record: HealthRecord?,
    onDelete: (Long) -> Unit,
    onBack: () -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(record?.let { "Запис №${it.id}" } ?: "Запис") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (record != null) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Видалити")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (record == null) {
                Text("Запис не знайдено", style = MaterialTheme.typography.titleMedium)
                return@Column
            }
            ElevatedCard {
                ListItem(
                    leadingContent = { Icon(record.type.icon, contentDescription = null) },
                    headlineContent = { Text(record.type.title) },
                    supportingContent = { Text(formatDate(record.timestamp)) },
                    trailingContent = {
                        Text(record.formattedValue(), style = MaterialTheme.typography.titleLarge)
                    }
                )
            }
            Text("Нотатка", style = MaterialTheme.typography.titleMedium)
            Text(record.note.ifBlank { "—" })
        }
    }

    if (confirmDelete && record != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Видалити запис?") },
            text = { Text("${record.type.title}: ${record.formattedValue()}. Цю дію не можна скасувати.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete(record.id)
                }) { Text("Видалити") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Скасувати") }
            }
        )
    }
}
