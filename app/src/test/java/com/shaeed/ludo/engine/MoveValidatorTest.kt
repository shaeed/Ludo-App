package com.shaeed.ludo.engine

import com.shaeed.ludo.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MoveValidatorTest {

    private lateinit var layout: StandardBoardLayout
    private lateinit var config: GameConfig
    private lateinit var ruleSet: StandardRuleSet
    private lateinit var pathCalculator: PathCalculator
    private lateinit var validator: MoveValidator

    @Before
    fun setUp() {
        layout = StandardBoardLayout()
        config = GameConfig(
            playerConfigs = listOf(
                PlayerConfig(PlayerColor.RED, "Red", false),
                PlayerConfig(PlayerColor.GREEN, "Green", true)
            )
        )
        ruleSet = StandardRuleSet(config)
        pathCalculator = PathCalculator(layout)
        validator = MoveValidator(layout, ruleSet, pathCalculator)
    }

    @Test
    fun allTokensInBase_dice6_canEnterBoard() {
        val state = createState(
            redTokenCells = listOf(Cell.Base(PlayerColor.RED), Cell.Base(PlayerColor.RED), Cell.Base(PlayerColor.RED), Cell.Base(PlayerColor.RED)),
            diceValue = 6
        )
        val moves = validator.computeLegalMoves(state)
        assertTrue("Should have at least one move with dice 6", moves.isNotEmpty())
        // All base tokens should produce the same destination, so moves should be deduplicated to multiple tokens
        assertTrue(moves.all { it.token.cell is Cell.Base })
    }

    @Test
    fun allTokensInBase_diceNot6_noMoves() {
        val state = createState(
            redTokenCells = listOf(Cell.Base(PlayerColor.RED), Cell.Base(PlayerColor.RED), Cell.Base(PlayerColor.RED), Cell.Base(PlayerColor.RED)),
            diceValue = 3
        )
        val moves = validator.computeLegalMoves(state)
        assertTrue("Should have no moves without 6", moves.isEmpty())
    }

    @Test
    fun tokenOnBoard_canMoveForward() {
        val state = createState(
            redTokenCells = listOf(Cell.Normal(0), Cell.Base(PlayerColor.RED), Cell.Base(PlayerColor.RED), Cell.Base(PlayerColor.RED)),
            diceValue = 4
        )
        val moves = validator.computeLegalMoves(state)
        assertTrue("Should be able to move token on board", moves.isNotEmpty())
        val boardMove = moves.first { it.token.cell is Cell.Normal }
        assertTrue(boardMove.destination is Cell.Normal)
        assertEquals(4, (boardMove.destination as Cell.Normal).index)
    }

    @Test
    fun tokenAtHome_cannotMove() {
        val state = createState(
            redTokenCells = listOf(Cell.Home(PlayerColor.RED), Cell.Base(PlayerColor.RED), Cell.Base(PlayerColor.RED), Cell.Base(PlayerColor.RED)),
            diceValue = 6
        )
        val moves = validator.computeLegalMoves(state)
        // Home tokens can't move; only base tokens can enter with 6
        val homeMoves = moves.filter { it.token.cell is Cell.Home }
        assertTrue("Home tokens should have no moves", homeMoves.isEmpty())
    }

    @Test
    fun noDiceResult_noMoves() {
        val players = listOf(
            Player(PlayerColor.RED, "Red", false, tokens = (0..3).map { Token(it, PlayerColor.RED, Cell.Base(PlayerColor.RED)) }),
            Player(PlayerColor.GREEN, "Green", true, tokens = (0..3).map { Token(it, PlayerColor.GREEN, Cell.Base(PlayerColor.GREEN)) })
        )
        val state = GameState(players, 0, null, GamePhase.WAITING_FOR_MOVE, null)
        val moves = validator.computeLegalMoves(state)
        assertTrue(moves.isEmpty())
    }

    @Test
    fun capture_detectedCorrectly() {
        // RED token moving to a cell occupied by GREEN
        val state = createStateWithGreen(
            redTokenCells = listOf(Cell.Normal(10), Cell.Base(PlayerColor.RED), Cell.Base(PlayerColor.RED), Cell.Base(PlayerColor.RED)),
            greenTokenCells = listOf(Cell.Normal(13), Cell.Base(PlayerColor.GREEN), Cell.Base(PlayerColor.GREEN), Cell.Base(PlayerColor.GREEN)),
            diceValue = 3
        )
        val moves = validator.computeLegalMoves(state)
        val captureMove = moves.firstOrNull { it.captures.isNotEmpty() }
        // Normal(10) + 3 = Normal(13) where GREEN is
        // But Normal(13) is GREEN's start and a safe position, so no capture
        // Let's check the safe positions: 0, 8, 13, 21, 26, 34, 39, 47
        // 13 is safe, so no capture here
        assertNull("Should not capture on safe zone", captureMove)
    }

    @Test
    fun capture_onNonSafeCell() {
        // Place GREEN on non-safe cell 10
        val state = createStateWithGreen(
            redTokenCells = listOf(Cell.Normal(7), Cell.Base(PlayerColor.RED), Cell.Base(PlayerColor.RED), Cell.Base(PlayerColor.RED)),
            greenTokenCells = listOf(Cell.Normal(10), Cell.Base(PlayerColor.GREEN), Cell.Base(PlayerColor.GREEN), Cell.Base(PlayerColor.GREEN)),
            diceValue = 3
        )
        val moves = validator.computeLegalMoves(state)
        val captureMove = moves.firstOrNull { it.captures.isNotEmpty() }
        assertNotNull("Should capture on non-safe cell", captureMove)
    }

    private fun createState(redTokenCells: List<Cell>, diceValue: Int): GameState {
        val redTokens = redTokenCells.mapIndexed { i, cell -> Token(i, PlayerColor.RED, cell) }
        val greenTokens = (0..3).map { Token(it, PlayerColor.GREEN, Cell.Base(PlayerColor.GREEN)) }
        val players = listOf(
            Player(PlayerColor.RED, "Red", false, tokens = redTokens),
            Player(PlayerColor.GREEN, "Green", true, tokens = greenTokens)
        )
        return GameState(players, 0, DiceResult(diceValue), GamePhase.WAITING_FOR_MOVE, null)
    }

    private fun createStateWithGreen(redTokenCells: List<Cell>, greenTokenCells: List<Cell>, diceValue: Int): GameState {
        val redTokens = redTokenCells.mapIndexed { i, cell -> Token(i, PlayerColor.RED, cell) }
        val greenTokens = greenTokenCells.mapIndexed { i, cell -> Token(i, PlayerColor.GREEN, cell) }
        val players = listOf(
            Player(PlayerColor.RED, "Red", false, tokens = redTokens),
            Player(PlayerColor.GREEN, "Green", true, tokens = greenTokens)
        )
        return GameState(players, 0, DiceResult(diceValue), GamePhase.WAITING_FOR_MOVE, null)
    }
}
