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
import com.shaeed.ludo.model.GameState
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
        // Top player panels with dice
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (state.players.isNotEmpty()) {
                PlayerDice(
                    playerIndex = 0, state = state,
                    isRolling = viewModel.isRolling, canRoll = canRoll,
                    onRoll = { viewModel.rollDice() }
                )
                PlayerPanel(
                    player = state.players[0],
                    isCurrentTurn = state.currentPlayerIndex == 0,
                    modifier = Modifier.weight(1f)
                )
            }
            if (state.players.size >= 2) {
                PlayerPanel(
                    player = state.players[1],
                    isCurrentTurn = state.currentPlayerIndex == 1,
                    modifier = Modifier.weight(1f)
                )
                PlayerDice(
                    playerIndex = 1, state = state,
                    isRolling = viewModel.isRolling, canRoll = canRoll,
                    onRoll = { viewModel.rollDice() }
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

        // Bottom player panels with dice
        if (state.players.size > 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Bottom-left player (last player)
                val leftIdx = state.players.size - 1
                PlayerDice(
                    playerIndex = leftIdx, state = state,
                    isRolling = viewModel.isRolling, canRoll = canRoll,
                    onRoll = { viewModel.rollDice() }
                )
                PlayerPanel(
                    player = state.players[leftIdx],
                    isCurrentTurn = state.currentPlayerIndex == leftIdx,
                    modifier = Modifier.weight(1f)
                )

                // Bottom-right player (player 2) if 4 players
                if (state.players.size > 3) {
                    PlayerPanel(
                        player = state.players[2],
                        isCurrentTurn = state.currentPlayerIndex == 2,
                        modifier = Modifier.weight(1f)
                    )
                    PlayerDice(
                        playerIndex = 2, state = state,
                        isRolling = viewModel.isRolling, canRoll = canRoll,
                        onRoll = { viewModel.rollDice() }
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Turn status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
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

@Composable
private fun PlayerDice(
    playerIndex: Int,
    state: GameState,
    isRolling: Boolean,
    canRoll: Boolean,
    onRoll: () -> Unit
) {
    val isThisTurn = state.currentPlayerIndex == playerIndex
    DiceView(
        value = state.players[playerIndex].diceValue,
        isRolling = isRolling && isThisTurn,
        enabled = canRoll && isThisTurn,
        playerColor = state.players[playerIndex].color,
        onRoll = onRoll
    )
}
