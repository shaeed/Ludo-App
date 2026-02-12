package com.shaeed.ludo.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object GameSetup : Screen("game_setup")
    data object Game : Screen("game")
    data object SavedGames : Screen("saved_games")
    data object Settings : Screen("settings")
    data object About : Screen("about")
}
