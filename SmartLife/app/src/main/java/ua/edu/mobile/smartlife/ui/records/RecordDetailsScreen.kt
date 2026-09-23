package ua.edu.mobile.smartlife.ui.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.edu.mobile.smartlife.data.model.RecordType
import ua.edu.mobile.smartlife.ui.AppViewModelProvider
import ua.edu.mobile.smartlife.ui.components.formatDate
import ua.edu.mobile.smartlife.ui.components.formattedValue
import ua.edu.mobile.smartlife.ui.components.icon

@Composable
fun RecordDetailsScreen(
    onBack: () -> Unit,
    viewModel: RecordDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RecordDetailsContent(
        uiState = uiState,
        onUpdate = viewModel::updateRecord,
        onDelete = { viewModel.deleteRecord(onDeleted = onBack) },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDetailsContent(
    uiState: RecordDetailsUiState,
    onUpdate: (RecordType, Double, String) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    val record = uiState.record
    var confirmDelete by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

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
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Редагувати")
                        }
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
            if (uiState.isLoading) {
                CircularProgressIndicator()
                return@Column
            }
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

    if (showEditDialog && record != null) {
        // Той самий діалог, що й для створення, але з початковими значеннями запису
        AddRecordDialog(
            title = "Редагування запису",
            initialType = record.type,
            initialValue = record.value.toString(),
            initialNote = record.note,
            onDismiss = { showEditDialog = false },
            onSave = { type, value, note ->
                onUpdate(type, value, note)
                showEditDialog = false
            }
        )
    }

    if (confirmDelete && record != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Видалити запис?") },
            text = { Text("${record.type.title}: ${record.formattedValue()}. Цю дію не можна скасувати.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text("Видалити") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Скасувати") }
            }
        )
    }
}
