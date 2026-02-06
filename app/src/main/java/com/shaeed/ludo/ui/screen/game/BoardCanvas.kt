package com.shaeed.ludo.ui.screen.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.shaeed.ludo.engine.Move
import com.shaeed.ludo.model.*
import com.shaeed.ludo.ui.components.colorForPlayer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val startColorMap = mapOf(
    0 to PlayerColor.RED,
    13 to PlayerColor.GREEN,
    26 to PlayerColor.YELLOW,
    39 to PlayerColor.BLUE
)

private data class BoardTokenInfo(val color: PlayerColor, val movable: Boolean)

@Composable
fun BoardCanvas(
    gameState: GameState,
    layout: BoardLayout,
    legalMoves: List<Move>,
    onCellTapped: (Int, Int) -> Unit,
    tokenAnimation: TokenAnimation? = null,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(legalMoves) {
                detectTapGestures { offset ->
                    val cs = size.width / layout.gridSize.toFloat()
                    val col = (offset.x / cs).toInt()
                    val row = (offset.y / cs).toInt()
                    onCellTapped(row, col)
                }
            }
    ) {
        val cs = size.width / layout.gridSize

        // 1. White background
        drawRect(color = Color.White, size = size)

        // 2. Base areas
        drawBaseArea(PlayerColor.RED, 0, 0, cs)
        drawBaseArea(PlayerColor.GREEN, 0, 9, cs)
        drawBaseArea(PlayerColor.YELLOW, 9, 9, cs)
        drawBaseArea(PlayerColor.BLUE, 9, 0, cs)

        // 3. Center triangles (behind track cells)
        drawCenterTriangles(cs)

        // 4. Track cells
        for (i in 0 until layout.trackSize) {
            val cell = Cell.Normal(i, i in layout.safePositions)
            val (row, col) = layout.cellToGrid(cell)
            val startColor = startColorMap[i]
            drawTrackCell(row, col, cs, startColor)
        }

        // 5. Home stretch cells
        for (color in PlayerColor.entries) {
            for (i in 0 until layout.homeStretchLength) {
                val cell = Cell.HomeStretch(color, i)
                val (row, col) = layout.cellToGrid(cell)
                drawHomeStretchCell(row, col, cs, color)
            }
        }

        // 6. Stars on non-start safe positions
        for (safeIdx in layout.safePositions) {
            val (row, col) = layout.cellToGrid(Cell.Normal(safeIdx, true))
            drawStar((col + 0.5f) * cs, (row + 0.5f) * cs, cs * 0.28f, Color(0xFF424242))
        }

        // 7. Arm-tip arrows
        drawArrow(0, 7, cs, colorForPlayer(PlayerColor.GREEN), "down")
        drawArrow(7, 0, cs, colorForPlayer(PlayerColor.RED), "right")
        drawArrow(7, 14, cs, colorForPlayer(PlayerColor.YELLOW), "left")
        drawArrow(14, 7, cs, colorForPlayer(PlayerColor.BLUE), "up")

        // 8. Legal move highlights
        for (move in legalMoves) {
            if (move.destination !is Cell.Base) {
                val (r, c) = layout.cellToGrid(move.destination)
                drawLegalMoveHighlight(r, c, cs)
            }
        }

        // Helper to check if a token is being animated
        fun isAnimatingToken(tokenId: Int, tokenColor: PlayerColor): Boolean {
            return tokenAnimation != null &&
                tokenAnimation.tokenId == tokenId &&
                tokenAnimation.tokenColor == tokenColor
        }

        // 9. Tokens in base (skip animating token)
        for (player in gameState.players) {
            val baseTokens = player.tokens.filter { it.cell is Cell.Base }
            val basePositions = layout.basePositions(player.color)
            for ((idx, token) in baseTokens.withIndex()) {
                if (idx < basePositions.size && !isAnimatingToken(token.id, token.color)) {
                    val (row, col) = basePositions[idx]
                    val movable = legalMoves.any { it.token.id == token.id && it.token.color == token.color }
                    drawToken(row, col, cs, token.color, movable)
                }
            }
        }

        // 10. Tokens on board (grouped by cell for overlap visibility, skip animating token)
        val boardTokensByCell = mutableMapOf<Pair<Int, Int>, MutableList<BoardTokenInfo>>()
        for (player in gameState.players) {
            for (token in player.tokens) {
                if ((token.cell is Cell.Normal || token.cell is Cell.HomeStretch) &&
                    !isAnimatingToken(token.id, token.color)) {
                    val (row, col) = layout.cellToGrid(token.cell)
                    val movable = legalMoves.any { it.token.id == token.id && it.token.color == token.color }
                    boardTokensByCell.getOrPut(Pair(row, col)) { mutableListOf() }
                        .add(BoardTokenInfo(token.color, movable))
                }
            }
        }
        for ((pos, tokens) in boardTokensByCell) {
            val (row, col) = pos
            drawStackedTokens(row, col, cs, tokens)
        }

        // 11. Tokens at home (center)
        for (player in gameState.players) {
            val homeTokens = player.tokens.filter { it.cell is Cell.Home }
            if (homeTokens.isNotEmpty()) {
                val off = when (player.color) {
                    PlayerColor.RED -> Pair(-0.3f, -0.3f)
                    PlayerColor.GREEN -> Pair(-0.3f, 0.3f)
                    PlayerColor.YELLOW -> Pair(0.3f, 0.3f)
                    PlayerColor.BLUE -> Pair(0.3f, -0.3f)
                }
                drawToken(7, 7, cs, player.color, false, off.second * cs, off.first * cs)
            }
        }

        // 12. Animating token (drawn on top)
        if (tokenAnimation != null) {
            val animCell = tokenAnimation.currentCell
            when (animCell) {
                is Cell.Normal, is Cell.HomeStretch -> {
                    val (row, col) = layout.cellToGrid(animCell)
                    drawToken(row, col, cs, tokenAnimation.tokenColor, false)
                }
                is Cell.Home -> {
                    val off = when (tokenAnimation.tokenColor) {
                        PlayerColor.RED -> Pair(-0.3f, -0.3f)
                        PlayerColor.GREEN -> Pair(-0.3f, 0.3f)
                        PlayerColor.YELLOW -> Pair(0.3f, 0.3f)
                        PlayerColor.BLUE -> Pair(0.3f, -0.3f)
                    }
                    drawToken(7, 7, cs, tokenAnimation.tokenColor, false, off.second * cs, off.first * cs)
                }
                is Cell.Base -> {
                    val basePositions = layout.basePositions(animCell.color)
                    val player = gameState.players.first { it.color == animCell.color }
                    val numInBase = player.tokens.count { it.cell is Cell.Base }
                    val slotIndex = minOf(numInBase, basePositions.size - 1)
                    val (row, col) = basePositions[slotIndex]
                    drawToken(row, col, cs, tokenAnimation.tokenColor, false)
                }
            }
        }
    }
}

