package com.shaeed.ludo.engine

import com.shaeed.ludo.model.Cell
import com.shaeed.ludo.model.GameState
import com.shaeed.ludo.model.Token

interface RuleSet {
    fun canEnterBoard(diceValue: Int): Boolean
    fun grantsExtraTurn(diceValue: Int, consecutiveSixes: Int, maxConsecutiveSixes: Int): Boolean
    fun isCaptured(movingToken: Token, destination: Cell, allTokens: List<Token>): List<Token>
    fun requiresExactRoll(): Boolean
    fun shouldForfeitForConsecutiveSixes(consecutiveSixes: Int, maxConsecutiveSixes: Int): Boolean
}
