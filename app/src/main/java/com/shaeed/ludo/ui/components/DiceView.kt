package com.shaeed.ludo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun DiceView(
    value: Int?,
    isRolling: Boolean,
    enabled: Boolean,
    onRoll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayValue by remember { mutableIntStateOf(value ?: 1) }
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(isRolling) {
        if (isRolling) {
            // Animate through random values
            repeat(8) { i ->
                displayValue = (1..6).random()
                delay(60L + i * 30L) // ease-out timing
            }
            // Settle on final value
            if (value != null) displayValue = value
        }
    }

    LaunchedEffect(value) {
        if (value != null && !isRolling) {
            displayValue = value
        }
    }

    Canvas(
        modifier = modifier
            .size(64.dp)
            .clickable(enabled = enabled) { onRoll() }
    ) {
        val dieSize = size.minDimension
        val padding = dieSize * 0.1f
        val dotRadius = dieSize * 0.08f

        // Draw die background
        drawRoundRect(
            color = Color.White,
            size = size,
            cornerRadius = CornerRadius(dieSize * 0.15f)
        )
        drawRoundRect(
            color = Color.DarkGray,
            size = size,
            cornerRadius = CornerRadius(dieSize * 0.15f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
        )

        // Draw dots
        drawDiceDots(displayValue, dieSize, padding, dotRadius)
    }
}

private fun DrawScope.drawDiceDots(value: Int, dieSize: Float, padding: Float, dotRadius: Float) {
    val center = dieSize / 2f
    val left = padding + dotRadius
    val right = dieSize - padding - dotRadius
    val top = padding + dotRadius
    val bottom = dieSize - padding - dotRadius
    val dotColor = Color.Black

    // Center dot (1, 3, 5)
    if (value in listOf(1, 3, 5)) {
        drawCircle(dotColor, dotRadius, Offset(center, center))
    }
    // Top-left and bottom-right (2, 3, 4, 5, 6)
    if (value >= 2) {
        drawCircle(dotColor, dotRadius, Offset(left, top))
        drawCircle(dotColor, dotRadius, Offset(right, bottom))
    }
    // Top-right and bottom-left (4, 5, 6)
    if (value >= 4) {
        drawCircle(dotColor, dotRadius, Offset(right, top))
        drawCircle(dotColor, dotRadius, Offset(left, bottom))
    }
    // Middle-left and middle-right (6)
    if (value == 6) {
        drawCircle(dotColor, dotRadius, Offset(left, center))
        drawCircle(dotColor, dotRadius, Offset(right, center))
    }
}
