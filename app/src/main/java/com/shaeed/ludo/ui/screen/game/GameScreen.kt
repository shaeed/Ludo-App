package com.shaeed.ludo.ui.screen.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shaeed.ludo.model.GamePhase
import com.shaeed.ludo.ui.components.DiceView
import com.shaeed.ludo.ui.components.PlayerPanel

@Composable
fun GameScreen(
    onGameEnd: () -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    val state = viewModel.gameState
    val currentPlayer = state.players[state.currentPlayerIndex]
    val isHumanTurn = !currentPlayer.isAI
    val canRoll = state.phase == GamePhase.WAITING_FOR_ROLL && isHumanTurn

    var showExitDialog by remember { mutableStateOf(false) }

    // Intercept back button when game is in progress
    BackHandler(enabled = state.phase != GamePhase.GAME_OVER) {
        showExitDialog = true
    }

    // Exit confirmation dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("End Game?") },
            text = { Text("Are you sure you want to quit? Your game progress will be lost.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        onGameEnd()
                    }
                ) {
                    Text("Yes, Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Continue Playing")
                }
            }
        )
    }

    // Shake to roll
    DiceController(
        enabled = canRoll,
        onShake = { viewModel.rollDice() }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Top player panels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 0 until minOf(2, state.players.size)) {
                PlayerPanel(
                    player = state.players[i],
                    isCurrentTurn = state.currentPlayerIndex == i,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Board
        BoardCanvas(
            gameState = state,
            layout = viewModel.getLayout(),
            legalMoves = viewModel.legalMoves,
            onCellTapped = { row, col -> viewModel.onCellTapped(row, col) },
            tokenAnimation = viewModel.tokenAnimation,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Bottom player panels
        if (state.players.size > 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 2 until state.players.size) {
                    PlayerPanel(
                        player = state.players[i],
                        isCurrentTurn = state.currentPlayerIndex == i,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Dice and controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Show gifted dice value or regular dice
            val displayDiceValue = state.giftedDice?.value ?: state.dice?.value
            DiceView(
                value = displayDiceValue,
                isRolling = viewModel.isRolling,
                enabled = canRoll,
                onRoll = { viewModel.rollDice() }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${currentPlayer.name}'s turn",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )

                // Show bonus dice indicator
                if (state.giftedDice != null || viewModel.isUsingGiftedDice) {
                    Text(
                        text = "Bonus dice from previous player!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = when {
                        viewModel.isAnimating -> "Moving..."
                        state.giftedDice != null -> if (isHumanTurn) "Use the bonus dice!" else "AI using bonus..."
                        viewModel.isUsingGiftedDice -> if (isHumanTurn) "Tap a token to move" else "AI is moving..."
                        state.phase == GamePhase.WAITING_FOR_ROLL -> if (isHumanTurn) "Tap dice or shake to roll" else "AI is thinking..."
                        state.phase == GamePhase.WAITING_FOR_MOVE -> if (isHumanTurn) "Tap a token to move" else "AI is moving..."
                        state.phase == GamePhase.ROLLING -> "Rolling..."
                        state.phase == GamePhase.ANIMATING -> "Moving..."
                        state.phase == GamePhase.GAME_OVER -> "Game Over!"
                        else -> ""
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Game over dialog
        if (state.phase == GamePhase.GAME_OVER && state.winner != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${state.players.first { it.color == state.winner }.name} Wins!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onGameEnd) {
                        Text("Back to Menu")
                    }
                }
            }
        }
    }
}
