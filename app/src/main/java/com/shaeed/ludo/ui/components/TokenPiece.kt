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

        // Token body
        drawCircle(
            color = tokenColor,
            radius = radius,
            center = center
        )

        // Token border
        drawCircle(
            color = Color.Black.copy(alpha = 0.3f),
            radius = radius,
            center = center,
            style = Stroke(width = 2f)
        )

        // Inner highlight
        drawCircle(
            color = Color.White.copy(alpha = 0.4f),
            radius = radius * 0.4f,
            center = Offset(center.x - radius * 0.15f, center.y - radius * 0.15f)
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
