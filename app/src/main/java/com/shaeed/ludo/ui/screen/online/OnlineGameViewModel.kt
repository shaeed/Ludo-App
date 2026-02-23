package com.shaeed.ludo.ui.screen.online

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaeed.ludo.audio.GameSound
import com.shaeed.ludo.audio.SoundManagerHolder
import com.shaeed.ludo.data.SavedGame
import com.shaeed.ludo.engine.GameEngine
import com.shaeed.ludo.engine.Move
import com.shaeed.ludo.model.*
import com.shaeed.ludo.model.online.GameMessage
import com.shaeed.ludo.model.online.MessageType
import com.shaeed.ludo.network.OnlineRoomRepository
import com.shaeed.ludo.network.WebSocketClient
import com.shaeed.ludo.ui.screen.game.GameController
import com.shaeed.ludo.ui.screen.game.TokenAnimation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class OnlineSessionPhase { CONNECTING, LOBBY, IN_GAME, DISCONNECTED }

class OnlineGameViewModel : ViewModel(), GameController {

    // ---- Identity / session ----
    val myColor: PlayerColor
    val myName: String
    val roomCode: String
    val isHost: Boolean

    // ---- Online session state ----
    var sessionPhase by mutableStateOf(OnlineSessionPhase.CONNECTING)
        private set
    var connectedPlayers by mutableStateOf<List<String>>(emptyList())
        private set
    var connectionError by mutableStateOf<String?>(null)
        private set

    // ---- GameController state ----
    override var gameState by mutableStateOf(
        GameState(emptyList(), 0, null, GamePhase.WAITING_FOR_ROLL, null)
    )
        private set
    override var legalMoves by mutableStateOf<List<Move>>(emptyList())
        private set
    override var isRolling by mutableStateOf(false)
        private set
    override var tokenAnimation by mutableStateOf<TokenAnimation?>(null)
        private set
    override var isAnimating by mutableStateOf(false)
        private set
    override var isUsingGiftedDice by mutableStateOf(false)
        private set
    override val canSave: Boolean = false

    // ---- Engine ----
    private val layout: BoardLayout = StandardBoardLayout()
    private lateinit var config: GameConfig
    private lateinit var engine: GameEngine
    override val friendMode: Boolean get() = if (::config.isInitialized) config.friendMode else false

    // ---- Networking ----
    private val json = Json { ignoreUnknownKeys = true }
    private lateinit var wsClient: WebSocketClient

    init {
        val holder = OnlineGameHolder
        myColor = holder.myColor
        myName = holder.myName
        roomCode = holder.roomCode
        isHost = holder.isHost

        if (isHost) {
            val cfg = holder.gameConfig ?: GameConfig(
                playerConfigs = listOf(
                    PlayerConfig(myColor, myName, isAI = false),
                )
            )
            config = cfg
            engine = GameEngine(layout, cfg)
        }

        connectWebSocket(holder.serverBaseUrl)
    }

    // ---- WebSocket lifecycle ----

