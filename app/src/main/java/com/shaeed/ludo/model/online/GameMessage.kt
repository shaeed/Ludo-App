package com.shaeed.ludo.model.online

import kotlinx.serialization.Serializable

enum class MessageType {
    // Client → Server → All other clients (relay)
    GAME_START,       // Host sends GameConfig; relayed to all
    DICE_ROLLED,      // Active player's dice value; relayed to all
    MOVE_EXECUTED,    // Active player's chosen move; relayed to all
    GAME_OVER,        // Game ended; relayed to all

    // Client → Server only (stored for reconnection; not relayed)
    STATE_CHECKPOINT,

    // Server → Client only
    PLAYER_JOINED,    // A player connected to the room
    PLAYER_LEFT,      // A player disconnected
    STATE_SYNC,       // Full GameState sent to a reconnecting player
    ERROR,
}

@Serializable
data class GameMessage(
    val type: MessageType,

    val playerColor: String? = null,
    val playerName: String? = null,

    // DICE_ROLLED
    val diceValue: Int? = null,

    // MOVE_EXECUTED — JSON-encoded Move
    val moveJson: String? = null,

    // GAME_START — JSON-encoded GameConfig
    val configJson: String? = null,

    // STATE_CHECKPOINT / STATE_SYNC — JSON-encoded GameState
    val stateJson: String? = null,

    val errorMessage: String? = null,
)
