package com.shaeed.ludo.model

/**
 * Standard 15x15 cross-shaped Ludo board layout.
 *
 * Grid coordinate system: (row, col) where (0,0) is top-left.
 *
 * Board structure:
 * - 4 base areas (6x6 corners): RED(top-left), GREEN(top-right), YELLOW(bottom-right), BLUE(bottom-left)
 * - 52-cell track around the cross
 * - 4 home stretches of 5 cells each leading to center
 * - Center home area at (7,7)
 *
 * Track numbering: 0-51, starting from RED's entry point going clockwise.
 * Each player's start position is where tokens enter after leaving base.
 */
class StandardBoardLayout : BoardLayout {

    override val trackSize: Int = 52
    override val homeStretchLength: Int = 5
    override val gridSize: Int = 15

    // Safe positions on the track (0-indexed): each player's start + star positions
    override val safePositions: Set<Int> = setOf(0, 8, 13, 21, 26, 34, 39, 47)

    // Track cell grid coordinates (52 cells, clockwise from RED start)
    // RED starts at index 0
    private val trackCoordinates: List<Pair<Int, Int>> = listOf(
        // RED's column going up: row 6→1, col 6
        Pair(6, 6),  // 0 - RED start
        Pair(5, 6),  // 1
        Pair(4, 6),  // 2
        Pair(3, 6),  // 3
        Pair(2, 6),  // 4
        Pair(1, 6),  // 5
        // Top-left to top-right across top arm: row 0, col 6→8
        Pair(0, 6),  // 6
        Pair(0, 7),  // 7
        Pair(0, 8),  // 8
        // GREEN's column going down: row 1→6, col 8
        Pair(1, 8),  // 9
        Pair(2, 8),  // 10
        Pair(3, 8),  // 11
        Pair(4, 8),  // 12
        Pair(5, 8),  // 13 - GREEN start
        // Right arm going right: row 6, col 9→14
        Pair(6, 9),  // 14
        Pair(6, 10), // 15
        Pair(6, 11), // 16
        Pair(6, 12), // 17
        Pair(6, 13), // 18
        // Top-right to bottom-right down right side: row 6→8, col 14
        Pair(6, 14), // 19
        Pair(7, 14), // 20
        Pair(8, 14), // 21
        // Right column going left along bottom of right arm: row 8, col 13→9
        Pair(8, 13), // 22
        Pair(8, 12), // 23
        Pair(8, 11), // 24
        Pair(8, 10), // 25
        Pair(8, 9),  // 26 - YELLOW start
        // YELLOW's column going down: row 9→14, col 8
        Pair(9, 8),  // 27
        Pair(10, 8), // 28
        Pair(11, 8), // 29
        Pair(12, 8), // 30
        Pair(13, 8), // 31
        // Bottom-right to bottom-left across bottom arm: row 14, col 8→6
        Pair(14, 8), // 32
        Pair(14, 7), // 33
        Pair(14, 6), // 34
        // BLUE's column going up: row 13→8, col 6
        Pair(13, 6), // 35
        Pair(12, 6), // 36
        Pair(11, 6), // 37
        Pair(10, 6), // 38
        Pair(9, 6),  // 39 - BLUE start
        // Left arm going left: row 8, col 5→0
        Pair(8, 5),  // 40
        Pair(8, 4),  // 41
        Pair(8, 3),  // 42
        Pair(8, 2),  // 43
        Pair(8, 1),  // 44
        // Bottom-left to top-left up left side: row 8→6, col 0
        Pair(8, 0),  // 45
        Pair(7, 0),  // 46
        Pair(6, 0),  // 47
        // Left column going right along top of left arm: row 6, col 1→5
        Pair(6, 1),  // 48
        Pair(6, 2),  // 49
        Pair(6, 3),  // 50
        Pair(6, 4),  // 51
    )

