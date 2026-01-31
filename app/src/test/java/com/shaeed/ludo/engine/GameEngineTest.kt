package com.shaeed.ludo.engine

import com.shaeed.ludo.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GameEngineTest {

    private lateinit var layout: StandardBoardLayout
    private lateinit var config: GameConfig
    private lateinit var engine: GameEngine

    @Before
    fun setUp() {
        layout = StandardBoardLayout()
        config = GameConfig(
            playerConfigs = listOf(
                PlayerConfig(PlayerColor.RED, "Red", false),
                PlayerConfig(PlayerColor.GREEN, "Green", true)
            )
        )
        engine = GameEngine(layout, config)
    }

    @Test
    fun createInitialState_allTokensInBase() {
        val state = engine.createInitialState()
        assertEquals(2, state.players.size)
        assertEquals(0, state.currentPlayerIndex)
        assertEquals(GamePhase.WAITING_FOR_ROLL, state.phase)
        assertNull(state.winner)
        assertNull(state.dice)

        for (player in state.players) {
            assertEquals(4, player.tokens.size)
            player.tokens.forEach { token ->
                assertTrue(token.cell is Cell.Base)
            }
        }
    }

    @Test
    fun rollDice_producesValidResult() {
        // Test with a token on the board so the state isn't auto-skipped
        val redTokens = listOf(
            Token(0, PlayerColor.RED, Cell.Normal(0)),
            Token(1, PlayerColor.RED, Cell.Base(PlayerColor.RED)),
            Token(2, PlayerColor.RED, Cell.Base(PlayerColor.RED)),
            Token(3, PlayerColor.RED, Cell.Base(PlayerColor.RED))
        )
        val greenTokens = (0..3).map { Token(it, PlayerColor.GREEN, Cell.Base(PlayerColor.GREEN)) }
        val state = GameState(
            players = listOf(
                Player(PlayerColor.RED, "Red", false, tokens = redTokens),
                Player(PlayerColor.GREEN, "Green", true, tokens = greenTokens)
            ),
            currentPlayerIndex = 0,
            dice = null,
            phase = GamePhase.WAITING_FOR_ROLL,
            winner = null
        )
        val newState = engine.rollDice(state)
        // Token on board can always move, so dice should be set and phase should be WAITING_FOR_MOVE
        assertNotNull(newState.dice)
        assertTrue(newState.dice!!.value in 1..6)
        assertEquals(GamePhase.WAITING_FOR_MOVE, newState.phase)
    }

    @Test
    fun rollDice_autoSkipsWhenNoMoves() {
        // All tokens in base, roll will likely not be 6 (run multiple times)
        val state = engine.createInitialState()
        val newState = engine.rollDice(state)

        // If auto-skipped (no legal moves and not a 6), dice is cleared
        if (newState.dice == null) {
            assertEquals(GamePhase.WAITING_FOR_ROLL, newState.phase)
            // Skipped to next player
            assertEquals(1, newState.currentPlayerIndex)
        } else {
            // Got a 6, so can enter board
            assertEquals(6, newState.dice!!.value)
            assertEquals(GamePhase.WAITING_FOR_MOVE, newState.phase)
        }
    }

    @Test
    fun executeMove_movesToken() {
        // Set up a state where RED has a token on the board
        val redTokens = listOf(
            Token(0, PlayerColor.RED, Cell.Normal(0)),
            Token(1, PlayerColor.RED, Cell.Base(PlayerColor.RED)),
            Token(2, PlayerColor.RED, Cell.Base(PlayerColor.RED)),
            Token(3, PlayerColor.RED, Cell.Base(PlayerColor.RED))
        )
        val greenTokens = (0..3).map { Token(it, PlayerColor.GREEN, Cell.Base(PlayerColor.GREEN)) }
        val state = GameState(
            players = listOf(
                Player(PlayerColor.RED, "Red", false, tokens = redTokens),
                Player(PlayerColor.GREEN, "Green", true, tokens = greenTokens)
            ),
            currentPlayerIndex = 0,
            dice = DiceResult(4),
            phase = GamePhase.WAITING_FOR_MOVE,
            winner = null
        )

        val move = Move(
            token = redTokens[0],
            destination = Cell.Normal(4),
            captures = emptyList()
        )
        val newState = engine.executeMove(state, move)

        val movedToken = newState.players[0].tokens[0]
        assertTrue(movedToken.cell is Cell.Normal)
        assertEquals(4, (movedToken.cell as Cell.Normal).index)
    }

    @Test
    fun executeMove_capture_sendsTokenToBase() {
        val redTokens = listOf(
            Token(0, PlayerColor.RED, Cell.Normal(7)),
            Token(1, PlayerColor.RED, Cell.Base(PlayerColor.RED)),
            Token(2, PlayerColor.RED, Cell.Base(PlayerColor.RED)),
            Token(3, PlayerColor.RED, Cell.Base(PlayerColor.RED))
        )
        val greenTokens = listOf(
            Token(0, PlayerColor.GREEN, Cell.Normal(10)),
            Token(1, PlayerColor.GREEN, Cell.Base(PlayerColor.GREEN)),
            Token(2, PlayerColor.GREEN, Cell.Base(PlayerColor.GREEN)),
            Token(3, PlayerColor.GREEN, Cell.Base(PlayerColor.GREEN))
        )
        val state = GameState(
            players = listOf(
                Player(PlayerColor.RED, "Red", false, tokens = redTokens),
                Player(PlayerColor.GREEN, "Green", true, tokens = greenTokens)
            ),
            currentPlayerIndex = 0,
            dice = DiceResult(3),
            phase = GamePhase.WAITING_FOR_MOVE,
            winner = null
        )

        val capturedToken = greenTokens[0]
        val move = Move(
            token = redTokens[0],
            destination = Cell.Normal(10),
            captures = listOf(capturedToken)
        )
        val newState = engine.executeMove(state, move)

        // GREEN's token should be back at base
        val greenToken = newState.players[1].tokens[0]
        assertTrue("Captured token should be in base", greenToken.cell is Cell.Base)
    }

    @Test
    fun executeMove_allTokensHome_gameOver() {
        // RED has 3 tokens at home, 1 about to reach home
        val redTokens = listOf(
            Token(0, PlayerColor.RED, Cell.HomeStretch(PlayerColor.RED, 4)),
            Token(1, PlayerColor.RED, Cell.Home(PlayerColor.RED)),
            Token(2, PlayerColor.RED, Cell.Home(PlayerColor.RED)),
            Token(3, PlayerColor.RED, Cell.Home(PlayerColor.RED))
        )
        val greenTokens = (0..3).map { Token(it, PlayerColor.GREEN, Cell.Base(PlayerColor.GREEN)) }
        val state = GameState(
            players = listOf(
                Player(PlayerColor.RED, "Red", false, tokens = redTokens),
                Player(PlayerColor.GREEN, "Green", true, tokens = greenTokens)
            ),
            currentPlayerIndex = 0,
            dice = DiceResult(1),
            phase = GamePhase.WAITING_FOR_MOVE,
            winner = null
        )

        val move = Move(
            token = redTokens[0],
            destination = Cell.Home(PlayerColor.RED),
            captures = emptyList()
        )
        val newState = engine.executeMove(state, move)

        assertEquals(GamePhase.GAME_OVER, newState.phase)
        assertEquals(PlayerColor.RED, newState.winner)
    }

    @Test
    fun extraTurn_onRolling6() {
        val redTokens = listOf(
            Token(0, PlayerColor.RED, Cell.Normal(5)),
            Token(1, PlayerColor.RED, Cell.Base(PlayerColor.RED)),
            Token(2, PlayerColor.RED, Cell.Base(PlayerColor.RED)),
            Token(3, PlayerColor.RED, Cell.Base(PlayerColor.RED))
        )
        val greenTokens = (0..3).map { Token(it, PlayerColor.GREEN, Cell.Base(PlayerColor.GREEN)) }
        val state = GameState(
            players = listOf(
                Player(PlayerColor.RED, "Red", false, tokens = redTokens),
                Player(PlayerColor.GREEN, "Green", true, tokens = greenTokens)
            ),
            currentPlayerIndex = 0,
            dice = DiceResult(6),
            phase = GamePhase.WAITING_FOR_MOVE,
            winner = null,
            consecutiveSixes = 1 // This is the first 6
        )

        val move = Move(
            token = redTokens[0],
            destination = Cell.Normal(11),
            captures = emptyList()
        )
        val newState = engine.executeMove(state, move)

        // Should get extra turn (stay as RED)
        assertEquals(0, newState.currentPlayerIndex)
        assertEquals(GamePhase.WAITING_FOR_ROLL, newState.phase)
    }

    @Test
    fun skipTurn_advancesToNextPlayer() {
        val state = engine.createInitialState()
        val skipped = engine.skipTurn(state)
        assertEquals(1, skipped.currentPlayerIndex)
        assertEquals(GamePhase.WAITING_FOR_ROLL, skipped.phase)
    }

    @Test
    fun skipTurn_wrapsAround() {
        val state = engine.createInitialState().copy(currentPlayerIndex = 1)
        val skipped = engine.skipTurn(state)
        assertEquals(0, skipped.currentPlayerIndex)
    }

    @Test
    fun consecutiveSixes_forfeit() {
        // With max 3 consecutive sixes, the 3rd 6 should forfeit
        val config3 = GameConfig(
            playerConfigs = listOf(
                PlayerConfig(PlayerColor.RED, "Red", false),
                PlayerConfig(PlayerColor.GREEN, "Green", true)
            ),
            maxConsecutiveSixes = 3
        )
        val engine3 = GameEngine(layout, config3)
        val ruleSet = StandardRuleSet(config3)
        assertTrue(ruleSet.shouldForfeitForConsecutiveSixes(3, 3))
        assertFalse(ruleSet.shouldForfeitForConsecutiveSixes(2, 3))
    }
}