// ── Base area: solid color bg → white rounded rect → 4 colored circles ──

private fun DrawScope.drawBaseArea(color: PlayerColor, startRow: Int, startCol: Int, cs: Float) {
    val baseColor = colorForPlayer(color)
    val topLeft = Offset(startCol * cs, startRow * cs)
    val areaSize = Size(6 * cs, 6 * cs)

    // Solid color fill
    drawRect(color = baseColor, topLeft = topLeft, size = areaSize)

    // White rounded rectangle
    val inset = 0.65f * cs
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(topLeft.x + inset, topLeft.y + inset),
        size = Size(areaSize.width - 2 * inset, areaSize.height - 2 * inset),
        cornerRadius = CornerRadius(0.6f * cs)
    )

    // 4 token slot circles (relative positions within the 6×6 area)
    val slots = listOf(
        Pair(1.8f, 1.8f), Pair(1.8f, 4.2f),
        Pair(4.2f, 1.8f), Pair(4.2f, 4.2f)
    )
    for ((relRow, relCol) in slots) {
        val cx = (startCol + relCol) * cs
        val cy = (startRow + relRow) * cs
        drawCircle(color = baseColor, radius = 0.78f * cs, center = Offset(cx, cy))
    }
}

// ── Track cell: white (or player-colored if start) with thin border ──

