package com.shaeed.ludo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shaeed.ludo.model.PlayerColor
import com.shaeed.ludo.model.TokenStyle
import com.shaeed.ludo.model.TokenStyleHolder
import com.shaeed.ludo.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TokenPiece(
    color: PlayerColor,
    isSelected: Boolean = false,
    size: Dp = 24.dp,
    tokenStyle: TokenStyle = TokenStyleHolder.current,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tokenGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val tokenColor = colorForPlayer(color)
    val darkColor = darkenColor(tokenColor)
    val lightColor = lightenColor(tokenColor)

    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val radius = this.size.minDimension / 2f * 0.8f

        // Selection glow
        if (isSelected) {
            drawCircle(
                color = Color.White.copy(alpha = glowAlpha),
                radius = radius * 1.4f,
                center = center
            )
        }

        when (tokenStyle) {
            TokenStyle.CLASSIC_CONE -> drawConeToken(center, radius, tokenColor, darkColor, lightColor)
            TokenStyle.FLAT_DISC -> drawFlatDiscToken(center, radius, tokenColor, darkColor)
            TokenStyle.STAR -> drawStarToken(center, radius, tokenColor, darkColor)
            TokenStyle.RING -> drawRingToken(center, radius, tokenColor, darkColor)
            TokenStyle.PAWN -> drawPawnToken(center, radius, tokenColor, darkColor, lightColor)
        }
    }
}

// ── Classic cone (original) ──

internal fun DrawScope.drawConeToken(
    center: Offset, radius: Float,
    tokenColor: Color, darkColor: Color, lightColor: Color
) {
    // Drop shadow
    drawCircle(Color.Black.copy(alpha = 0.22f), radius, Offset(center.x + 1.5f, center.y + 2f))
    // Base ring
    drawCircle(darkColor, radius, center)
    // Cone body
    drawCircle(tokenColor, radius * 0.85f, center)
    // Dome highlight
    drawCircle(lightColor.copy(alpha = 0.6f), radius * 0.55f, center)
    // Top knob
    drawCircle(tokenColor, radius * 0.3f, center)
    drawCircle(darkColor.copy(alpha = 0.5f), radius * 0.3f, center, style = Stroke(width = 1.5f))
    // Outer border
    drawCircle(Color.Black.copy(alpha = 0.3f), radius, center, style = Stroke(width = 2f))
    // Specular highlights
    drawCircle(Color.White.copy(alpha = 0.55f), radius * 0.14f, Offset(center.x - radius * 0.25f, center.y - radius * 0.3f))
    drawCircle(Color.White.copy(alpha = 0.2f), radius * 0.22f, Offset(center.x - radius * 0.18f, center.y - radius * 0.18f))
}

// ── Flat disc ──

internal fun DrawScope.drawFlatDiscToken(
    center: Offset, radius: Float,
    tokenColor: Color, darkColor: Color
) {
    // Drop shadow
    drawCircle(Color.Black.copy(alpha = 0.22f), radius, Offset(center.x + 1.5f, center.y + 2f))
    // Main filled circle
    drawCircle(tokenColor, radius, center)
    // Inner ring
    drawCircle(darkColor.copy(alpha = 0.4f), radius * 0.65f, center, style = Stroke(width = radius * 0.12f))
    // Outer border
    drawCircle(Color.Black.copy(alpha = 0.3f), radius, center, style = Stroke(width = 2f))
    // Specular dot
    drawCircle(Color.White.copy(alpha = 0.5f), radius * 0.12f, Offset(center.x - radius * 0.25f, center.y - radius * 0.25f))
}

// ── Star ──

internal fun DrawScope.drawStarToken(
    center: Offset, radius: Float,
    tokenColor: Color, darkColor: Color
) {
    // Drop shadow
    drawCircle(Color.Black.copy(alpha = 0.22f), radius, Offset(center.x + 1.5f, center.y + 2f))

    val starPath = createStarPath(center, radius * 0.95f, radius * 0.42f)
    // Filled star
    drawPath(starPath, tokenColor)
    // Star outline
    drawPath(starPath, darkColor, style = Stroke(width = 1.5f))
    // Specular highlight
    drawCircle(Color.White.copy(alpha = 0.45f), radius * 0.15f, Offset(center.x - radius * 0.15f, center.y - radius * 0.2f))
}

// ── Ring / Donut ──

