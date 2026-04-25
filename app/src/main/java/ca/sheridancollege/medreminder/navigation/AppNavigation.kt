package ca.sheridancollege.medreminder.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
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
import ca.sheridancollege.medreminder.presentation.garage.MedicationsScreen
import ca.sheridancollege.medreminder.presentation.settings.SettingsScreen
import ca.sheridancollege.medreminder.presentation.today.TodayScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Simplified navigation: Removed "Add" from tabs, kept core domains
    val items = listOf(
        Screen.Today,
        Screen.Medications,
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
                        label = { Text(screen.label) },
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.Add.route) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Medication")
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Today.route, modifier = Modifier.padding(innerPadding)) {
            composable(Screen.Today.route) { TodayScreen() }
            composable(Screen.Medications.route) { 
                MedicationsScreen(
                    onEditMedication = { id -> navController.navigate("edit/$id") }, 
                    onAddNewMedication = { navController.navigate(Screen.Add.route) }
                ) 
            }
            composable(
                route = Screen.Edit.route,
                arguments = listOf(androidx.navigation.navArgument("medicationId") { type = androidx.navigation.NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("medicationId") ?: 0
                // For now, navigating back for testing purposes or calling a hypothetical edit screen
                Text("Edit screen for $id")
            }
            composable(Screen.Add.route) { AddMedicationScreen(onNavigateBack = { navController.popBackStack() }) }
            composable(Screen.Emergency.route) { EmergencyScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Today : Screen("today", "Today", Icons.Default.Home)
    object Medications : Screen("medications", "Medications", Icons.AutoMirrored.Filled.List)
    object Add : Screen("add", "Add", Icons.Default.Add)
    object Edit : Screen("edit/{medicationId}", "Edit", Icons.Default.Edit)
    object Emergency : Screen("emergency", "Emergency", Icons.Default.Warning)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}
