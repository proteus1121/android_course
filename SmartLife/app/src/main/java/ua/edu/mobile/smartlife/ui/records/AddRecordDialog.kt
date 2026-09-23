package ua.edu.mobile.smartlife.ui.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ua.edu.mobile.smartlife.data.model.RecordType
import ua.edu.mobile.smartlife.ui.components.icon

/**
 * Діалог створення/редагування запису.
 * Сам нічого не зберігає — лише повертає введені дані через onSave.
 */
@Composable
fun AddRecordDialog(
    onDismiss: () -> Unit,
    onSave: (type: RecordType, value: Double, note: String) -> Unit,
    title: String = "Новий запис",
    initialType: RecordType = RecordType.WATER,
    initialValue: String = "",
    initialNote: String = ""
) {
    var type by rememberSaveable { mutableStateOf(initialType) }
    var valueText by rememberSaveable { mutableStateOf(initialValue) }
    var note by rememberSaveable { mutableStateOf(initialNote) }

    // Кома чи крапка — обидва варіанти приймаємо
    val value = valueText.replace(',', '.').toDoubleOrNull()
    val isValueValid = value != null && value > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Тип показника")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RecordType.entries.forEach { item ->
                        FilterChip(
                            selected = item == type,
                            onClick = { type = item },
                            label = { Text(item.title) },
                            leadingIcon = { Icon(item.icon, contentDescription = null) }
                        )
                    }
                }
                OutlinedTextField(
                    value = valueText,
                    onValueChange = { valueText = it },
                    label = { Text("Значення, ${type.unit}") },
                    isError = valueText.isNotEmpty() && !isValueValid,
                    supportingText = {
                        if (valueText.isNotEmpty() && !isValueValid) Text("Введіть додатне число")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Нотатка (необов'язково)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(type, value!!, note.trim()) },
                enabled = isValueValid
            ) { Text("Зберегти") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Скасувати") }
        }
    )
}