private fun DrawScope.drawTrackCell(row: Int, col: Int, cs: Float, startColor: PlayerColor?) {
    val topLeft = Offset(col * cs, row * cs)
    val cellSize = Size(cs, cs)
    val fill = if (startColor != null) colorForPlayer(startColor) else Color.White

    drawRect(color = fill, topLeft = topLeft, size = cellSize)
    drawRect(color = Color(0x30000000), topLeft = topLeft, size = cellSize, style = Stroke(1f))
}

// ── Home stretch cell: solid player color with border ──

private fun DrawScope.drawHomeStretchCell(row: Int, col: Int, cs: Float, color: PlayerColor) {
    val topLeft = Offset(col * cs, row * cs)
    val cellSize = Size(cs, cs)

    drawRect(color = colorForPlayer(color), topLeft = topLeft, size = cellSize)
    drawRect(color = Color(0x30000000), topLeft = topLeft, size = cellSize, style = Stroke(1f))
}

// ── Center home: 4 colored triangles meeting at center point ──

private fun DrawScope.drawCenterTriangles(cs: Float) {
    val cx = 7.5f * cs
    val cy = 7.5f * cs
    val l = 6f * cs
    val t = 6f * cs
    val r = 9f * cs
    val b = 9f * cs

    // Top triangle → GREEN (home stretch enters from top)
    drawPath(Path().apply { moveTo(l, t); lineTo(r, t); lineTo(cx, cy); close() },
        colorForPlayer(PlayerColor.GREEN))
    // Right triangle → YELLOW (home stretch enters from right)
    drawPath(Path().apply { moveTo(r, t); lineTo(r, b); lineTo(cx, cy); close() },
        colorForPlayer(PlayerColor.YELLOW))
    // Bottom triangle → BLUE (home stretch enters from bottom)
    drawPath(Path().apply { moveTo(r, b); lineTo(l, b); lineTo(cx, cy); close() },
        colorForPlayer(PlayerColor.BLUE))
    // Left triangle → RED (home stretch enters from left)
    drawPath(Path().apply { moveTo(l, b); lineTo(l, t); lineTo(cx, cy); close() },
        colorForPlayer(PlayerColor.RED))

    // Thin dividers between triangles
    val divColor = Color(0x40000000)
    drawLine(divColor, Offset(l, t), Offset(cx, cy), strokeWidth = 1.5f)
    drawLine(divColor, Offset(r, t), Offset(cx, cy), strokeWidth = 1.5f)
    drawLine(divColor, Offset(r, b), Offset(cx, cy), strokeWidth = 1.5f)
    drawLine(divColor, Offset(l, b), Offset(cx, cy), strokeWidth = 1.5f)

    // Border around center square
    drawRect(Color(0x30000000), Offset(l, t), Size(r - l, b - t), style = Stroke(1.5f))
}

// ── 5-pointed star outline (safe-zone marker) ──

private fun DrawScope.drawStar(cx: Float, cy: Float, outerR: Float, color: Color) {
    val innerR = outerR * 0.4f
    val path = Path()
    val step = PI.toFloat() / 5f
    val start = -PI.toFloat() / 2f

    for (i in 0 until 10) {
        val rad = if (i % 2 == 0) outerR else innerR
        val angle = start + i * step
        val x = cx + rad * cos(angle)
        val y = cy + rad * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color, style = Stroke(width = 1.5f))
}

// ── Chevron arrow at arm tips ──

private fun DrawScope.drawArrow(row: Int, col: Int, cs: Float, color: Color, dir: String) {
    val cx = (col + 0.5f) * cs
    val cy = (row + 0.5f) * cs
    val s = cs * 0.25f
    val path = Path()
    when (dir) {
        "down"  -> { path.moveTo(cx - s, cy - s * 0.5f); path.lineTo(cx, cy + s * 0.5f); path.lineTo(cx + s, cy - s * 0.5f) }
        "up"    -> { path.moveTo(cx - s, cy + s * 0.5f); path.lineTo(cx, cy - s * 0.5f); path.lineTo(cx + s, cy + s * 0.5f) }
        "right" -> { path.moveTo(cx - s * 0.5f, cy - s); path.lineTo(cx + s * 0.5f, cy); path.lineTo(cx - s * 0.5f, cy + s) }
        "left"  -> { path.moveTo(cx + s * 0.5f, cy - s); path.lineTo(cx - s * 0.5f, cy); path.lineTo(cx + s * 0.5f, cy + s) }
    }
    drawPath(path, color, style = Stroke(width = 2.5f))
}

