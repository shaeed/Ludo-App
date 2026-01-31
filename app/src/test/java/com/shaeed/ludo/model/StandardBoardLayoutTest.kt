package com.shaeed.ludo.model

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class StandardBoardLayoutTest {

    private lateinit var layout: StandardBoardLayout

    @Before
    fun setUp() {
        layout = StandardBoardLayout()
    }

    @Test
    fun trackSize_is52() {
        assertEquals(52, layout.trackSize)
    }

    @Test
    fun homeStretchLength_is5() {
        assertEquals(5, layout.homeStretchLength)
    }

    @Test
    fun gridSize_is15() {
        assertEquals(15, layout.gridSize)
    }

    @Test
    fun safePositions_has8Entries() {
        assertEquals(8, layout.safePositions.size)
    }

    @Test
    fun startPositions_areCorrect() {
        assertEquals(0, layout.startPosition(PlayerColor.RED))
        assertEquals(13, layout.startPosition(PlayerColor.GREEN))
        assertEquals(26, layout.startPosition(PlayerColor.YELLOW))
        assertEquals(39, layout.startPosition(PlayerColor.BLUE))
    }

    @Test
    fun homeStretchEntries_areCorrect() {
        assertEquals(51, layout.homeStretchEntry(PlayerColor.RED))
        assertEquals(12, layout.homeStretchEntry(PlayerColor.GREEN))
        assertEquals(25, layout.homeStretchEntry(PlayerColor.YELLOW))
        assertEquals(38, layout.homeStretchEntry(PlayerColor.BLUE))
    }

    @Test
    fun fullPath_startsWithBase_endsWithHome() {
        for (color in PlayerColor.entries) {
            val path = layout.fullPath(color)
            assertTrue("Path should start with Base", path.first() is Cell.Base)
            assertTrue("Path should end with Home", path.last() is Cell.Home)
        }
    }

    @Test
    fun fullPath_hasCorrectLength() {
        // Base(1) + Track(52) + HomeStretch(5) + Home(1) = 59
        for (color in PlayerColor.entries) {
            val path = layout.fullPath(color)
            assertEquals(59, path.size)
        }
    }

    @Test
    fun fullPath_containsAllTrackCells() {
        val path = layout.fullPath(PlayerColor.RED)
        val trackCells = path.filterIsInstance<Cell.Normal>()
        assertEquals(52, trackCells.size)
        // Should cover all 52 indices
        val indices = trackCells.map { it.index }.toSet()
        assertEquals((0 until 52).toSet(), indices)
    }

    @Test
    fun fullPath_trackStartsAtPlayerStart() {
        for (color in PlayerColor.entries) {
            val path = layout.fullPath(color)
            val firstTrack = path[1] as Cell.Normal
            assertEquals(layout.startPosition(color), firstTrack.index)
        }
    }

    @Test
    fun fullPath_homeStretchIsCorrectColor() {
        for (color in PlayerColor.entries) {
            val path = layout.fullPath(color)
            val homeStretchCells = path.filterIsInstance<Cell.HomeStretch>()
            assertEquals(5, homeStretchCells.size)
            homeStretchCells.forEach { assertEquals(color, it.color) }
        }
    }

    @Test
    fun basePositions_has4PerColor() {
        for (color in PlayerColor.entries) {
            assertEquals(4, layout.basePositions(color).size)
        }
    }

    @Test
    fun basePositions_areWithinGrid() {
        for (color in PlayerColor.entries) {
            for ((row, col) in layout.basePositions(color)) {
                assertTrue("Row $row should be in grid", row in 0 until layout.gridSize)
                assertTrue("Col $col should be in grid", col in 0 until layout.gridSize)
            }
        }
    }

    @Test
    fun cellToGrid_trackCells_areWithinGrid() {
        for (i in 0 until layout.trackSize) {
            val cell = Cell.Normal(i)
            val (row, col) = layout.cellToGrid(cell)
            assertTrue("Track cell $i row=$row should be in grid", row in 0 until layout.gridSize)
            assertTrue("Track cell $i col=$col should be in grid", col in 0 until layout.gridSize)
        }
    }

    @Test
    fun cellToGrid_homeStretchCells_areWithinGrid() {
        for (color in PlayerColor.entries) {
            for (i in 0 until layout.homeStretchLength) {
                val cell = Cell.HomeStretch(color, i)
                val (row, col) = layout.cellToGrid(cell)
                assertTrue("HomeStretch row=$row should be in grid", row in 0 until layout.gridSize)
                assertTrue("HomeStretch col=$col should be in grid", col in 0 until layout.gridSize)
            }
        }
    }

    @Test
    fun cellToGrid_homeCell_isCenter() {
        for (color in PlayerColor.entries) {
            val (row, col) = layout.cellToGrid(Cell.Home(color))
            assertEquals(7, row)
            assertEquals(7, col)
        }
    }

    @Test
    fun trackCells_haveUniqueCoordinates() {
        val coords = mutableSetOf<Pair<Int, Int>>()
        for (i in 0 until layout.trackSize) {
            val coord = layout.cellToGrid(Cell.Normal(i))
            assertTrue("Track cell $i has duplicate coordinate $coord", coords.add(coord))
        }
    }

    @Test
    fun startPositions_areSafe() {
        for (color in PlayerColor.entries) {
            assertTrue(
                "${color.name} start should be safe",
                layout.startPosition(color) in layout.safePositions
            )
        }
    }
}
