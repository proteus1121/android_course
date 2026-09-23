package ua.edu.mobile.smartlife.ui.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.edu.mobile.smartlife.data.model.HealthRecord
import ua.edu.mobile.smartlife.data.model.RecordType
import ua.edu.mobile.smartlife.ui.AppViewModelProvider
import ua.edu.mobile.smartlife.ui.components.RecordCard

@Composable
fun RecordsScreen(
    onRecordClick: (Long) -> Unit,
    viewModel: RecordsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val records by viewModel.records.collectAsStateWithLifecycle()
    RecordsContent(
        records = records,
        onAddRecord = viewModel::addRecord,
        onRecordClick = onRecordClick
    )
}

/**
 * Екран списку без власних даних (stateless): список приходить параметром,
 * а про нові записи екран повідомляє через onAddRecord (state hoisting).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsContent(
    records: List<HealthRecord>,
    onAddRecord: (RecordType, Double, String) -> Unit,
    onRecordClick: (Long) -> Unit
) {
    // Стан діалогу — суто UI-стан, тому лишається локальним
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Журнал показників (${records.size})") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Додати запис")
            }
        }
    ) { innerPadding ->
        if (records.isEmpty()) {
            EmptyRecords(modifier = Modifier.padding(innerPadding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(records, key = { it.id }) { record ->
                RecordCard(record = record, onClick = { onRecordClick(record.id) })
            }
        }
    }

    if (showAddDialog) {
        AddRecordDialog(
            onDismiss = { showAddDialog = false },
            onSave = { type, value, note ->
                onAddRecord(type, value, note)
                showAddDialog = false
            }
        )
    }
}

/** Заглушка, коли в базі ще немає жодного запису. */
@Composable
private fun EmptyRecords(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.AutoMirrored.Filled.EventNote,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            "Записів поки немає",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            "Натисніть «+», щоб додати перший показник",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
