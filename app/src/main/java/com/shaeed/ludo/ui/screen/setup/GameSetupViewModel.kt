package com.shaeed.ludo.ui.screen.setup

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.shaeed.ludo.data.UserPreferences
import com.shaeed.ludo.model.*

class GameSetupViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = UserPreferences(application)

    var playerCount by mutableIntStateOf(2)
        private set

    val playerConfigs = mutableStateListOf(
        PlayerConfig(PlayerColor.RED, "Player 1", isAI = false),
        PlayerConfig(PlayerColor.GREEN, "Player 2", isAI = true),
        PlayerConfig(PlayerColor.YELLOW, "Player 3", isAI = true),
        PlayerConfig(PlayerColor.BLUE, "Player 4", isAI = true)
    )

    var maxConsecutiveSixes by mutableIntStateOf(prefs.maxConsecutiveSixes)
        private set
    var passDiceToNextPlayer by mutableStateOf(prefs.passDiceToNextPlayer)
        private set
    var friendMode by mutableStateOf(GameConfigHolder.current.friendMode)
        private set

    fun updatePlayerCount(count: Int) {
        playerCount = count.coerceIn(2, 4)
    }

    fun toggleAI(index: Int) {
        if (index in playerConfigs.indices) {
            val config = playerConfigs[index]
            playerConfigs[index] = config.copy(isAI = !config.isAI)
        }
    }

    fun setAiDifficulty(index: Int, difficulty: AiDifficulty) {
        if (index in playerConfigs.indices) {
            val config = playerConfigs[index]
            playerConfigs[index] = config.copy(difficulty = difficulty)
        }
    }

    fun setPlayerName(index: Int, name: String) {
        if (index in playerConfigs.indices) {
            val config = playerConfigs[index]
            playerConfigs[index] = config.copy(name = name)
        }
    }

    fun updateMaxConsecutiveSixes(value: Int) { maxConsecutiveSixes = value.coerceIn(1, 5) }
    fun togglePassDiceToNextPlayer() { passDiceToNextPlayer = !passDiceToNextPlayer }
    fun toggleFriendMode() { friendMode = !friendMode }

    fun buildConfig(): GameConfig {
        val activePlayers = playerConfigs.take(playerCount)
        return GameConfig(
            playerConfigs = activePlayers,
            enterOnSixOnly = prefs.enterOnSixOnly,
            safeZonesEnabled = prefs.safeZonesEnabled,
            maxConsecutiveSixes = maxConsecutiveSixes,
            passDiceToNextPlayer = passDiceToNextPlayer,
            friendMode = friendMode
        )
    }
}
