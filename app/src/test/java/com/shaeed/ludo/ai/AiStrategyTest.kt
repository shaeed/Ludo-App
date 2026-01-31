package com.shaeed.ludo.ai

import com.shaeed.ludo.engine.Move
import com.shaeed.ludo.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AiStrategyTest {

    private lateinit var layout: StandardBoardLayout
    private lateinit var state: GameState
    private lateinit var legalMoves: List<Move>

    @Before
    fun setUp() {
        layout = StandardBoardLayout()

        val redTokens = listOf(
            Token(0, PlayerColor.RED, Cell.Normal(5)),
            Token(1, PlayerColor.RED, Cell.Base(PlayerColor.RED)),
            Token(2, PlayerColor.RED, Cell.Normal(20)),
            Token(3, PlayerColor.RED, Cell.Base(PlayerColor.RED))
        )
        val greenTokens = listOf(
            Token(0, PlayerColor.GREEN, Cell.Normal(22)),
            Token(1, PlayerColor.GREEN, Cell.Base(PlayerColor.GREEN)),
            Token(2, PlayerColor.GREEN, Cell.Base(PlayerColor.GREEN)),
            Token(3, PlayerColor.GREEN, Cell.Base(PlayerColor.GREEN))
        )

        state = GameState(
            players = listOf(
                Player(PlayerColor.RED, "Red", false, tokens = redTokens),
                Player(PlayerColor.GREEN, "Green", true, tokens = greenTokens)
            ),
            currentPlayerIndex = 0,
            dice = DiceResult(3),
            phase = GamePhase.WAITING_FOR_MOVE,
            winner = null
        )

        legalMoves = listOf(
            Move(redTokens[0], Cell.Normal(8), emptyList()),
            Move(redTokens[2], Cell.Normal(23), emptyList())
        )
    }

    @Test
    fun easyStrategy_returnsValidMove() = runBlocking {
        val strategy = EasyAiStrategy()
        val move = strategy.chooseMove(state, legalMoves, layout)
        assertTrue("Move should be from legal moves", move in legalMoves)
    }

    @Test
    fun mediumStrategy_returnsValidMove() = runBlocking {
        val strategy = MediumAiStrategy()
        val move = strategy.chooseMove(state, legalMoves, layout)
        assertTrue("Move should be from legal moves", move in legalMoves)
    }

    @Test
    fun hardStrategy_returnsValidMove() = runBlocking {
        val strategy = HardAiStrategy()
        val move = strategy.chooseMove(state, legalMoves, layout)
        assertTrue("Move should be from legal moves", move in legalMoves)
    }

    @Test
    fun mediumStrategy_prefersCapture() = runBlocking {
        val captureMove = Move(
            token = Token(0, PlayerColor.RED, Cell.Normal(5)),
            destination = Cell.Normal(8),
            captures = listOf(Token(0, PlayerColor.GREEN, Cell.Normal(8)))
        )
        val normalMove = Move(
            token = Token(2, PlayerColor.RED, Cell.Normal(20)),
            destination = Cell.Normal(23),
            captures = emptyList()
        )
        val moves = listOf(normalMove, captureMove)

        val strategy = MediumAiStrategy()
        val chosen = strategy.chooseMove(state, moves, layout)
        assertEquals("Should prefer capture", captureMove, chosen)
    }

    @Test
    fun mediumStrategy_prefersEnteringBoard() = runBlocking {
        val enterMove = Move(
            token = Token(1, PlayerColor.RED, Cell.Base(PlayerColor.RED)),
            destination = Cell.Normal(0),
            captures = emptyList()
        )
        val normalMove = Move(
            token = Token(2, PlayerColor.RED, Cell.Normal(20)),
            destination = Cell.Normal(23),
            captures = emptyList()
        )
        val moves = listOf(normalMove, enterMove)

        val strategy = MediumAiStrategy()
        val chosen = strategy.chooseMove(state, moves, layout)
        assertEquals("Should prefer entering board", enterMove, chosen)
    }

    @Test
    fun hardStrategy_prefersCapture() = runBlocking {
        val captureMove = Move(
            token = Token(0, PlayerColor.RED, Cell.Normal(5)),
            destination = Cell.Normal(8),
            captures = listOf(Token(0, PlayerColor.GREEN, Cell.Normal(8)))
        )
        val normalMove = Move(
            token = Token(2, PlayerColor.RED, Cell.Normal(20)),
            destination = Cell.Normal(23),
            captures = emptyList()
        )
        val moves = listOf(normalMove, captureMove)

        val strategy = HardAiStrategy()
        val chosen = strategy.chooseMove(state, moves, layout)
        assertEquals("Should prefer capture", captureMove, chosen)
    }

    @Test
    fun allStrategies_handleSingleMove() = runBlocking {
        val singleMove = listOf(legalMoves[0])

        assertEquals(singleMove[0], EasyAiStrategy().chooseMove(state, singleMove, layout))
        assertEquals(singleMove[0], MediumAiStrategy().chooseMove(state, singleMove, layout))
        assertEquals(singleMove[0], HardAiStrategy().chooseMove(state, singleMove, layout))
    }
}
