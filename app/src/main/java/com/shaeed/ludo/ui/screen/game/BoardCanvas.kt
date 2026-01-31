package com.shaeed.ludo.ui.screen.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.shaeed.ludo.engine.Move
import com.shaeed.ludo.model.*
import com.shaeed.ludo.ui.components.colorForPlayer
import com.shaeed.ludo.ui.components.lightColorForPlayer
import com.shaeed.ludo.ui.theme.*

@Composable
fun BoardCanvas(
    gameState: GameState,
    layout: BoardLayout,
    legalMoves: List<Move>,
    onCellTapped: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(legalMoves) {
                detectTapGestures { offset ->
                    val cellSize = size.width / layout.gridSize.toFloat()
                    val col = (offset.x / cellSize).toInt()
                    val row = (offset.y / cellSize).toInt()
                    onCellTapped(row, col)
                }
            }
    ) {
        val cellSize = size.width / layout.gridSize

        // Draw board background
        drawRect(color = BoardBackground, size = size)

        // Draw base areas
        drawBaseArea(PlayerColor.RED, 0, 0, cellSize)
        drawBaseArea(PlayerColor.GREEN, 0, 9, cellSize)
        drawBaseArea(PlayerColor.YELLOW, 9, 9, cellSize)
        drawBaseArea(PlayerColor.BLUE, 9, 0, cellSize)

        // Draw track cells
        for (i in 0 until layout.trackSize) {
            val cell = Cell.Normal(i, i in layout.safePositions)
            val (row, col) = layout.cellToGrid(cell)
            drawTrackCell(row, col, cellSize, cell.isSafe)
        }

        // Draw home stretch cells
        for (color in PlayerColor.entries) {
            for (i in 0 until layout.homeStretchLength) {
                val cell = Cell.HomeStretch(color, i)
                val (row, col) = layout.cellToGrid(cell)
                drawHomeStretchCell(row, col, cellSize, color)
            }
        }

        // Draw center home
        drawCenterHome(cellSize)

        // Highlight legal move destinations
        val moveDestinations = legalMoves.associate { move ->
            val (r, c) = if (move.destination is Cell.Base) {
                Pair(-1, -1)
            } else {
                layout.cellToGrid(move.destination)
            }
            Pair(r, c) to move
        }

        for ((pos, _) in moveDestinations) {
            if (pos.first >= 0 && pos.second >= 0) {
                drawLegalMoveHighlight(pos.first, pos.second, cellSize)
            }
        }

        // Draw tokens on base
        for (player in gameState.players) {
            val baseTokens = player.tokens.filter { it.cell is Cell.Base }
            val basePositions = layout.basePositions(player.color)
            for ((idx, token) in baseTokens.withIndex()) {
                if (idx < basePositions.size) {
                    val (row, col) = basePositions[idx]
                    val isMovable = legalMoves.any { it.token.id == token.id && it.token.color == token.color }
                    drawToken(row, col, cellSize, token.color, isMovable)
                }
            }
        }

        // Draw tokens on board (Normal, HomeStretch)
        for (player in gameState.players) {
            for (token in player.tokens) {
                if (token.cell is Cell.Normal || token.cell is Cell.HomeStretch) {
                    val (row, col) = layout.cellToGrid(token.cell)
                    val isMovable = legalMoves.any { it.token.id == token.id && it.token.color == token.color }
                    drawToken(row, col, cellSize, token.color, isMovable)
                }
            }
        }

        // Draw tokens at home (center) — stack them
        for (player in gameState.players) {
            val homeTokens = player.tokens.filter { it.cell is Cell.Home }
            if (homeTokens.isNotEmpty()) {
                val (baseRow, baseCol) = layout.homePosition(player.color)
                // Offset slightly per color to avoid overlap
                val colorOffset = when (player.color) {
                    PlayerColor.RED -> Pair(-0.3f, -0.3f)
                    PlayerColor.GREEN -> Pair(-0.3f, 0.3f)
                    PlayerColor.YELLOW -> Pair(0.3f, 0.3f)
                    PlayerColor.BLUE -> Pair(0.3f, -0.3f)
                }
                drawToken(
                    baseRow, baseCol, cellSize, player.color, false,
                    offsetX = colorOffset.second * cellSize,
                    offsetY = colorOffset.first * cellSize,
                    label = homeTokens.size.toString()
                )
            }
        }
    }
}

