package ca.sheridancollege.medreminder.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ca.sheridancollege.medreminder.presentation.add.AddMedicationScreen
import ca.sheridancollege.medreminder.presentation.emergency.EmergencyScreen
import ca.sheridancollege.medreminder.presentation.garage.GarageScreen
import ca.sheridancollege.medreminder.presentation.settings.SettingsScreen
import ca.sheridancollege.medreminder.presentation.today.TodayScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Today,
        Screen.Garage,
        Screen.Add,
        Screen.Emergency,
        Screen.Settings
    )
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.route.replaceFirstChar { it.uppercase() }) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Today.route, modifier = Modifier.padding(innerPadding)) {
            composable(Screen.Today.route) { TodayScreen() }
            composable(Screen.Garage.route) { GarageScreen(onEditMedication = {}) }
            composable(Screen.Add.route) { AddMedicationScreen(onNavigateBack = {}) }
            composable(Screen.Emergency.route) { EmergencyScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}

sealed class Screen(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Today : Screen("today", Icons.Default.Home)
    object Garage : Screen("garage", Icons.Default.Dashboard)
    object Add : Screen("add", Icons.Default.Add)
    object Emergency : Screen("emergency", Icons.Default.Warning)
    object Settings : Screen("settings", Icons.Default.Settings)
}
