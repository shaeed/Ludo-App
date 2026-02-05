package com.shaeed.ludo.ui.screen.setup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.shaeed.ludo.model.*

class GameSetupViewModel : ViewModel() {

    var playerCount by mutableIntStateOf(2)
        private set

    val playerConfigs = mutableStateListOf(
        PlayerConfig(PlayerColor.RED, "Player 1", isAI = false),
        PlayerConfig(PlayerColor.GREEN, "Player 2", isAI = true),
        PlayerConfig(PlayerColor.YELLOW, "Player 3", isAI = true),
        PlayerConfig(PlayerColor.BLUE, "Player 4", isAI = true)
    )

    var enterOnSixOnly by mutableStateOf(true)
        private set
    var safeZonesEnabled by mutableStateOf(true)
        private set
    var maxConsecutiveSixes by mutableIntStateOf(3)
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

    fun toggleEnterOnSixOnly() { enterOnSixOnly = !enterOnSixOnly }
    fun toggleSafeZones() { safeZonesEnabled = !safeZonesEnabled }
    fun updateMaxConsecutiveSixes(value: Int) { maxConsecutiveSixes = value.coerceIn(1, 5) }

    fun buildConfig(): GameConfig {
        val activePlayers = playerConfigs.take(playerCount)
        return GameConfig(
            playerConfigs = activePlayers,
            enterOnSixOnly = enterOnSixOnly,
            safeZonesEnabled = safeZonesEnabled,
            maxConsecutiveSixes = maxConsecutiveSixes
        )
    }
}
