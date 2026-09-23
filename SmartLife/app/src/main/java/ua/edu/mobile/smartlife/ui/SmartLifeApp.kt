package ua.edu.mobile.smartlife.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ua.edu.mobile.smartlife.ui.auth.LoginScreen
import ua.edu.mobile.smartlife.ui.home.HomeScreen
import ua.edu.mobile.smartlife.ui.navigation.HomeRoute
import ua.edu.mobile.smartlife.ui.navigation.LoginRoute
import ua.edu.mobile.smartlife.ui.navigation.ProfileRoute
import ua.edu.mobile.smartlife.ui.navigation.RecordDetailsRoute
import ua.edu.mobile.smartlife.ui.navigation.RecordsRoute
import ua.edu.mobile.smartlife.ui.navigation.topLevelDestinations
import ua.edu.mobile.smartlife.ui.profile.ProfileScreen
import ua.edu.mobile.smartlife.ui.records.RecordDetailsScreen
import ua.edu.mobile.smartlife.ui.records.RecordsScreen

/** Кореневий composable: нижня панель + граф навігації між екранами. */
@Composable
fun SmartLifeApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // Нижню панель показуємо лише на "головних" екранах
    val showBottomBar = topLevelDestinations.any { currentDestination.isOn(it.route) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topLevelDestinations.forEach { item ->
                        NavigationBarItem(
                            selected = currentDestination.isOn(item.route),
                            onClick = { navController.navigateToTopLevel(item.route) },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = LoginRoute,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable<LoginRoute> {
                LoginScreen(
                    onLoggedIn = {
                        // Після входу прибираємо екран логіну зі стеку, щоб "Назад" не повертав на нього
                        navController.navigate(HomeRoute) {
                            popUpTo(LoginRoute) { inclusive = true }
                        }
                    }
                )
            }
            composable<HomeRoute> {
                HomeScreen(
                    onRecordClick = { id -> navController.navigate(RecordDetailsRoute(id)) },
                    onOpenRecords = { navController.navigateToTopLevel(RecordsRoute) },
                    onLogout = { navController.logout() }
                )
            }
            composable<RecordsRoute> {
                RecordsScreen(
                    onRecordClick = { id -> navController.navigate(RecordDetailsRoute(id)) }
                )
            }
            composable<RecordDetailsRoute> {
                // recordId з маршруту RecordDetailsViewModel отримає через SavedStateHandle
                RecordDetailsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable<ProfileRoute> {
                ProfileScreen(onLogout = { navController.logout() })
            }
        }
    }
}

/** Чи належить поточний екран до вказаного маршруту. */
private fun NavDestination?.isOn(route: Any): Boolean =
    this?.hierarchy?.any { it.hasRoute(route::class) } == true

/** Перехід між вкладками нижньої панелі без накопичення копій екранів у стеку. */
private fun NavHostController.navigateToTopLevel(route: Any) {
    navigate(route) {
        // Головна завжди лежить на дні стеку після входу
        popUpTo(HomeRoute) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Вихід: очищаємо весь стек і відкриваємо екран входу. */
private fun NavHostController.logout() {
    navigate(LoginRoute) {
        popUpTo(graph.id) { inclusive = true }
    }
}
