package com.osia.petsos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.osia.petsos.ui.welcome.WelcomeScreen

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
                    navController.navigate(Screen.Register.route) {
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
            // TODO: Implementar LoginScreen
            WelcomeScreen(
                onGetStartedClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onLoginClick = { }
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
            // TODO: Implementar HomeScreen
            WelcomeScreen(
                onGetStartedClick = { },
                onLoginClick = { }
            )
        }

        // Pantalla para reportar mascota perdida
        composable(route = Screen.ReportLost.route) {
            // TODO: Implementar ReportLostScreen
        }

        // Pantalla para reportar mascota encontrada
        composable(route = Screen.ReportFound.route) {
            // TODO: Implementar ReportFoundScreen
        }

        // Pantalla de detalle de mascota
        composable(route = Screen.PetDetail.route) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId")
            // TODO: Implementar PetDetailScreen con petId
        }

        // Pantalla de perfil
        composable(route = Screen.Profile.route) {
            // TODO: Implementar ProfileScreen
        }

        // Pantalla de configuración
        composable(route = Screen.Settings.route) {
            // TODO: Implementar SettingsScreen
        }
    }

}