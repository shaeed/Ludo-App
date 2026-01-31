package com.shaeed.ludo.ai

import com.shaeed.ludo.engine.Move
import com.shaeed.ludo.model.BoardLayout
import com.shaeed.ludo.model.Cell
import com.shaeed.ludo.model.GameState
import com.shaeed.ludo.model.PlayerColor

class HardAiStrategy : AiStrategy {
    override suspend fun chooseMove(state: GameState, legalMoves: List<Move>, layout: BoardLayout): Move {
        val currentColor = state.players[state.currentPlayerIndex].color
        val opponentTokens = state.players
            .filter { it.color != currentColor }
            .flatMap { it.tokens }

        return legalMoves.maxByOrNull { scoreMove(it, opponentTokens, layout, currentColor) }
            ?: legalMoves.first()
    }

    private fun scoreMove(
        move: Move,
        opponentTokens: List<com.shaeed.ludo.model.Token>,
        layout: BoardLayout,
        myColor: PlayerColor
    ): Int {
        var score = 0

        // Prefer captures heavily
        if (move.captures.isNotEmpty()) score += 60

        // Prefer entering the board from base
        if (move.token.cell is Cell.Base) score += 30

        // Prefer reaching home
        if (move.destination is Cell.Home) score += 50

        // Prefer safe zones
        if (move.destination is Cell.Normal && move.destination.isSafe) score += 25

        // Prefer advancing in home stretch (safe from capture)
        if (move.destination is Cell.HomeStretch) score += 20

        // Risk assessment: penalize landing on exposed positions
        if (move.destination is Cell.Normal && !move.destination.isSafe) {
            val destIndex = move.destination.index
            val dangerCount = countThreateningOpponents(destIndex, opponentTokens, layout, myColor)
            score -= dangerCount * 15
        }

        // Bonus for advancing tokens that are further along (finish them off)
        val path = layout.fullPath(myColor)
        val destPathIndex = path.indexOfFirst { cellsMatch(it, move.destination) }
        if (destPathIndex > 0) {
            score += destPathIndex / 5 // Small bonus for progress
        }

        return score
    }

    /**
     * Count how many opponent tokens could potentially capture the token at [trackIndex]
     * within the next roll (1-6 steps away behind it).
     */
    private fun countThreateningOpponents(
        trackIndex: Int,
        opponentTokens: List<com.shaeed.ludo.model.Token>,
        layout: BoardLayout,
        myColor: PlayerColor
    ): Int {
        var threats = 0
        for (token in opponentTokens) {
            if (token.cell !is Cell.Normal) continue
            val opponentIndex = (token.cell as Cell.Normal).index
            // Check if opponent is 1-6 steps behind (in their walking direction)
            val opponentPath = layout.fullPath(token.color)
            val opponentPathIndex = opponentPath.indexOfFirst {
                it is Cell.Normal && it.index == opponentIndex
            }
            val targetPathIndex = opponentPath.indexOfFirst {
                it is Cell.Normal && it.index == trackIndex
            }
            if (opponentPathIndex > 0 && targetPathIndex > 0) {
                val distance = targetPathIndex - opponentPathIndex
                if (distance in 1..6) {
                    threats++
                }
            }
        }
        return threats
    }

    private fun cellsMatch(a: Cell, b: Cell): Boolean = when {
        a is Cell.Base && b is Cell.Base -> a.color == b.color
        a is Cell.Normal && b is Cell.Normal -> a.index == b.index
        a is Cell.HomeStretch && b is Cell.HomeStretch -> a.color == b.color && a.index == b.index
        a is Cell.Home && b is Cell.Home -> a.color == b.color
        else -> false
    }
}