internal fun DrawScope.drawRingToken(
    center: Offset, radius: Float,
    tokenColor: Color, darkColor: Color
) {
    // Drop shadow
    drawCircle(Color.Black.copy(alpha = 0.22f), radius, Offset(center.x + 1.5f, center.y + 2f))
    // Thick colored ring
    val ringRadius = radius * 0.72f
    val ringWidth = radius * 0.45f
    drawCircle(tokenColor, ringRadius, center, style = Stroke(width = ringWidth))
    // Outer border
    drawCircle(Color.Black.copy(alpha = 0.3f), ringRadius + ringWidth / 2f, center, style = Stroke(width = 1.5f))
    // Inner border
    drawCircle(Color.Black.copy(alpha = 0.2f), ringRadius - ringWidth / 2f, center, style = Stroke(width = 1f))
    // Highlight arc on top-left of ring
    drawCircle(Color.White.copy(alpha = 0.35f), radius * 0.1f, Offset(center.x - radius * 0.4f, center.y - radius * 0.4f))
}

// ── Pawn ──

internal fun DrawScope.drawPawnToken(
    center: Offset, radius: Float,
    tokenColor: Color, darkColor: Color, lightColor: Color
) {
    // Drop shadow
    drawCircle(Color.Black.copy(alpha = 0.22f), radius, Offset(center.x + 1.5f, center.y + 2f))

    // Base (oval at bottom)
    val baseY = center.y + radius * 0.45f
    val baseRx = radius * 0.75f
    val baseRy = radius * 0.28f
    drawOval(darkColor, Offset(center.x - baseRx, baseY - baseRy), androidx.compose.ui.geometry.Size(baseRx * 2, baseRy * 2))
    drawOval(tokenColor, Offset(center.x - baseRx * 0.88f, baseY - baseRy * 0.88f), androidx.compose.ui.geometry.Size(baseRx * 1.76f, baseRy * 1.76f))

    // Neck (narrow body connecting head to base)
    val neckPath = Path().apply {
        moveTo(center.x - radius * 0.2f, baseY - baseRy * 0.3f)
        lineTo(center.x - radius * 0.28f, center.y - radius * 0.15f)
        lineTo(center.x + radius * 0.28f, center.y - radius * 0.15f)
        lineTo(center.x + radius * 0.2f, baseY - baseRy * 0.3f)
        close()
    }
    drawPath(neckPath, tokenColor)
    drawPath(neckPath, darkColor.copy(alpha = 0.3f), style = Stroke(width = 1f))

    // Head (round ball on top)
    val headCenter = Offset(center.x, center.y - radius * 0.4f)
    val headRadius = radius * 0.38f
    drawCircle(tokenColor, headRadius, headCenter)
    drawCircle(darkColor.copy(alpha = 0.4f), headRadius, headCenter, style = Stroke(width = 1.2f))
    // Head highlight
    drawCircle(lightColor.copy(alpha = 0.5f), headRadius * 0.45f, Offset(headCenter.x - headRadius * 0.2f, headCenter.y - headRadius * 0.2f))
}

// ── Helper: create 5-pointed star path ──

private fun createStarPath(center: Offset, outerR: Float, innerR: Float): Path {
    val path = Path()
    val step = PI.toFloat() / 5f
    val start = -PI.toFloat() / 2f
    for (i in 0 until 10) {
        val rad = if (i % 2 == 0) outerR else innerR
        val angle = start + i * step
        val x = center.x + rad * cos(angle)
        val y = center.y + rad * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

// ── Color helpers ──

fun darkenColor(color: Color): Color = Color(
    red = color.red * 0.55f,
    green = color.green * 0.55f,
    blue = color.blue * 0.55f
)

fun lightenColor(color: Color): Color = Color(
    red = minOf(1f, color.red + (1f - color.red) * 0.45f),
    green = minOf(1f, color.green + (1f - color.green) * 0.45f),
    blue = minOf(1f, color.blue + (1f - color.blue) * 0.45f)
)

fun colorForPlayer(color: PlayerColor): Color = when (color) {
    PlayerColor.RED -> LudoRed
    PlayerColor.GREEN -> LudoGreen
    PlayerColor.YELLOW -> LudoYellow
    PlayerColor.BLUE -> LudoBlue
}

fun lightColorForPlayer(color: PlayerColor): Color = when (color) {
    PlayerColor.RED -> LudoRedLight
    PlayerColor.GREEN -> LudoGreenLight
    PlayerColor.YELLOW -> LudoYellowLight
    PlayerColor.BLUE -> LudoBlueLight
}
