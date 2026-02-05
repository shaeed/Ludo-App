package com.shaeed.ludo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.shaeed.ludo.ui.screen.about.AboutScreen
import com.shaeed.ludo.ui.screen.game.GameScreen
import com.shaeed.ludo.ui.screen.home.HomeScreen
import com.shaeed.ludo.ui.screen.settings.SettingsScreen
import com.shaeed.ludo.ui.screen.setup.GameSetupScreen

@Composable
fun LudoNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Screen.Home.route, modifier = modifier) {
        composable(Screen.Home.route) {
            HomeScreen(
                onPlayClicked = { navController.navigate(Screen.GameSetup.route) },
                onSettingsClicked = { navController.navigate(Screen.Settings.route) },
                onAboutClicked = { navController.navigate(Screen.About.route) }
            )
        }
        composable(Screen.GameSetup.route) {
            GameSetupScreen(
                onStartGame = {
                    navController.navigate(Screen.Game.route) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Game.route) {
            GameScreen(
                onGameEnd = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onAboutClicked = { navController.navigate(Screen.About.route) }
            )
        }
        composable(Screen.About.route) {
            AboutScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
