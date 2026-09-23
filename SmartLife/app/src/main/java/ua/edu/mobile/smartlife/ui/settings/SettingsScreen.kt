package ua.edu.mobile.smartlife.ui.settings

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.edu.mobile.smartlife.data.settings.ThemeMode
import ua.edu.mobile.smartlife.data.settings.UserSettings
import ua.edu.mobile.smartlife.ui.AppViewModelProvider
import ua.edu.mobile.smartlife.ui.components.PermissionCard
import ua.edu.mobile.smartlife.ui.components.rememberPermissionsState

/** POST_NOTIFICATIONS існує лише з Android 13; на старіших версіях дозвіл не потрібен. */
private val notificationPermissions: List<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) listOf(Manifest.permission.POST_NOTIFICATIONS)
    else emptyList()

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    SettingsContent(
        settings = settings,
        onBack = onBack,
        onSaveName = viewModel::setUserName,
        onThemeChange = viewModel::setThemeMode,
        onWaterGoalChange = viewModel::setWaterGoal,
        onTestNotification = viewModel::sendTestNotification
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    settings: UserSettings?,
    onBack: () -> Unit,
    onSaveName: (String) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onWaterGoalChange: (Double) -> Unit,
    onTestNotification: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Налаштування") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        // Поки DataStore читає файл, показуємо індикатор завантаження
        if (settings == null) {
            CircularProgressIndicator(modifier = Modifier.padding(innerPadding).padding(16.dp))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- Ім'я користувача ---
            Text("Профіль", style = MaterialTheme.typography.titleMedium)
            var name by rememberSaveable { mutableStateOf(settings.userName) }
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Ваше ім'я") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { onSaveName(name) },
                enabled = name.isNotBlank() && name.trim() != settings.userName
            ) { Text("Зберегти ім'я") }

            HorizontalDivider()

            // --- Тема оформлення ---
            Text("Тема оформлення", style = MaterialTheme.typography.titleMedium)
            ThemeMode.entries.forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = settings.themeMode == mode,
                            onClick = { onThemeChange(mode) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = settings.themeMode == mode, onClick = null)
                    Text(mode.title, modifier = Modifier.padding(start = 12.dp))
                }
            }

            HorizontalDivider()

            // --- Денна ціль води ---
            var goal by rememberSaveable { mutableFloatStateOf(settings.waterGoalLiters.toFloat()) }
            Text(
                "Денна ціль води: ${"%.2f".format(goal)} л",
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = goal,
                onValueChange = { goal = it },
                onValueChangeFinished = { onWaterGoalChange(goal.toDouble()) },
                valueRange = 1f..4f,
                steps = 11 // крок 0.25 л
            )

            HorizontalDivider()

            // --- Сповіщення ---
            Text("Сповіщення", style = MaterialTheme.typography.titleMedium)
            val notificationPermission = rememberPermissionsState(notificationPermissions)
            if (!notificationPermission.granted) {
                PermissionCard(
                    icon = Icons.Filled.Notifications,
                    title = "Дозвольте сповіщення",
                    rationale = "Smart Life нагадуватиме пити воду та попереджатиме про високий пульс.",
                    state = notificationPermission
                )
            } else {
                OutlinedButton(onClick = onTestNotification) {
                    Icon(Icons.Filled.NotificationsActive, contentDescription = null)
                    Text("Надіслати тестове нагадування", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
