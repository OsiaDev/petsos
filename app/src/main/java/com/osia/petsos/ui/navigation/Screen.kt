package com.osia.petsos.ui.navigation

sealed class Screen(val route: String) {

    data object Welcome : Screen("welcome")

    data object Login : Screen("login")

    data object Register : Screen("register")

    data object Home : Screen("home")

    data object ReportLost : Screen("report_lost")

    data object ReportFound : Screen("report_found")

    data object PetDetail : Screen("pet_detail/{petId}") {
        fun createRoute(petId: String) = "pet_detail/$petId"
    }

    data object Profile : Screen("profile")

    data object Settings : Screen("settings")

}