package com.shaeed.ludo.ai

import com.shaeed.ludo.engine.Move
import com.shaeed.ludo.model.BoardLayout
import com.shaeed.ludo.model.Cell
import com.shaeed.ludo.model.GameState

class MediumAiStrategy : AiStrategy {
    override suspend fun chooseMove(state: GameState, legalMoves: List<Move>, layout: BoardLayout): Move {
        return legalMoves.maxByOrNull { scoreMove(it) } ?: legalMoves.first()
    }

    private fun scoreMove(move: Move): Int {
        var score = 0

        // Prefer captures
        if (move.captures.isNotEmpty()) score += 50

        // Prefer entering the board from base
        if (move.token.cell is Cell.Base) score += 30

        // Prefer reaching home
        if (move.destination is Cell.Home) score += 40

        // Prefer safe zones
        if (move.destination is Cell.Normal && move.destination.isSafe) score += 20

        // Prefer advancing in home stretch
        if (move.destination is Cell.HomeStretch) score += 15

        return score
    }
}
