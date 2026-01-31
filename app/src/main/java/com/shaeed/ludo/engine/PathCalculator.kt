package com.shaeed.ludo.engine

import com.shaeed.ludo.model.BoardLayout
import com.shaeed.ludo.model.Cell
import com.shaeed.ludo.model.PlayerColor
import com.shaeed.ludo.model.Token

class PathCalculator(private val layout: BoardLayout) {

    /**
     * Calculates the destination cell after moving [steps] forward from token's current position.
     * Returns null if the move is invalid (overshoot past home).
     */
    fun calculateDestination(token: Token, steps: Int): Cell? {
        val path = layout.fullPath(token.color)
        val currentIndex = findPathIndex(token, path) ?: return null
        val targetIndex = currentIndex + steps

        return if (targetIndex < path.size) {
            path[targetIndex]
        } else {
            null // Overshoot — requires exact roll
        }
    }

    /**
     * For a token in base, the destination is the start position (first track cell in player's path).
     * This is index 1 in the full path (index 0 is Base).
     */
    fun enterBoardDestination(color: PlayerColor): Cell {
        return layout.fullPath(color)[1]
    }

    /**
     * Finds the index of the token's current cell within its color's full path.
     */
    private fun findPathIndex(token: Token, path: List<Cell>): Int? {
        return path.indexOfFirst { cellsMatch(it, token.cell) }.takeIf { it >= 0 }
    }

    private fun cellsMatch(a: Cell, b: Cell): Boolean = when {
        a is Cell.Base && b is Cell.Base -> a.color == b.color
        a is Cell.Normal && b is Cell.Normal -> a.index == b.index
        a is Cell.HomeStretch && b is Cell.HomeStretch -> a.color == b.color && a.index == b.index
        a is Cell.Home && b is Cell.Home -> a.color == b.color
        else -> false
    }
}
