package ua.edu.mobile.smartlife.ui.components

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect

/** Чи надані ВСІ (requireAll = true) або ХОЧА Б ОДИН із перелічених дозволів. */
fun Context.hasPermissions(permissions: List<String>, requireAll: Boolean = true): Boolean {
    val check = { p: String ->
        ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED
    }
    return if (requireAll) permissions.all(check) else permissions.any(check)
}

/** Стан групи дозволів для UI. */
class PermissionsState(
    val granted: Boolean,
    val deniedOnce: Boolean,
    val request: () -> Unit
)

/**
 * Запам'ятовує стан дозволів і повертає функцію для їх запиту.
 * Системне вікно запиту показує сам Android — ми лише запускаємо його через launcher.
 */
@Composable
fun rememberPermissionsState(
    permissions: List<String>,
    requireAll: Boolean = true
): PermissionsState {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(context.hasPermissions(permissions, requireAll)) }
    var deniedOnce by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        granted = if (requireAll) result.values.all { it } else result.values.any { it }
        if (!granted) deniedOnce = true
    }

    // Користувач міг змінити дозвіл у налаштуваннях — перевіряємо при кожному поверненні на екран
    LifecycleResumeEffect(permissions) {
        granted = context.hasPermissions(permissions, requireAll)
        onPauseOrDispose { }
    }

    return PermissionsState(granted, deniedOnce) {
        if (permissions.isEmpty()) granted = true else launcher.launch(permissions.toTypedArray())
    }
}

/** Картка з поясненням, навіщо потрібен дозвіл, і кнопкою запиту. */
@Composable
fun PermissionCard(
    icon: ImageVector,
    title: String,
    rationale: String,
    state: PermissionsState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(rationale, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = state.request) { Text("Надати дозвіл") }
            if (state.deniedOnce) {
                // Після повторної відмови Android більше не показує вікно — лишаються лише налаштування
                Text(
                    "Якщо вікно запиту не з'являється, увімкніть дозвіл у налаштуваннях застосунку.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                OutlinedButton(onClick = { context.openAppSettings() }) { Text("Відкрити налаштування") }
            }
        }
    }
}

/** Відкриває системну сторінку налаштувань нашого застосунку. */
fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .setData(Uri.fromParts("package", packageName, null))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}