// ── Legal-move highlight ring ──

private fun DrawScope.drawLegalMoveHighlight(row: Int, col: Int, cs: Float) {
    val center = Offset((col + 0.5f) * cs, (row + 0.5f) * cs)
    drawCircle(Color.White.copy(alpha = 0.7f), cs * 0.4f, center)
    drawCircle(Color(0xFF4CAF50).copy(alpha = 0.6f), cs * 0.35f, center, style = Stroke(3f))
}

// ── Stacked tokens (overlap handling) ──

private fun DrawScope.drawStackedTokens(
    row: Int, col: Int, cs: Float, tokens: List<BoardTokenInfo>
) {
    val offsets = when (tokens.size) {
        1 -> listOf(Pair(0f, 0f))
        2 -> listOf(Pair(-0.18f, 0f), Pair(0.18f, 0f))
        3 -> listOf(Pair(0f, -0.16f), Pair(-0.16f, 0.12f), Pair(0.16f, 0.12f))
        else -> listOf(
            Pair(-0.16f, -0.16f), Pair(0.16f, -0.16f),
            Pair(-0.16f, 0.16f), Pair(0.16f, 0.16f)
        )
    }
    val scale = when {
        tokens.size == 1 -> 1f
        tokens.size <= 3 -> 0.7f
        else -> 0.6f
    }
    for ((idx, token) in tokens.withIndex()) {
        if (idx >= offsets.size) break
        val (ox, oy) = offsets[idx]
        drawToken(row, col, cs, token.color, token.movable, ox * cs, oy * cs, scale)
    }
}

// ── Token piece (3D cone-shaped Ludo token) ──

private fun DrawScope.drawToken(
    row: Int, col: Int, cs: Float, color: PlayerColor, isMovable: Boolean,
    offsetX: Float = 0f, offsetY: Float = 0f, scale: Float = 1f
) {
    val center = Offset((col + 0.5f) * cs + offsetX, (row + 0.5f) * cs + offsetY)
    val radius = cs * 0.35f * scale
    val tokenColor = colorForPlayer(color)

    // Color variants for 3D shading
    val darkColor = Color(
        red = tokenColor.red * 0.55f,
        green = tokenColor.green * 0.55f,
        blue = tokenColor.blue * 0.55f
    )
    val lightColor = Color(
        red = minOf(1f, tokenColor.red + (1f - tokenColor.red) * 0.45f),
        green = minOf(1f, tokenColor.green + (1f - tokenColor.green) * 0.45f),
        blue = minOf(1f, tokenColor.blue + (1f - tokenColor.blue) * 0.45f)
    )

    // Movable glow
    if (isMovable) {
        drawCircle(Color.White.copy(alpha = 0.7f), radius * 1.6f, center)
        drawCircle(Color(0xFF4CAF50).copy(alpha = 0.5f), radius * 1.4f, center, style = Stroke(2.5f * scale))
    }

    // Drop shadow
    drawCircle(
        Color.Black.copy(alpha = 0.22f), radius,
        Offset(center.x + 1.5f * scale, center.y + 2f * scale)
    )

    // Base ring (dark edge — the flat base of the cone)
    drawCircle(darkColor, radius, center)

    // Cone body (main color, inset from base)
    drawCircle(tokenColor, radius * 0.85f, center)

    // Dome highlight (lighter center — convex surface catching light)
    drawCircle(lightColor.copy(alpha = 0.6f), radius * 0.55f, center)

    // Top knob (small sphere at the peak)
    drawCircle(tokenColor, radius * 0.3f, center)
    drawCircle(darkColor.copy(alpha = 0.5f), radius * 0.3f, center, style = Stroke(1f * scale))

    // Outer border
    drawCircle(Color.Black.copy(alpha = 0.3f), radius, center, style = Stroke(1.5f * scale))

    // Specular highlights (glossy plastic reflection)
    drawCircle(
        Color.White.copy(alpha = 0.55f), radius * 0.14f,
        Offset(center.x - radius * 0.25f, center.y - radius * 0.3f)
    )
    drawCircle(
        Color.White.copy(alpha = 0.2f), radius * 0.22f,
        Offset(center.x - radius * 0.18f, center.y - radius * 0.18f)
    )
}
