package com.shaeed.ludo.ui.screen.game

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaeed.ludo.ai.*
import com.shaeed.ludo.data.GameRepository
import com.shaeed.ludo.data.SavedGame
import com.shaeed.ludo.engine.GameEngine
import com.shaeed.ludo.engine.Move
import com.shaeed.ludo.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class TokenAnimation(
    val tokenId: Int,
    val tokenColor: PlayerColor,
    val currentCell: Cell
)

object RestoredGameHolder {
    var pending: SavedGame? = null
}

class GameViewModel : ViewModel() {

    private val layout: BoardLayout = StandardBoardLayout()
    private val config: GameConfig
    private val engine: GameEngine
    private var currentSaveId: String? = null

    var gameState by mutableStateOf<GameState>(
        GameState(emptyList(), 0, null, GamePhase.WAITING_FOR_ROLL, null)
    )
        private set

    var legalMoves by mutableStateOf<List<Move>>(emptyList())
        private set

    var isRolling by mutableStateOf(false)
        private set

    var tokenAnimation by mutableStateOf<TokenAnimation?>(null)
        private set

    var isAnimating by mutableStateOf(false)
        private set

    var isUsingGiftedDice by mutableStateOf(false)
        private set

    init {
        val restored = RestoredGameHolder.pending
        if (restored != null) {
            RestoredGameHolder.pending = null
            GameConfigHolder.current = restored.gameConfig
            config = restored.gameConfig
            engine = GameEngine(layout, config)
            gameState = restored.gameState
            currentSaveId = restored.id
        } else {
            config = GameConfigHolder.current
            engine = GameEngine(layout, config)
            gameState = engine.createInitialState()
        }
        checkAndHandleTurn()
    }

    fun getLayout(): BoardLayout = layout

    fun saveGame(context: Context, name: String): SavedGame {
        val repo = GameRepository(context)
        val id = currentSaveId
        val saved = if (id != null) {
            repo.update(id, gameState, config, name)
        } else {
            repo.save(gameState, config, name)
        }
        currentSaveId = saved.id
        return saved
    }

    fun rollDice() {
        if (isRolling) return
        if (isAnimating) return
        if (gameState.phase != GamePhase.WAITING_FOR_ROLL) return
        val currentPlayer = gameState.players[gameState.currentPlayerIndex]
        if (currentPlayer.isAI) return

        isRolling = true
        viewModelScope.launch {
            delay(400) // Visual rolling delay
            val newState = engine.rollDice(gameState)
            gameState = newState
            isRolling = false
            legalMoves = if (newState.phase == GamePhase.WAITING_FOR_MOVE) {
                engine.moveValidator.computeLegalMoves(newState)
            } else {
                emptyList()
            }

            // Auto-move if there's only one meaningful choice
            if (legalMoves.isNotEmpty() && shouldAutoMove(legalMoves)) {
                delay(400) // Let user see the dice result
                executeMove(legalMoves.first())
                return@launch
            }

            checkAndHandleTurn()
        }
    }

    fun onCellTapped(row: Int, col: Int) {
        if (isAnimating) return
        if (gameState.phase != GamePhase.WAITING_FOR_MOVE) return
        val currentPlayer = gameState.players[gameState.currentPlayerIndex]
        if (currentPlayer.isAI) return

        // Find which move corresponds to this tap
        val matchedMove = findMoveForTap(row, col)
        if (matchedMove != null) {
            executeMove(matchedMove, usingGiftedDice = isUsingGiftedDice)
        }
    }

    private fun executeMove(move: Move, usingGiftedDice: Boolean = false) {
        viewModelScope.launch {
            isAnimating = true
            legalMoves = emptyList()

            // Save state before any visual modifications (used for engine processing)
            val stateForEngine = gameState

            // Calculate the path from current cell to destination
            val path = engine.pathCalculator.calculatePath(move.token, move.destination)

            // Animate through each cell in the path
            for (cell in path) {
                tokenAnimation = TokenAnimation(
                    tokenId = move.token.id,
                    tokenColor = move.token.color,
                    currentCell = cell
                )
                delay(120) // Delay at each cell
            }
            tokenAnimation = null

            // Animate captured tokens back to base
            if (move.captures.isNotEmpty()) {
                // Temporarily update state to show moving token at destination
                val currentPlayer = gameState.players[gameState.currentPlayerIndex]
                val updatedTokens = currentPlayer.tokens.map { token ->
                    if (token.id == move.token.id) token.copy(cell = move.destination) else token
                }
                val updatedPlayer = currentPlayer.copy(tokens = updatedTokens)
                gameState = gameState.copy(
                    players = gameState.players.mapIndexed { index, player ->
                        if (index == gameState.currentPlayerIndex) updatedPlayer else player
                    }
                )

                for (capturedToken in move.captures) {
                    val reversePath = engine.pathCalculator.calculateReversePath(capturedToken)
                    for (cell in reversePath) {
                        tokenAnimation = TokenAnimation(
                            tokenId = capturedToken.id,
                            tokenColor = capturedToken.color,
                            currentCell = cell
                        )
                        delay(120)
                    }
                    tokenAnimation = null
                }
            }

            // Clear animation and apply the final state
            isAnimating = false

            if (usingGiftedDice) {
                // After using gifted dice, resume from the player after the original roller
                val resumePlayerIndex = engine.getResumePlayerIndexAfterGiftedDice(stateForEngine)
                val newState = engine.executeMove(stateForEngine, move)

                gameState = if (newState.phase != GamePhase.GAME_OVER) {
                    newState.copy(
                        currentPlayerIndex = resumePlayerIndex,
                        phase = GamePhase.WAITING_FOR_ROLL,
                        dice = null,
                        giftedDiceOriginalPlayerIndex = null,
                        consecutiveSixes = 0
                    )
                } else {
                    newState
                }
                isUsingGiftedDice = false
            } else {
                gameState = engine.executeMove(stateForEngine, move)
            }
            checkAndHandleTurn()
        }
    }

