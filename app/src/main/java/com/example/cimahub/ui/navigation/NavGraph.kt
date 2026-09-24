package com.example.cimahub.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.cimahub.ui.screens.AddCaseScreen
import com.example.cimahub.ui.screens.AtlasScreen
import com.example.cimahub.ui.screens.CasesScreen
import com.example.cimahub.ui.screens.LoginScreen
import com.example.cimahub.ui.screens.SimulationScreen
import com.example.cimahub.ui.viewmodel.MedicalViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Cases : Screen("cases_catalog")
    object Atlas : Screen("anatomical_atlas")
    object AddCase : Screen("add_case?caseId={caseId}") {
        fun createRoute(caseId: Int? = null) = if (caseId != null) "add_case?caseId=$caseId" else "add_case"
    }
    object Simulation : Screen("simulation/{caseId}") {
        fun createRoute(caseId: Int) = "simulation/$caseId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: MedicalViewModel,
    onMenuClick: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(onLoginSuccess = { role ->
                viewModel.login(role)
                navController.navigate(Screen.Cases.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Cases.route) {
            CasesScreen(
                viewModel = viewModel, 
                onMenuClick = onMenuClick,
                onAddCase = { navController.navigate(Screen.AddCase.createRoute(null)) },
                onEditCase = { caseId -> navController.navigate(Screen.AddCase.createRoute(caseId)) }
            )
        }
        composable(
            route = Screen.AddCase.route,
            arguments = listOf(navArgument("caseId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val caseIdStr = backStackEntry.arguments?.getString("caseId")
            val caseId = caseIdStr?.toIntOrNull()
            AddCaseScreen(
                viewModel = viewModel,
                caseId = caseId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Atlas.route) {
            AtlasScreen(viewModel = viewModel)
        }
        composable(Screen.Simulation.route) { backStackEntry ->
            val caseId = backStackEntry.arguments?.getString("caseId")?.toIntOrNull()
            SimulationScreen(
                caseId = caseId, 
                viewModel = viewModel, 
                onBack = { navController.popBackStack() },
                onEditCase = { id -> navController.navigate(Screen.AddCase.createRoute(id)) }
            )
        }
    }
}
