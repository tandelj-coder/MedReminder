package ca.sheridancollege.medreminder.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ca.sheridancollege.medreminder.presentation.add.AddMedicationScreen
import ca.sheridancollege.medreminder.presentation.garage.MedicationsScreen
import ca.sheridancollege.medreminder.presentation.history.HistoryScreen
import ca.sheridancollege.medreminder.presentation.today.TodayScreen

sealed class Screen(val route: String) {
    object Today : Screen("today")
    object Medications : Screen("medications")
    object History : Screen("history")
    object AddMedication : Screen("add_medication?medicationId={medicationId}") {
        fun createRoute(medicationId: Int?) = "add_medication?medicationId=$medicationId"
    }
}

@Composable
fun MedReminderNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Today.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Today.route) {
            TodayScreen()
        }
        composable(Screen.Medications.route) {
            MedicationsScreen(
                onEditMedication = { id -> navController.navigate(Screen.AddMedication.createRoute(id)) },
                onAddNewMedication = { navController.navigate(Screen.AddMedication.createRoute(null)) }
            )
        }
        composable(Screen.History.route) {
            HistoryScreen()
        }
        composable(
            route = Screen.AddMedication.route,
            arguments = listOf(navArgument("medicationId") {
                type = NavType.StringType
                nullable = true
            })
        ) { _ ->
            AddMedicationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
