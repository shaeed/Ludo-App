package com.shaeed.ludo.ui.screen.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaeed.ludo.ai.*
import com.shaeed.ludo.engine.GameEngine
import com.shaeed.ludo.engine.Move
import com.shaeed.ludo.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {

    private val layout: BoardLayout = StandardBoardLayout()
    private val config: GameConfig = GameConfigHolder.current
    private val engine = GameEngine(layout, config)

    var gameState by mutableStateOf(engine.createInitialState())
        private set

    var legalMoves by mutableStateOf<List<Move>>(emptyList())
        private set

    var isRolling by mutableStateOf(false)
        private set

    init {
        checkAndHandleAiTurn()
    }

    fun getLayout(): BoardLayout = layout

    fun rollDice() {
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
            checkAndHandleAiTurn()
        }
    }

    fun onCellTapped(row: Int, col: Int) {
        if (gameState.phase != GamePhase.WAITING_FOR_MOVE) return
        val currentPlayer = gameState.players[gameState.currentPlayerIndex]
        if (currentPlayer.isAI) return

        // Find which move corresponds to this tap
        val matchedMove = findMoveForTap(row, col)
        if (matchedMove != null) {
            executeMove(matchedMove)
        }
    }

    fun onTokenTapped(tokenId: Int, tokenColor: PlayerColor) {
        if (gameState.phase != GamePhase.WAITING_FOR_MOVE) return
        val currentPlayer = gameState.players[gameState.currentPlayerIndex]
        if (currentPlayer.isAI) return
        if (tokenColor != currentPlayer.color) return

        val move = legalMoves.firstOrNull { it.token.id == tokenId && it.token.color == tokenColor }
        if (move != null) {
            executeMove(move)
        }
    }

    private fun executeMove(move: Move) {
        gameState = engine.executeMove(gameState, move)
        legalMoves = emptyList()
        checkAndHandleAiTurn()
    }

    private fun checkAndHandleAiTurn() {
        val state = gameState
        if (state.phase == GamePhase.GAME_OVER) return

        val currentPlayer = state.players[state.currentPlayerIndex]
        if (!currentPlayer.isAI) return

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
                    // No moves or auto-skipped, check if still AI's turn
                    legalMoves = emptyList()
                    checkAndHandleAiTurn()
                }
            }
        }
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
