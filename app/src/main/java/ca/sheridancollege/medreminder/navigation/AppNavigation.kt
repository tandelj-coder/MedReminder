package ca.sheridancollege.medreminder.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ca.sheridancollege.medreminder.presentation.add.AddMedicationScreen
import ca.sheridancollege.medreminder.presentation.auth.AuthViewModel
import ca.sheridancollege.medreminder.presentation.auth.CompleteProfileScreen
import ca.sheridancollege.medreminder.presentation.auth.LoginScreen
import ca.sheridancollege.medreminder.presentation.history.HistoryScreen
import ca.sheridancollege.medreminder.presentation.settings.ProfileDetailScreen
import ca.sheridancollege.medreminder.presentation.settings.SettingsScreen
import ca.sheridancollege.medreminder.presentation.today.TodayScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Login : Screen("login", "Login", Icons.Default.Home)
    object CompleteProfile : Screen("complete_profile", "Setup", Icons.Default.Home)
    object Today : Screen("today", "Today", Icons.Default.Home)
    object Add : Screen("add", "Add", Icons.Default.Add)
    object History : Screen("history", "History", Icons.Default.DateRange)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object ProfileDetail : Screen("profile_detail", "Profile", Icons.Default.Settings)
    object Edit : Screen("edit/{medicationId}", "Edit", Icons.Default.Add)
}

val bottomNavItems = listOf(Screen.Today, Screen.Add, Screen.History, Screen.Settings)

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val authState by authViewModel.state.collectAsState()
    val isLoggedIn = authState.user != null
    val isProfileComplete = authState.user?.isProfileComplete ?: false

    val showBottomBar = isLoggedIn && isProfileComplete && bottomNavItems.any {
        currentDestination?.hierarchy?.any { d -> d.route == it.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any {
                                it.route == screen.route
                            } == true,
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (!isLoggedIn) Screen.Login.route 
                              else if (!isProfileComplete) Screen.CompleteProfile.route 
                              else Screen.Today.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        // Navigation is handled by startDestination logic in NavHost init mostly,
                        // but explicit navigation helps for instantaneous feedback.
                    }
                )
            }
            composable(Screen.CompleteProfile.route) {
                CompleteProfileScreen(
                    viewModel = authViewModel,
                    onComplete = {
                        navController.navigate(Screen.Today.route) {
                            popUpTo(Screen.CompleteProfile.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Today.route) {
                TodayScreen(
                    onEditMedication = { id ->
                        navController.navigate("edit/$id")
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.ProfileDetail.route)
                    }
                )
            }
            composable(Screen.Add.route) {
                AddMedicationScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable("edit/{medicationId}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("medicationId")?.toIntOrNull() ?: 0
                AddMedicationScreen(
                    medicationId = id,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.History.route) { HistoryScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(Screen.ProfileDetail.route) {
                ProfileDetailScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
