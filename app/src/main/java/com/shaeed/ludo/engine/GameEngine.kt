package com.shaeed.ludo.engine

import com.shaeed.ludo.model.*

class GameEngine(
    private val layout: BoardLayout,
    private val config: GameConfig
) {
    private val ruleSet: RuleSet = StandardRuleSet(config)
    private val pathCalculator = PathCalculator(layout)
    val moveValidator = MoveValidator(layout, ruleSet, pathCalculator)

    fun createInitialState(): GameState {
        val players = config.playerConfigs.map { pc ->
            Player(
                color = pc.color,
                name = pc.name,
                isAI = pc.isAI,
                difficulty = pc.difficulty,
                tokens = (0 until 4).map { i ->
                    Token(id = i, color = pc.color, cell = Cell.Base(pc.color))
                }
            )
        }
        return GameState(
            players = players,
            currentPlayerIndex = 0,
            dice = null,
            phase = GamePhase.WAITING_FOR_ROLL,
            winner = null,
            consecutiveSixes = 0
        )
    }

    fun rollDice(state: GameState): GameState {
        val value = (1..6).random()
        val dice = DiceResult(value)
        val newConsecutiveSixes = if (value == 6) state.consecutiveSixes + 1 else 0

        // Check for consecutive sixes forfeit
        if (ruleSet.shouldForfeitForConsecutiveSixes(newConsecutiveSixes, config.maxConsecutiveSixes)) {
            return advanceToNextPlayer(state.copy(dice = dice, consecutiveSixes = 0))
        }

        val newState = state.copy(
            dice = dice,
            phase = GamePhase.WAITING_FOR_MOVE,
            consecutiveSixes = newConsecutiveSixes
        )

        // Auto-skip if no legal moves
        val legalMoves = moveValidator.computeLegalMoves(newState)
        if (legalMoves.isEmpty()) {
            return if (value == 6 && ruleSet.grantsExtraTurn(value, state.consecutiveSixes, config.maxConsecutiveSixes)) {
                newState.copy(phase = GamePhase.WAITING_FOR_ROLL)
            } else {
                advanceToNextPlayer(newState)
            }
        }

        return newState
    }

    fun executeMove(state: GameState, move: Move): GameState {
        val currentPlayer = state.players[state.currentPlayerIndex]
        val diceValue = state.dice?.value ?: return state

        // Update moving token
        val updatedTokens = currentPlayer.tokens.map { token ->
            if (token.id == move.token.id) token.copy(cell = move.destination) else token
        }
        val updatedCurrentPlayer = currentPlayer.copy(tokens = updatedTokens)

        // Handle captures — send captured tokens back to base
        var updatedPlayers = state.players.map { player ->
            if (player.color == currentPlayer.color) {
                updatedCurrentPlayer
            } else {
                val newTokens = player.tokens.map { token ->
                    if (move.captures.any { it.id == token.id && it.color == token.color }) {
                        token.copy(cell = Cell.Base(token.color))
                    } else {
                        token
                    }
                }
                player.copy(tokens = newTokens)
            }
        }

        // Check for win
        val winner = checkWinner(updatedPlayers)
        if (winner != null) {
            return state.copy(
                players = updatedPlayers,
                phase = GamePhase.GAME_OVER,
                winner = winner
            )
        }

        val newState = state.copy(players = updatedPlayers)

        // Extra turn on 6 (and captures grant extra turn in standard rules)
        val getsExtraTurn = diceValue == 6 &&
            ruleSet.grantsExtraTurn(diceValue, state.consecutiveSixes - 1, config.maxConsecutiveSixes)

        return if (getsExtraTurn) {
            newState.copy(
                phase = GamePhase.WAITING_FOR_ROLL,
                dice = null
            )
        } else {
            advanceToNextPlayer(newState)
        }
    }

    fun skipTurn(state: GameState): GameState {
        return advanceToNextPlayer(state)
    }

    private fun advanceToNextPlayer(state: GameState): GameState {
        val nextIndex = (state.currentPlayerIndex + 1) % state.players.size
        return state.copy(
            currentPlayerIndex = nextIndex,
            phase = GamePhase.WAITING_FOR_ROLL,
            dice = null,
            consecutiveSixes = 0
        )
    }

    private fun checkWinner(players: List<Player>): PlayerColor? {
        return players.firstOrNull { player ->
            player.tokens.all { it.cell is Cell.Home }
        }?.color
    }
}