    private fun checkAndHandleTurn() {
        val state = gameState
        if (state.phase == GamePhase.GAME_OVER) return

        val currentPlayer = state.players[state.currentPlayerIndex]

        // Check for gifted dice first (applies to both human and AI)
        if (state.giftedDice != null) {
            handleGiftedDice(currentPlayer)
            return
        }

        // Handle AI turn
        if (currentPlayer.isAI) {
            handleAiTurn(currentPlayer)
        }
    }

    private fun handleGiftedDice(currentPlayer: Player) {
        viewModelScope.launch {
            delay(300) // Brief pause to show gifted dice

            // Apply the gifted dice
            val stateWithDice = engine.applyGiftedDice(gameState)
            gameState = stateWithDice
            isUsingGiftedDice = true

            val moves = engine.moveValidator.computeLegalMoves(stateWithDice)
            legalMoves = moves

            if (moves.isNotEmpty()) {
                if (currentPlayer.isAI) {
                    delay(400) // AI thinking
                    val strategy = getStrategy(currentPlayer.difficulty)
                    val chosenMove = strategy.chooseMove(stateWithDice, moves, layout)
                    executeMove(chosenMove, usingGiftedDice = true)
                } else if (shouldAutoMove(moves)) {
                    delay(400) // Let user see the dice result
                    executeMove(moves.first(), usingGiftedDice = true)
                }
                // For human player with multiple choices, wait for tap
            } else {
                // Safety fallback: No moves with gifted dice (shouldn't happen as we pre-check)
                // Resume from the player after the original roller
                val resumePlayerIndex = engine.getResumePlayerIndexAfterGiftedDice(gameState)
                isUsingGiftedDice = false
                legalMoves = emptyList()
                gameState = stateWithDice.copy(
                    currentPlayerIndex = resumePlayerIndex,
                    dice = null,
                    giftedDiceOriginalPlayerIndex = null,
                    phase = GamePhase.WAITING_FOR_ROLL
                )
                checkAndHandleTurn()
            }
        }
    }

    private fun handleAiTurn(currentPlayer: Player) {
        viewModelScope.launch {
            delay(600) // Thinking delay

            if (gameState.phase == GamePhase.WAITING_FOR_ROLL) {
                // AI rolls dice
                isRolling = true
                delay(400)
                val newState = engine.rollDice(gameState)
                gameState = newState
                isRolling = false

                if (newState.phase == GamePhase.WAITING_FOR_MOVE) {
                    val moves = engine.moveValidator.computeLegalMoves(newState)
                    legalMoves = moves

                    if (moves.isNotEmpty()) {
                        delay(400) // Thinking before move
                        val strategy = getStrategy(currentPlayer.difficulty)
                        val chosenMove = strategy.chooseMove(newState, moves, layout)
                        executeMove(chosenMove)
                    }
                } else {
                    // No moves or auto-skipped, check if still AI's turn or gifted dice passed
                    legalMoves = emptyList()
                    checkAndHandleTurn()
                }
            }
        }
    }

    private fun shouldAutoMove(moves: List<Move>): Boolean {
        if (moves.size == 1) return true
        // All moves go to the same destination (e.g. all tokens at same cell)
        return moves.all { it.destination == moves.first().destination }
    }

    private fun getStrategy(difficulty: AiDifficulty): AiStrategy = when (difficulty) {
        AiDifficulty.EASY -> EasyAiStrategy()
        AiDifficulty.MEDIUM -> MediumAiStrategy()
        AiDifficulty.HARD -> HardAiStrategy()
    }

    private fun findMoveForTap(row: Int, col: Int): Move? {
        // Check if tapping on a movable token in base
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

        // Check if tapping on a token on the board
        for (token in currentPlayer.tokens) {
            if (token.cell is Cell.Normal || token.cell is Cell.HomeStretch) {
                val (tRow, tCol) = layout.cellToGrid(token.cell)
                if (tRow == row && tCol == col) {
                    return legalMoves.firstOrNull { it.token.id == token.id && it.token.color == token.color }
                }
            }
        }

        // Check if tapping on a destination cell
        for (move in legalMoves) {
            if (move.destination !is Cell.Base) {
                val (dRow, dCol) = layout.cellToGrid(move.destination)
                if (dRow == row && dCol == col) {
                    return move
                }
            }
        }

        return null
    }
}
