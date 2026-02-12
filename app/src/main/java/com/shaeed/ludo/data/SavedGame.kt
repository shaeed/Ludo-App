package com.shaeed.ludo.data

import com.shaeed.ludo.model.GameConfig
import com.shaeed.ludo.model.GameState
import kotlinx.serialization.Serializable

@Serializable
data class SavedGame(
    val id: String,
    val name: String,
    val timestamp: Long,
    val gameState: GameState,
    val gameConfig: GameConfig
)
