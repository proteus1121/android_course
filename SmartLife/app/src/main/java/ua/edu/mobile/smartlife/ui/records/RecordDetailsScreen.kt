package ua.edu.mobile.smartlife.ui.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ua.edu.mobile.smartlife.data.FakeData
import ua.edu.mobile.smartlife.ui.components.formatDate
import ua.edu.mobile.smartlife.ui.components.formattedValue
import ua.edu.mobile.smartlife.ui.components.icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDetailsScreen(recordId: Long, onBack: () -> Unit) {
    val record = FakeData.records.find { it.id == recordId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Запис №$recordId") },
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
}
