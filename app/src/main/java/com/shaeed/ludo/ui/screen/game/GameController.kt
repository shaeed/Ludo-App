package com.shaeed.ludo.ui.screen.game

import android.content.Context
import com.shaeed.ludo.data.SavedGame
import com.shaeed.ludo.engine.Move
import com.shaeed.ludo.model.BoardLayout
import com.shaeed.ludo.model.GameState

/**
 * Shared contract between [GameViewModel] (local game) and OnlineGameViewModel (online game).
 * [GameScreen] depends only on this interface so it can render both modes without change.
 */
interface GameController {
    val gameState: GameState
    val legalMoves: List<Move>
    val isRolling: Boolean
    val tokenAnimation: TokenAnimation?
    val isAnimating: Boolean
    val isUsingGiftedDice: Boolean
    val friendMode: Boolean

    /** False for online games where saving to disk makes no sense. */
    val canSave: Boolean

    fun getLayout(): BoardLayout
    fun rollDice()
    fun onCellTapped(row: Int, col: Int)
    fun saveGame(context: Context, name: String): SavedGame
}