    private fun connectWebSocket(serverBaseUrl: String) {
        val wsUrl = OnlineRoomRepository(serverBaseUrl)
            .buildWsUrl(roomCode, myColor.name, myName)
        wsClient = WebSocketClient(wsUrl)
        wsClient.connect()
        sessionPhase = OnlineSessionPhase.LOBBY

        viewModelScope.launch {
            wsClient.incomingMessages.collect { message ->
                handleMessage(message)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        wsClient.close()
    }

    // ---- Host: start the game ----

    fun startGame() {
        if (!isHost || !::engine.isInitialized) return
        val configJson = json.encodeToString(config)
        wsClient.send(GameMessage(type = MessageType.GAME_START, configJson = configJson))
        // Host initialises its own state immediately (server doesn't echo back to sender)
        gameState = engine.createInitialState()
        sessionPhase = OnlineSessionPhase.IN_GAME
        connectedPlayers = connectedPlayers + myColor.name
    }

    // ---- Message handler ----

    private fun handleMessage(message: GameMessage) {
        when (message.type) {

            MessageType.PLAYER_JOINED -> {
                val color = message.playerColor ?: return
                if (color !in connectedPlayers) {
                    connectedPlayers = connectedPlayers + color
                }
            }

            MessageType.PLAYER_LEFT -> {
                val color = message.playerColor ?: return
                connectedPlayers = connectedPlayers - color
            }

            MessageType.GAME_START -> {
                val cfgJson = message.configJson ?: return
                val cfg = try { json.decodeFromString<GameConfig>(cfgJson) } catch (_: Exception) { return }
                config = cfg
                engine = GameEngine(layout, cfg)
                gameState = engine.createInitialState()
                sessionPhase = OnlineSessionPhase.IN_GAME
            }

            MessageType.DICE_ROLLED -> {
                val value = message.diceValue ?: return
                viewModelScope.launch {
                    isRolling = true
                    delay(400)
                    val newState = engine.applyDiceValue(gameState, value)
                    gameState = newState
                    isRolling = false
                    // Only compute legal moves if it became the local player's turn
                    legalMoves = if (newState.phase == GamePhase.WAITING_FOR_MOVE && isMyTurn) {
                        engine.moveValidator.computeLegalMoves(newState)
                    } else {
                        emptyList()
                    }
                    if (legalMoves.size == 1 && isMyTurn) {
                        delay(400)
                        executeMoveAndBroadcast(legalMoves.first())
                    }
                }
            }

            MessageType.MOVE_EXECUTED -> {
                val move = message.moveJson?.let {
                    try { json.decodeFromString<Move>(it) } catch (_: Exception) { null }
                } ?: return
                viewModelScope.launch {
                    runMoveAnimation(move, sendToNetwork = false)
                }
            }

            MessageType.STATE_SYNC -> {
                val stateJson = message.stateJson ?: return
                val state = try { json.decodeFromString<GameState>(stateJson) } catch (_: Exception) { return }
                gameState = state
                legalMoves = if (state.phase == GamePhase.WAITING_FOR_MOVE && isMyTurn) {
                    engine.moveValidator.computeLegalMoves(state)
                } else emptyList()
                if (sessionPhase != OnlineSessionPhase.IN_GAME) {
                    sessionPhase = OnlineSessionPhase.IN_GAME
                }
            }

            else -> Unit
        }
    }

    // ---- GameController overrides ----

    override fun getLayout(): BoardLayout = layout

    override fun rollDice() {
        if (!isMyTurn) return
        if (isRolling || isAnimating) return
        if (gameState.phase != GamePhase.WAITING_FOR_ROLL) return

        val value = (1..6).random()

        viewModelScope.launch {
            isRolling = true
            launch { SoundManagerHolder.instance.play(GameSound.DICE_ROLL) }
            delay(400)
            val newState = engine.applyDiceValue(gameState, value)
            gameState = newState
            isRolling = false

            // Broadcast to others
            wsClient.send(GameMessage(type = MessageType.DICE_ROLLED, diceValue = value))

            legalMoves = if (newState.phase == GamePhase.WAITING_FOR_MOVE) {
                engine.moveValidator.computeLegalMoves(newState)
            } else emptyList()

            if (legalMoves.size == 1) {
                delay(400)
                executeMoveAndBroadcast(legalMoves.first())
            }
        }
    }

    override fun onCellTapped(row: Int, col: Int) {
        if (!isMyTurn || isAnimating) return
        if (gameState.phase != GamePhase.WAITING_FOR_MOVE) return

        val move = findMoveForTap(row, col) ?: return
        viewModelScope.launch {
            executeMoveAndBroadcast(move)
        }
    }

    override fun saveGame(context: Context, name: String): SavedGame {
        throw UnsupportedOperationException("Online games cannot be saved")
    }

    // ---- Move execution ----

    private suspend fun executeMoveAndBroadcast(move: Move) {
        legalMoves = emptyList()

        // Broadcast before animation so others can start rendering immediately
        wsClient.send(GameMessage(
            type = MessageType.MOVE_EXECUTED,
            moveJson = json.encodeToString(move),
        ))

        runMoveAnimation(move, sendToNetwork = false)

        // Send full state snapshot for reconnection support
        wsClient.send(GameMessage(
            type = MessageType.STATE_CHECKPOINT,
            stateJson = json.encodeToString(gameState),
        ))
    }

    /**
     * Runs the token move animation and updates [gameState].
     * Mirrors [GameViewModel]'s executeMove logic exactly so both sides animate identically.
     * [sendToNetwork] is unused (reserved for future use); broadcasting is done by the caller.
     */
    private suspend fun runMoveAnimation(move: Move, sendToNetwork: Boolean) {
        if (!::engine.isInitialized) return
        isAnimating = true
        legalMoves = emptyList()

        val stateForEngine = gameState
        val path = engine.pathCalculator.calculatePath(move.token, move.destination)

        if (path.isNotEmpty()) {
            launch { SoundManagerHolder.instance.play(GameSound.TOKEN_MOVE) }
        }

        for (cell in path) {
            tokenAnimation = TokenAnimation(
                tokenId = move.token.id,
                tokenColor = move.token.color,
                currentCell = cell,
            )
            delay(120)
        }
        tokenAnimation = null

        if (move.captures.isNotEmpty()) {
            launch { SoundManagerHolder.instance.play(GameSound.TOKEN_CAPTURE) }
            val currentPlayer = gameState.players[gameState.currentPlayerIndex]
            val updatedTokens = currentPlayer.tokens.map { token ->
                if (token.id == move.token.id) token.copy(cell = move.destination) else token
            }
            gameState = gameState.copy(
                players = gameState.players.mapIndexed { index, player ->
                    if (index == gameState.currentPlayerIndex) player.copy(tokens = updatedTokens) else player
                }
            )
            for (capturedToken in move.captures) {
                val reversePath = engine.pathCalculator.calculateReversePath(capturedToken)
                for (cell in reversePath) {
                    tokenAnimation = TokenAnimation(
                        tokenId = capturedToken.id,
                        tokenColor = capturedToken.color,
                        currentCell = cell,
                    )
                    delay(120)
                }
                tokenAnimation = null
            }
        }

        if (move.destination is Cell.Home) {
            launch { SoundManagerHolder.instance.play(GameSound.TOKEN_HOME) }
        }

        isAnimating = false
        val newState = engine.executeMove(stateForEngine, move)
        if (newState.phase == GamePhase.GAME_OVER) {
            launch { SoundManagerHolder.instance.play(GameSound.GAME_WIN) }
        }
        gameState = newState

        // After the move, compute legal moves if it is now the local player's turn
        legalMoves = if (newState.phase == GamePhase.WAITING_FOR_MOVE && isMyTurn) {
            engine.moveValidator.computeLegalMoves(newState)
        } else emptyList()
    }

    // ---- Helpers ----

    val isMyTurn: Boolean
        get() = gameState.players.getOrNull(gameState.currentPlayerIndex)?.color == myColor

    private fun findMoveForTap(row: Int, col: Int): Move? {
        val currentPlayer = gameState.players[gameState.currentPlayerIndex]
        val basePositions = layout.basePositions(currentPlayer.color)
        val baseTokens = currentPlayer.tokens.filter { it.cell is Cell.Base }

        for ((idx, token) in baseTokens.withIndex()) {
            if (idx < basePositions.size) {
                val (bRow, bCol) = basePositions[idx]
                if (bRow == row && bCol == col) {
                    return legalMoves.firstOrNull { it.token.id == token.id && it.token.color == token.color }
                }
            }
        }

        for (token in currentPlayer.tokens) {
            if (token.cell is Cell.Normal || token.cell is Cell.HomeStretch) {
                val (tRow, tCol) = layout.cellToGrid(token.cell)
                if (tRow == row && tCol == col) {
                    return legalMoves.firstOrNull { it.token.id == token.id && it.token.color == token.color }
                }
            }
        }

        for (move in legalMoves) {
            if (move.destination !is Cell.Base) {
                val (dRow, dCol) = layout.cellToGrid(move.destination)
                if (dRow == row && dCol == col) return move
            }
        }

        return null
    }

    // Needed for coroutine launch inside a suspend fun
    private fun launch(block: suspend () -> Unit) = viewModelScope.launch { block() }
}
