package ua.edu.mobile.smartlife.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Teal40,
    primaryContainer = TealContainerLight,
    secondary = Slate40,
    secondaryContainer = SlateContainerLight,
    tertiary = Coral40
)

private val DarkColors = darkColorScheme(
    primary = Teal80,
    primaryContainer = TealContainerDark,
    secondary = Slate80,
    secondaryContainer = SlateContainerDark,
    tertiary = Coral80
)

/** Тема застосунку: кольори Material 3 для світлого та темного режимів. */
@Composable
fun SmartLifeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