private fun DrawScope.drawBaseArea(color: PlayerColor, startRow: Int, startCol: Int, cellSize: Float) {
    val baseColor = lightColorForPlayer(color)
    val topLeft = Offset(startCol * cellSize, startRow * cellSize)
    val areaSize = Size(6 * cellSize, 6 * cellSize)

    drawRect(color = baseColor, topLeft = topLeft, size = areaSize)
    drawRect(color = Color.Black.copy(alpha = 0.2f), topLeft = topLeft, size = areaSize, style = Stroke(2f))

    // Draw base token slots
    val layout = StandardBoardLayout()
    val positions = layout.basePositions(color)
    for ((row, col) in positions) {
        val center = Offset((col + 0.5f) * cellSize, (row + 0.5f) * cellSize)
        drawCircle(color = Color.White, radius = cellSize * 0.35f, center = center)
        drawCircle(color = Color.Black.copy(alpha = 0.2f), radius = cellSize * 0.35f, center = center, style = Stroke(1.5f))
    }
}

private fun DrawScope.drawTrackCell(row: Int, col: Int, cellSize: Float, isSafe: Boolean) {
    val topLeft = Offset(col * cellSize, row * cellSize)
    val cellSizeObj = Size(cellSize, cellSize)

    drawRect(color = BoardTrack, topLeft = topLeft, size = cellSizeObj)
    drawRect(color = Color.Black.copy(alpha = 0.15f), topLeft = topLeft, size = cellSizeObj, style = Stroke(0.5f))

    if (isSafe) {
        val center = Offset((col + 0.5f) * cellSize, (row + 0.5f) * cellSize)
        // Draw a star/cross indicator for safe zone
        drawCircle(color = BoardSafeZone.copy(alpha = 0.3f), radius = cellSize * 0.3f, center = center)
    }
}

private fun DrawScope.drawHomeStretchCell(row: Int, col: Int, cellSize: Float, color: PlayerColor) {
    val topLeft = Offset(col * cellSize, row * cellSize)
    val cellSizeObj = Size(cellSize, cellSize)
    val stretchColor = colorForPlayer(color).copy(alpha = 0.4f)

    drawRect(color = stretchColor, topLeft = topLeft, size = cellSizeObj)
    drawRect(color = Color.Black.copy(alpha = 0.15f), topLeft = topLeft, size = cellSizeObj, style = Stroke(0.5f))
}

private fun DrawScope.drawCenterHome(cellSize: Float) {
    val center = Offset(7.5f * cellSize, 7.5f * cellSize)
    val radius = cellSize * 1.2f

    // Draw quadrants
    val colors = listOf(
        colorForPlayer(PlayerColor.RED),
        colorForPlayer(PlayerColor.GREEN),
        colorForPlayer(PlayerColor.YELLOW),
        colorForPlayer(PlayerColor.BLUE)
    )
    for (i in colors.indices) {
        drawArc(
            color = colors[i].copy(alpha = 0.6f),
            startAngle = 90f * i - 135f,
            sweepAngle = 90f,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )
    }
    drawCircle(color = Color.White, radius = radius * 0.3f, center = center)
    drawCircle(color = Color.Black.copy(alpha = 0.3f), radius = radius, center = center, style = Stroke(2f))
}

private fun DrawScope.drawLegalMoveHighlight(row: Int, col: Int, cellSize: Float) {
    val center = Offset((col + 0.5f) * cellSize, (row + 0.5f) * cellSize)
    drawCircle(
        color = Color.White.copy(alpha = 0.7f),
        radius = cellSize * 0.4f,
        center = center
    )
    drawCircle(
        color = Color.Green.copy(alpha = 0.5f),
        radius = cellSize * 0.35f,
        center = center,
        style = Stroke(3f)
    )
}

private fun DrawScope.drawToken(
    row: Int, col: Int, cellSize: Float, color: PlayerColor, isMovable: Boolean,
    offsetX: Float = 0f, offsetY: Float = 0f, label: String? = null
) {
    val center = Offset((col + 0.5f) * cellSize + offsetX, (row + 0.5f) * cellSize + offsetY)
    val radius = cellSize * 0.35f
    val tokenColor = colorForPlayer(color)

    if (isMovable) {
        drawCircle(
            color = Color.White.copy(alpha = 0.6f),
            radius = radius * 1.5f,
            center = center
        )
    }

    drawCircle(color = tokenColor, radius = radius, center = center)
    drawCircle(color = Color.Black.copy(alpha = 0.3f), radius = radius, center = center, style = Stroke(1.5f))
    drawCircle(color = Color.White.copy(alpha = 0.4f), radius = radius * 0.35f,
        center = Offset(center.x - radius * 0.15f, center.y - radius * 0.15f))
}
