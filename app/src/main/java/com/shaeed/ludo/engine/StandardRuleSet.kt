package com.shaeed.ludo.engine

import com.shaeed.ludo.model.Cell
import com.shaeed.ludo.model.GameConfig
import com.shaeed.ludo.model.Token

class StandardRuleSet(private val config: GameConfig) : RuleSet {

    override fun canEnterBoard(diceValue: Int): Boolean {
        return if (config.enterOnSixOnly) diceValue == 6 else diceValue == 6 || diceValue == 1
    }

    override fun grantsExtraTurn(diceValue: Int, consecutiveSixes: Int, maxConsecutiveSixes: Int): Boolean {
        if (diceValue != 6) return false
        // If rolling another 6 would exceed max, no extra turn (turn forfeited)
        return consecutiveSixes + 1 < maxConsecutiveSixes
    }

    override fun isCaptured(movingToken: Token, destination: Cell, allTokens: List<Token>): List<Token> {
        if (destination !is Cell.Normal) return emptyList()
        if (config.safeZonesEnabled && destination.isSafe) return emptyList()

        return allTokens.filter { other ->
            other.color != movingToken.color &&
            other.cell is Cell.Normal &&
            (other.cell as Cell.Normal).index == destination.index
        }
    }

    override fun requiresExactRoll(): Boolean = true

    override fun shouldForfeitForConsecutiveSixes(consecutiveSixes: Int, maxConsecutiveSixes: Int): Boolean {
        return consecutiveSixes >= maxConsecutiveSixes
    }
}
