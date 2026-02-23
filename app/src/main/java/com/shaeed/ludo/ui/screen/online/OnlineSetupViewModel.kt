package com.shaeed.ludo.ui.screen.online

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shaeed.ludo.data.UserPreferences
import com.shaeed.ludo.model.*
import com.shaeed.ludo.network.OnlineRoomRepository
import kotlinx.coroutines.launch

class OnlineSetupViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = UserPreferences(application)

    // ---- Shared ----
    var serverUrl by mutableStateOf(OnlineGameHolder.serverBaseUrl)
        private set
    var myName by mutableStateOf("Player")
        private set
    var myColor by mutableStateOf(PlayerColor.RED)
        private set

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // ---- Create Room (host) ----
    var playerCount by mutableIntStateOf(2)
        private set
    var maxConsecutiveSixes by mutableIntStateOf(prefs.maxConsecutiveSixes)
        private set
    var passDiceToNextPlayer by mutableStateOf(prefs.passDiceToNextPlayer)
        private set
    var friendMode by mutableStateOf(false)
        private set
    var createdRoomCode by mutableStateOf<String?>(null)
        private set

    // ---- Join Room ----
    var joinCode by mutableStateOf("")
        private set

    // ---- Update functions ----

    fun updateServerUrl(url: String) { serverUrl = url }
    fun updateMyName(name: String) { myName = name }
    fun updateMyColor(color: PlayerColor) { myColor = color }
    fun updateJoinCode(code: String) { joinCode = code.uppercase().take(6) }
    fun updatePlayerCount(count: Int) { playerCount = count.coerceIn(2, 4) }
    fun updateMaxConsecutiveSixes(value: Int) { maxConsecutiveSixes = value.coerceIn(1, 5) }
    fun togglePassDice() { passDiceToNextPlayer = !passDiceToNextPlayer }
    fun toggleFriendMode() { friendMode = !friendMode }
    fun clearError() { errorMessage = null }

    // ---- Host: create room ----

    fun createRoom(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val repo = OnlineRoomRepository(serverUrl)
                val code = repo.createRoom()
                createdRoomCode = code
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Could not create room: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /** Called when the host taps "Start" after the room code has been created. */
    fun prepareHostAndNavigate(onNavigate: () -> Unit) {
        val code = createdRoomCode ?: return
        OnlineGameHolder.isHost = true
        OnlineGameHolder.myColor = myColor
        OnlineGameHolder.myName = myName
        OnlineGameHolder.roomCode = code
        OnlineGameHolder.serverBaseUrl = serverUrl
        OnlineGameHolder.gameConfig = buildHostConfig()
        onNavigate()
    }

    // ---- Joiner: validate and join ----

    fun joinRoom(onNavigate: () -> Unit) {
        val code = joinCode.uppercase().trim()
        if (code.length != 6) {
            errorMessage = "Room code must be 6 characters"
            return
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val repo = OnlineRoomRepository(serverUrl)
                val exists = repo.validateRoom(code)
                if (!exists) {
                    errorMessage = "Room \"$code\" not found"
                    return@launch
                }
                OnlineGameHolder.isHost = false
                OnlineGameHolder.myColor = myColor
                OnlineGameHolder.myName = myName
                OnlineGameHolder.roomCode = code
                OnlineGameHolder.serverBaseUrl = serverUrl
                OnlineGameHolder.gameConfig = null
                onNavigate()
            } catch (e: Exception) {
                errorMessage = "Could not reach server: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // ---- Internal helpers ----

    private fun buildHostConfig(): GameConfig {
        val colors = listOf(PlayerColor.RED, PlayerColor.GREEN, PlayerColor.YELLOW, PlayerColor.BLUE)
        val playerConfigs = colors.take(playerCount).mapIndexed { index, color ->
            PlayerConfig(
                color = color,
                name = if (color == myColor) myName else "Player ${index + 1}",
                isAI = false,
            )
        }
        return GameConfig(
            playerConfigs = playerConfigs,
            enterOnSixOnly = prefs.enterOnSixOnly,
            safeZonesEnabled = prefs.safeZonesEnabled,
            maxConsecutiveSixes = maxConsecutiveSixes,
            passDiceToNextPlayer = passDiceToNextPlayer,
            friendMode = friendMode,
        )
    }
}
