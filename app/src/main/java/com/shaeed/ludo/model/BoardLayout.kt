package com.shaeed.ludo.model

interface BoardLayout {
    val trackSize: Int
    val homeStretchLength: Int
    val gridSize: Int
    val safePositions: Set<Int>

    fun startPosition(color: PlayerColor): Int
    fun fullPath(color: PlayerColor): List<Cell>
    fun cellToGrid(cell: Cell): Pair<Int, Int>
    fun basePositions(color: PlayerColor): List<Pair<Int, Int>>
    fun homePosition(color: PlayerColor): Pair<Int, Int>
}
