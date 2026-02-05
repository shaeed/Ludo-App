package com.shaeed.ludo.model

data class PlayerConfig(
    val color: PlayerColor,
    val name: String,
    val isAI: Boolean,
    val difficulty: AiDifficulty = AiDifficulty.MEDIUM
)

data class GameConfig(
    val playerConfigs: List<PlayerConfig>,
    val enterOnSixOnly: Boolean = true,
    val safeZonesEnabled: Boolean = true,
    val maxConsecutiveSixes: Int = 3
)

object GameConfigHolder {
    var current: GameConfig = GameConfig(
        playerConfigs = listOf(
            PlayerConfig(PlayerColor.RED, "Player 1", isAI = false),
            PlayerConfig(PlayerColor.GREEN, "Player 2", isAI = true)
        )
    )
}
