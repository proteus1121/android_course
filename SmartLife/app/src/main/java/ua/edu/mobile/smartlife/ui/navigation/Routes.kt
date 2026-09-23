package ua.edu.mobile.smartlife.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

// Маршрути (routes) — "адреси" екранів. @Serializable потрібен для типобезпечної навігації.
@Serializable object LoginRoute
@Serializable object HomeRoute
@Serializable object RecordsRoute
@Serializable object ProfileRoute
@Serializable object SettingsRoute
@Serializable data class RecordDetailsRoute(val recordId: Long)

/** Пункт нижньої панелі навігації. */
data class TopLevelDestination(
    val route: Any,
    val label: String,
    val icon: ImageVector
)

val topLevelDestinations = listOf(
    TopLevelDestination(HomeRoute, "Головна", Icons.Filled.Home),
    TopLevelDestination(RecordsRoute, "Журнал", Icons.AutoMirrored.Filled.List),
    TopLevelDestination(ProfileRoute, "Профіль", Icons.Filled.Person)
)
