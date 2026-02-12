package com.shaeed.ludo.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerConfig(
    val color: PlayerColor,
    val name: String,
    val isAI: Boolean,
    val difficulty: AiDifficulty = AiDifficulty.MEDIUM
)

@Serializable
data class GameConfig(
    val playerConfigs: List<PlayerConfig>,
    val enterOnSixOnly: Boolean = true,
    val safeZonesEnabled: Boolean = true,
    val maxConsecutiveSixes: Int = 3,
    val passDiceToNextPlayer: Boolean = false,
    val friendMode: Boolean = false
)

object GameConfigHolder {
    var current: GameConfig = GameConfig(
        playerConfigs = listOf(
            PlayerConfig(PlayerColor.RED, "Player 1", isAI = false),
            PlayerConfig(PlayerColor.GREEN, "Player 2", isAI = true)
        )
    )
}
