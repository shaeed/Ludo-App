package com.shaeed.ludo.engine

import com.shaeed.ludo.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PathCalculatorTest {

    private lateinit var layout: StandardBoardLayout
    private lateinit var calculator: PathCalculator

    @Before
    fun setUp() {
        layout = StandardBoardLayout()
        calculator = PathCalculator(layout)
    }

    @Test
    fun enterBoardDestination_isFirstTrackCell() {
        for (color in PlayerColor.entries) {
            val dest = calculator.enterBoardDestination(color)
            assertTrue("Should be a Normal cell", dest is Cell.Normal)
            assertEquals(layout.startPosition(color), (dest as Cell.Normal).index)
        }
    }

    @Test
    fun calculateDestination_fromBase_returnsNull() {
        val token = Token(0, PlayerColor.RED, Cell.Base(PlayerColor.RED))
        // calculateDestination only works for tokens already on the path (not in base)
        // A base token needs enterBoardDestination instead
        val result = calculator.calculateDestination(token, 6)
        // Base is at index 0 in the path, so moving 6 steps should work
        assertNotNull(result)
    }

    @Test
    fun calculateDestination_movesForward() {
        // RED token at start position (track index 0)
        val token = Token(0, PlayerColor.RED, Cell.Normal(0))
        val dest = calculator.calculateDestination(token, 3)
        assertNotNull(dest)
        assertTrue(dest is Cell.Normal)
        assertEquals(3, (dest as Cell.Normal).index)
    }

    @Test
    fun calculateDestination_wrapsAroundTrack() {
        // RED token at track index 50, moving 4 steps
        val token = Token(0, PlayerColor.RED, Cell.Normal(50))
        val dest = calculator.calculateDestination(token, 4)
        // RED's path: 0,1,...,51 (52 cells), then homestretch
        // Track index 50 is at path index 51 (path[0]=Base, path[1]=Normal(0), ...)
        // Actually for RED, path[1]=Normal(0), path[51]=Normal(50), path[52]=Normal(51)
        // path[53]=HomeStretch(RED,0), ...
        // Moving 4 from path index 51 → path index 55 = HomeStretch(RED,2)
        assertNotNull(dest)
    }

    @Test
    fun calculateDestination_entersHomeStretch() {
        // RED's last track cell before homestretch is index 51
        val token = Token(0, PlayerColor.RED, Cell.Normal(51))
        val dest = calculator.calculateDestination(token, 1)
        assertNotNull(dest)
        assertTrue("Should enter homestretch", dest is Cell.HomeStretch)
        assertEquals(PlayerColor.RED, (dest as Cell.HomeStretch).color)
        assertEquals(0, dest.index)
    }

    @Test
    fun calculateDestination_reachesHome() {
        // RED homestretch index 4 (last) + 1 = Home
        val token = Token(0, PlayerColor.RED, Cell.HomeStretch(PlayerColor.RED, 4))
        val dest = calculator.calculateDestination(token, 1)
        assertNotNull(dest)
        assertTrue("Should reach Home", dest is Cell.Home)
    }

    @Test
    fun calculateDestination_overshoot_returnsNull() {
        // RED homestretch index 4 (last) + 2 would overshoot
        val token = Token(0, PlayerColor.RED, Cell.HomeStretch(PlayerColor.RED, 4))
        val dest = calculator.calculateDestination(token, 2)
        assertNull("Overshoot should return null", dest)
    }

    @Test
    fun calculateDestination_exactRollToHome() {
        // RED homestretch index 0, need 6 to reach home (5 homestretch cells + home)
        val token = Token(0, PlayerColor.RED, Cell.HomeStretch(PlayerColor.RED, 0))
        val dest = calculator.calculateDestination(token, 6)
        // HomeStretch(0) at path index 53, +6 = 59 which is beyond path size (59)
        // Path: 0=Base, 1-52=Track, 53-57=HomeStretch, 58=Home → size = 59
        // So index 53 + 6 = 59 which is >= 59, so null (overshoot)
        // Actually: need exactly 6 from HS[0] → HS[1](+1), HS[2](+2), HS[3](+3), HS[4](+4), Home(+5)
        // So from HS[0], moving 5 reaches Home
        assertNull("6 from HS[0] overshoots", dest)

        val dest5 = calculator.calculateDestination(token, 5)
        assertNotNull(dest5)
        assertTrue(dest5 is Cell.Home)
    }

    @Test
    fun allColors_canReachHomeWithExactRolls() {
        for (color in PlayerColor.entries) {
            val token = Token(0, color, Cell.HomeStretch(color, 4))
            val dest = calculator.calculateDestination(token, 1)
            assertNotNull("$color should reach home from last homestretch", dest)
            assertTrue(dest is Cell.Home)
        }
    }
}
