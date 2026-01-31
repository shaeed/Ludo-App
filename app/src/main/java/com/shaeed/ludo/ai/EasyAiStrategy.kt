package com.shaeed.ludo.ai

import com.shaeed.ludo.engine.Move
import com.shaeed.ludo.model.BoardLayout
import com.shaeed.ludo.model.GameState

class EasyAiStrategy : AiStrategy {
    override suspend fun chooseMove(state: GameState, legalMoves: List<Move>, layout: BoardLayout): Move {
        return legalMoves.random()
    }
}