    // Home stretch coordinates per color (5 cells each, leading toward center)
    private val homeStretchCoordinates: Map<PlayerColor, List<Pair<Int, Int>>> = mapOf(
        PlayerColor.RED to listOf(
            Pair(7, 1), Pair(7, 2), Pair(7, 3), Pair(7, 4), Pair(7, 5)
        ),
        PlayerColor.GREEN to listOf(
            Pair(1, 7), Pair(2, 7), Pair(3, 7), Pair(4, 7), Pair(5, 7)
        ),
        PlayerColor.YELLOW to listOf(
            Pair(7, 13), Pair(7, 12), Pair(7, 11), Pair(7, 10), Pair(7, 9)
        ),
        PlayerColor.BLUE to listOf(
            Pair(13, 7), Pair(12, 7), Pair(11, 7), Pair(10, 7), Pair(9, 7)
        )
    )

    // Base token positions (4 tokens in each 6x6 corner area)
    private val baseCoordinates: Map<PlayerColor, List<Pair<Int, Int>>> = mapOf(
        PlayerColor.RED to listOf(Pair(1, 1), Pair(1, 4), Pair(4, 1), Pair(4, 4)),
        PlayerColor.GREEN to listOf(Pair(1, 10), Pair(1, 13), Pair(4, 10), Pair(4, 13)),
        PlayerColor.YELLOW to listOf(Pair(10, 10), Pair(10, 13), Pair(13, 10), Pair(13, 13)),
        PlayerColor.BLUE to listOf(Pair(10, 1), Pair(10, 4), Pair(13, 1), Pair(13, 4))
    )

    // Start position on track for each color
    private val startPositions: Map<PlayerColor, Int> = mapOf(
        PlayerColor.RED to 0,
        PlayerColor.GREEN to 13,
        PlayerColor.YELLOW to 26,
        PlayerColor.BLUE to 39
    )

    // The track index just before entering home stretch
    // (the cell the player passes through to enter their home stretch)
    private val homeStretchEntries: Map<PlayerColor, Int> = mapOf(
        PlayerColor.RED to 51,   // After cell 51, RED enters home stretch
        PlayerColor.GREEN to 12, // After cell 12, GREEN enters home stretch
        PlayerColor.YELLOW to 25,// After cell 25, YELLOW enters home stretch
        PlayerColor.BLUE to 38   // After cell 38, BLUE enters home stretch
    )

    // Precomputed full paths for each color: Base → track (full loop) → HomeStretch → Home
    private val fullPaths: Map<PlayerColor, List<Cell>> = PlayerColor.entries.associateWith { color ->
        buildList {
            add(Cell.Base(color))
            val start = startPositions[color]!!
            // Walk the track from this color's start position for 52 cells
            for (i in 0 until trackSize) {
                val trackIndex = (start + i) % trackSize
                add(Cell.Normal(trackIndex, trackIndex in safePositions))
            }
            // Home stretch cells
            for (i in 0 until homeStretchLength) {
                add(Cell.HomeStretch(color, i))
            }
            // Final home
            add(Cell.Home(color))
        }
    }

    override fun startPosition(color: PlayerColor): Int = startPositions[color]!!

    override fun homeStretchEntry(color: PlayerColor): Int = homeStretchEntries[color]!!

    override fun fullPath(color: PlayerColor): List<Cell> = fullPaths[color]!!

    override fun cellToGrid(cell: Cell): Pair<Int, Int> = when (cell) {
        is Cell.Normal -> trackCoordinates[cell.index]
        is Cell.HomeStretch -> homeStretchCoordinates[cell.color]!![cell.index]
        is Cell.Home -> Pair(7, 7) // Center of board
        is Cell.Base -> Pair(-1, -1) // Base positions handled separately per token
    }

    override fun basePositions(color: PlayerColor): List<Pair<Int, Int>> =
        baseCoordinates[color]!!

    override fun homePosition(color: PlayerColor): Pair<Int, Int> = Pair(7, 7)
}
