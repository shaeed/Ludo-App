package com.shaeed.ludo.ai

import com.shaeed.ludo.engine.Move
import com.shaeed.ludo.model.BoardLayout
import com.shaeed.ludo.model.GameState

interface AiStrategy {
    suspend fun chooseMove(state: GameState, legalMoves: List<Move>, layout: BoardLayout): Move
}
