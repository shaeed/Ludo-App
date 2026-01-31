package com.shaeed.ludo.engine

import com.shaeed.ludo.model.BoardLayout
import com.shaeed.ludo.model.Cell
import com.shaeed.ludo.model.GameState
import com.shaeed.ludo.model.Token

class MoveValidator(
    private val layout: BoardLayout,
    private val ruleSet: RuleSet,
    private val pathCalculator: PathCalculator
) {

    fun computeLegalMoves(state: GameState): List<Move> {
        val diceValue = state.dice?.value ?: return emptyList()
        val currentPlayer = state.players[state.currentPlayerIndex]
        val allTokens = state.players.flatMap { it.tokens }
        val moves = mutableListOf<Move>()

        for (token in currentPlayer.tokens) {
            when (token.cell) {
                is Cell.Base -> {
                    // Can only enter the board if dice allows
                    if (ruleSet.canEnterBoard(diceValue)) {
                        val dest = pathCalculator.enterBoardDestination(currentPlayer.color)
                        if (!ruleSet.isBlocked(dest, allTokens, token)) {
                            val captures = ruleSet.isCaptured(token, dest, allTokens)
                            moves.add(Move(token, dest, captures))
                        }
                    }
                }
                is Cell.Home -> {
                    // Already home, can't move
                }
                else -> {
                    // Normal or HomeStretch — move forward
                    val dest = pathCalculator.calculateDestination(token, diceValue)
                    if (dest != null && !ruleSet.isBlocked(dest, allTokens, token)) {
                        val captures = ruleSet.isCaptured(token, dest, allTokens)
                        moves.add(Move(token, dest, captures))
                    }
                }
            }
        }

        return moves
    }
}
