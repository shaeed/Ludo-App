package com.shaeed.ludo.ui.screen.online

import com.shaeed.ludo.model.GameConfig
import com.shaeed.ludo.model.PlayerColor

/**
 * Transient holder that carries online session parameters across navigation.
 * Set by [OnlineSetupViewModel] before navigating to [OnlineGameScreen].
 */
object OnlineGameHolder {
    var isHost: Boolean = false
    var myColor: PlayerColor = PlayerColor.RED
    var myName: String = "Player"
    var roomCode: String = ""
    var serverBaseUrl: String = "http://10.0.2.2:8080"  // emulator → localhost; change to VM IP for real devices
    var gameConfig: GameConfig? = null  // only set by host
}
