package com.shaeed.ludo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.shaeed.ludo.model.PlayerColor
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DiceView(
    value: Int?,
    isRolling: Boolean,
    enabled: Boolean,
    playerColor: PlayerColor,
    onRoll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayValue by remember { mutableIntStateOf(value ?: 1) }
    val tumbleAngle = remember { Animatable(0f) }
    val bounceScale = remember { Animatable(1f) }

    LaunchedEffect(isRolling) {
        if (isRolling) {
            coroutineScope {
                // 3D tumble rotation
                launch {
                    tumbleAngle.animateTo(
                        targetValue = 720f,
                        animationSpec = tween(600, easing = FastOutSlowInEasing)
                    )
                }
                // Bounce scale
                launch {
                    bounceScale.animateTo(1.25f, tween(100))
                    bounceScale.animateTo(1.0f, tween(500, easing = FastOutSlowInEasing))
                }
                // Random face values
                launch {
                    repeat(8) { i ->
                        displayValue = (1..6).random()
                        delay(50L + i * 20L)
                    }
                }
            }
            // Animation completed naturally
            tumbleAngle.snapTo(0f)
            if (value != null) displayValue = value
        } else {
            // Rolling stopped (effect restarted after cancellation) — reset stuck state
            tumbleAngle.snapTo(0f)
            bounceScale.snapTo(1f)
            if (value != null) displayValue = value
        }
    }

    LaunchedEffect(value) {
        if (value != null) {
            displayValue = value
            tumbleAngle.snapTo(0f)
            // Landing bounce
            bounceScale.snapTo(1.12f)
            bounceScale.animateTo(
                1f,
                spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium)
            )
        }
    }

    val diceColor = colorForPlayer(playerColor)

    Canvas(
        modifier = modifier
            .size(44.dp)
            .graphicsLayer {
                scaleX = bounceScale.value
                scaleY = bounceScale.value
                rotationX = tumbleAngle.value
                rotationZ = tumbleAngle.value * 0.15f
                cameraDistance = 12f * density
            }
            .clickable(enabled = enabled && !isRolling) { onRoll() }
    ) {
        val dieSize = size.minDimension
        val cornerRadius = dieSize * 0.18f
        val dotRadius = dieSize * 0.075f
        val padding = dieSize * 0.18f
        val faceColor = if (enabled) diceColor else diceColor.copy(alpha = 0.35f)
        val darkColor = Color(
            red = faceColor.red * 0.6f,
            green = faceColor.green * 0.6f,
            blue = faceColor.blue * 0.6f,
            alpha = faceColor.alpha
        )

        // Drop shadow
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.25f),
            topLeft = Offset(2f, 3f),
            size = size,
            cornerRadius = CornerRadius(cornerRadius)
        )

        // Bottom edge (3D depth)
        drawRoundRect(
            color = darkColor,
            topLeft = Offset(0f, 2.5f),
            size = size,
            cornerRadius = CornerRadius(cornerRadius)
        )

        // Main face
        drawRoundRect(
            color = faceColor,
            size = size,
            cornerRadius = CornerRadius(cornerRadius)
        )

        // Top highlight (glossy effect)
        drawRoundRect(
            color = Color.White.copy(alpha = 0.2f),
            topLeft = Offset(3f, 3f),
            size = Size(size.width - 6f, size.height * 0.45f),
            cornerRadius = CornerRadius(cornerRadius)
        )

        // Border
        drawRoundRect(
            color = darkColor,
            size = size,
            cornerRadius = CornerRadius(cornerRadius),
            style = Stroke(1.5f)
        )

        // Dots with shadow for contrast on all player colors
        drawDiceDots3D(displayValue, dieSize, padding, dotRadius)
    }
}

private fun DrawScope.drawDiceDots3D(
    value: Int, dieSize: Float, padding: Float, dotRadius: Float
) {
    val center = dieSize / 2f
    val left = padding + dotRadius
    val right = dieSize - padding - dotRadius
    val top = padding + dotRadius
    val bottom = dieSize - padding - dotRadius

    val positions = mutableListOf<Offset>()

    // Center dot (1, 3, 5)
    if (value in listOf(1, 3, 5)) {
        positions.add(Offset(center, center))
    }
    // Top-left and bottom-right (2, 3, 4, 5, 6)
    if (value >= 2) {
        positions.add(Offset(left, top))
        positions.add(Offset(right, bottom))
    }
    // Top-right and bottom-left (4, 5, 6)
    if (value >= 4) {
        positions.add(Offset(right, top))
        positions.add(Offset(left, bottom))
    }
    // Middle-left and middle-right (6)
    if (value == 6) {
        positions.add(Offset(left, center))
        positions.add(Offset(right, center))
    }

    for (pos in positions) {
        // Dot shadow
        drawCircle(
            Color.Black.copy(alpha = 0.3f), dotRadius,
            Offset(pos.x + 0.5f, pos.y + 0.8f)
        )
        // White dot
        drawCircle(Color.White, dotRadius, pos)
        // Specular highlight
        drawCircle(
            Color.White.copy(alpha = 0.4f), dotRadius * 0.35f,
            Offset(pos.x - dotRadius * 0.2f, pos.y - dotRadius * 0.2f)
        )
    }
}
