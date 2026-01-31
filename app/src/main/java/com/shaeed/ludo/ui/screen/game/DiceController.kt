package com.shaeed.ludo.ui.screen.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.shaeed.ludo.sensor.ShakeDetector

@Composable
fun DiceController(
    enabled: Boolean,
    onShake: () -> Unit
) {
    val context = LocalContext.current

    DisposableEffect(enabled) {
        val detector = if (enabled) {
            ShakeDetector(context) { onShake() }.also { it.start() }
        } else null

        onDispose {
            detector?.stop()
        }
    }
}
