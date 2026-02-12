package com.shaeed.ludo.model

import kotlinx.serialization.Serializable

@Serializable
enum class AiDifficulty {
    EASY, MEDIUM, HARD
}

@Serializable
data class Player(
    val color: PlayerColor,
    val name: String,
    val isAI: Boolean,
    val difficulty: AiDifficulty = AiDifficulty.MEDIUM,
    val tokens: List<Token>,
    val diceValue: Int? = null
)
