package com.shaeed.ludo.model

data class GameState(
    val players: List<Player>,
    val currentPlayerIndex: Int,
    val dice: DiceResult?,
    val phase: GamePhase,
    val winner: PlayerColor?,
    val consecutiveSixes: Int = 0,
    val giftedDice: DiceResult? = null,
    val giftedDiceOriginalPlayerIndex: Int? = null  // Track who originally rolled the gifted dice
)
