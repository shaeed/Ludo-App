package com.shaeed.ludo.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object GameSetup : Screen("game_setup")
    data object Game : Screen("game")
    data object Settings : Screen("settings")
}
