package com.shaeed.ludo.model

import kotlinx.serialization.Serializable

@Serializable
enum class GamePhase {
    WAITING_FOR_ROLL,
    ROLLING,
    WAITING_FOR_MOVE,
    ANIMATING,
    GAME_OVER
}
