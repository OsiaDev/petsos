package com.osia.petsos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.osia.petsos.ui.welcome.WelcomeScreen
import com.osia.petsos.ui.home.HomeScreen
import com.osia.petsos.ui.report.ReportFoundScreen
import com.osia.petsos.ui.report.ReportLostScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.osia.petsos.ui.profile.ProfileScreen
import com.osia.petsos.ui.profile.ProfileViewModel
import com.osia.petsos.ui.profile.ProfileUiState

/**
 * Composable principal de navegación de la aplicación
 * Gestiona todas las rutas y transiciones entre pantallas
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        // Pantalla de bienvenida
        composable(route = Screen.Welcome.route) {
            WelcomeScreen(
                onGetStartedClick = {
                    navController.navigate(Screen.Home.route) {
                        // Evitar volver a la pantalla de bienvenida
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        // Pantalla de login
        composable(route = Screen.Login.route) {
            com.osia.petsos.ui.login.LoginScreen(
                onLoginSuccess = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla de registro
        composable(route = Screen.Register.route) {
            // TODO: Implementar RegisterScreen
            WelcomeScreen(
                onGetStartedClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onLoginClick = { }
            )
        }

        // Pantalla principal (Home)
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToAlerts = {
                    // TODO: Implementar pantalla de alertas
                },
                onNavigateToMessages = {
                    // TODO: Implementar pantalla de mensajes
                },
                onNavigateToReportLost = {
                    navController.navigate(Screen.ReportLost.route)
                },
                onNavigateToReportFound = {
                    navController.navigate(Screen.ReportFound.route)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onContactOwner = { petId ->
                    // TODO: Implementar lógica para contactar al dueño
                },
                onViewDetails = { petId ->
                    navController.navigate(Screen.PetDetail.createRoute(petId))
                }
            )
        }

        // Pantalla para reportar mascota perdida
        composable(route = Screen.ReportLost.route) {
            ReportLostScreen()
        }

        // Pantalla para reportar mascota encontrada
        composable(route = Screen.ReportFound.route) {
            ReportFoundScreen()
        }

        // Pantalla de detalle de mascota
        composable(route = Screen.PetDetail.route) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId")
            // TODO: Implementar PetDetailScreen con petId
        }

        // Pantalla de perfil
        composable(route = Screen.Profile.route) {
            val viewModel: ProfileViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(uiState) {
                if (uiState is ProfileUiState.Unauthenticated) {
                    navController.navigate(Screen.Login.route)
                }
            }

            if (uiState !is ProfileUiState.Unauthenticated) {
                ProfileScreen(navController = navController, viewModel = viewModel)
            }
        }

        // Pantalla de configuración
        composable(route = Screen.Settings.route) {
            // TODO: Implementar SettingsScreen
        }
    }

}