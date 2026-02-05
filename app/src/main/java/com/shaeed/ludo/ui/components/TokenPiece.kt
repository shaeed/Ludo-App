package com.shaeed.ludo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shaeed.ludo.model.PlayerColor
import com.shaeed.ludo.ui.theme.*

@Composable
fun TokenPiece(
    color: PlayerColor,
    isSelected: Boolean = false,
    size: Dp = 24.dp,
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

        // Drop shadow
        drawCircle(
            color = Color.Black.copy(alpha = 0.22f),
            radius = radius,
            center = Offset(center.x + 1.5f, center.y + 2f)
        )

        // Base ring (dark edge — flat base of the cone)
        drawCircle(color = darkColor, radius = radius, center = center)

        // Cone body (main color)
        drawCircle(color = tokenColor, radius = radius * 0.85f, center = center)

        // Dome highlight (lighter center)
        drawCircle(
            color = lightColor.copy(alpha = 0.6f),
            radius = radius * 0.55f,
            center = center
        )

        // Top knob
        drawCircle(color = tokenColor, radius = radius * 0.3f, center = center)
        drawCircle(
            color = darkColor.copy(alpha = 0.5f),
            radius = radius * 0.3f,
            center = center,
            style = Stroke(width = 1.5f)
        )

        // Outer border
        drawCircle(
            color = Color.Black.copy(alpha = 0.3f),
            radius = radius,
            center = center,
            style = Stroke(width = 2f)
        )

        // Specular highlights
        drawCircle(
            color = Color.White.copy(alpha = 0.55f),
            radius = radius * 0.14f,
            center = Offset(center.x - radius * 0.25f, center.y - radius * 0.3f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.2f),
            radius = radius * 0.22f,
            center = Offset(center.x - radius * 0.18f, center.y - radius * 0.18f)
        )
    }
}

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
