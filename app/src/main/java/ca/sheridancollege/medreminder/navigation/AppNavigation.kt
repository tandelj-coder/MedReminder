package ca.sheridancollege.medreminder.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import ca.sheridancollege.medreminder.presentation.auth.*
import ca.sheridancollege.medreminder.presentation.emergency.EmergencyScreen
import ca.sheridancollege.medreminder.presentation.history.HistoryScreen
import ca.sheridancollege.medreminder.presentation.settings.ProfileDetailScreen
import ca.sheridancollege.medreminder.presentation.settings.SettingsScreen
import ca.sheridancollege.medreminder.presentation.today.TodayScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Welcome       : Screen("welcome",         "Welcome",   Icons.Default.Home)
    object Login         : Screen("login",           "Login",     Icons.Default.Home)
    object SignUp        : Screen("signup",           "Sign Up",   Icons.Default.Home)
    object CompleteProfile : Screen("complete_profile", "Setup",  Icons.Default.Home)
    object Today         : Screen("today",           "Today",     Icons.Default.Home)
    object Add           : Screen("add",             "Add",       Icons.Default.Add)
    object History       : Screen("history",         "History",   Icons.Default.DateRange)
    object Settings      : Screen("settings",        "Settings",  Icons.Default.Settings)
    object ProfileDetail : Screen("profile_detail",  "Profile",   Icons.Default.Settings)
    object Edit          : Screen("edit/{medicationId}", "Edit",  Icons.Default.Add)
    object Emergency     : Screen("emergency",       "Emergency", Icons.Default.Emergency)
}

val bottomNavItems = listOf(
    Screen.Today, Screen.Add, Screen.History, Screen.Emergency, Screen.Settings
)

private val noBottomBarRoutes = setOf(
    Screen.Welcome.route, Screen.Login.route, Screen.SignUp.route,
    Screen.CompleteProfile.route, Screen.ProfileDetail.route
)

@Composable
fun AppNavigation(authViewModel: AuthViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val authState by authViewModel.state.collectAsState()
    val isLoggedIn = authState.user != null
    val isProfileComplete = authState.user?.isProfileComplete ?: false

    val showBottomBar = isLoggedIn && isProfileComplete &&
            currentRoute != null && currentRoute !in noBottomBarRoutes

    val startDestination = when {
        !isLoggedIn        -> Screen.Welcome.route
        !isProfileComplete -> Screen.CompleteProfile.route
        else               -> Screen.Today.route
    }

    // React to auth state changes (sign out, delete account)
    LaunchedEffect(isLoggedIn, isProfileComplete) {
        when {
            !isLoggedIn -> navController.navigate(Screen.Welcome.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
            !isProfileComplete && currentRoute != Screen.CompleteProfile.route -> {
                navController.navigate(Screen.CompleteProfile.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, screen.label) },
                            label = { Text(screen.label) },
                            selected = navBackStackEntry?.destination?.hierarchy
                                ?.any { it.route == screen.route } == true,
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
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ── Auth flow ──────────────────────────────────────────
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onLoginClick = { navController.navigate(Screen.Login.route) },
                    onSignUpClick = { navController.navigate(Screen.SignUp.route) }
                )
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Today.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) }
                )
            }
            composable(Screen.SignUp.route) {
                SignUpScreen(
                    viewModel = authViewModel,
                    onNavigateToLogin = { navController.popBackStack() },
                    onSignUpSuccess = {
                        navController.navigate(Screen.CompleteProfile.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = false }
                        }
                    }
                )
            }
            composable(Screen.CompleteProfile.route) {
                CompleteProfileScreen(
                    viewModel = authViewModel,
                    onComplete = {
                        navController.navigate(Screen.Today.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            // ── Main app ──────────────────────────────────────────
            composable(Screen.Today.route) {
                TodayScreen(
                    onEditMedication = { navController.navigate("edit/$it") },
                    onNavigateToProfile = { navController.navigate(Screen.ProfileDetail.route) }
                )
            }
            composable(Screen.Add.route) {
                AddMedicationScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable("edit/{medicationId}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("medicationId")?.toIntOrNull() ?: 0
                AddMedicationScreen(medicationId = id,
                    onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.History.route)  { HistoryScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(Screen.Emergency.route) { EmergencyScreen() }
            composable(Screen.ProfileDetail.route) {
                ProfileDetailScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
