package ua.edu.mobile.smartlife

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ua.edu.mobile.smartlife.data.settings.ThemeMode
import ua.edu.mobile.smartlife.data.settings.UserSettings
import ua.edu.mobile.smartlife.ui.SmartLifeApp
import ua.edu.mobile.smartlife.ui.theme.SmartLifeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settingsRepository = (application as SmartLifeApplication).container.settingsRepository
        setContent {
            // Тема залежить від збереженого налаштування: при його зміні інтерфейс перемалюється
            val settings by settingsRepository.settings.collectAsStateWithLifecycle(initialValue = UserSettings())
            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            // Колір значків системних панелей (годинник, батарея) узгоджуємо з темою застосунку
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme }
                )
                onDispose { }
            }
            SmartLifeTheme(darkTheme = darkTheme) {
                SmartLifeApp()
            }
        }
    }
}
