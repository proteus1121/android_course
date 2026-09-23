package ua.edu.mobile.smartlife.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import ua.edu.mobile.smartlife.data.model.HealthRecord
import ua.edu.mobile.smartlife.data.model.RecordType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Іконка для кожного типу запису. */
val RecordType.icon: ImageVector
    get() = when (this) {
        RecordType.WATER -> Icons.Filled.WaterDrop
        RecordType.PULSE -> Icons.Filled.Favorite
        RecordType.WEIGHT -> Icons.Filled.MonitorWeight
        RecordType.SLEEP -> Icons.Filled.Bedtime
        RecordType.MOOD -> Icons.Filled.SentimentSatisfied
    }

/** Значення з одиницею виміру, наприклад «0.5 л» або «72 уд/хв». */
fun HealthRecord.formattedValue(): String {
    val number = if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
    return "$number ${type.unit}"
}

private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.forLanguageTag("uk"))

fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))
