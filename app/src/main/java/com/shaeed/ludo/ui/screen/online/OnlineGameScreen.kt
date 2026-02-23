package com.shaeed.ludo.ui.screen.online

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shaeed.ludo.ui.components.colorForPlayer
import com.shaeed.ludo.ui.screen.game.GameScreen

@Composable
fun OnlineGameScreen(
    onGameEnd: () -> Unit,
    viewModel: OnlineGameViewModel = viewModel(),
) {
    when (viewModel.sessionPhase) {
        OnlineSessionPhase.CONNECTING -> ConnectingScreen()
        OnlineSessionPhase.LOBBY -> LobbyScreen(viewModel, onGameEnd)
        OnlineSessionPhase.IN_GAME -> GameScreen(controller = viewModel, onGameEnd = onGameEnd)
        OnlineSessionPhase.DISCONNECTED -> DisconnectedScreen(onExit = onGameEnd)
    }
}

// -------------------------------------------------------------------------------------------------
// Connecting
// -------------------------------------------------------------------------------------------------

@Composable
private fun ConnectingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Connecting to server…")
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Lobby
// -------------------------------------------------------------------------------------------------

@Composable
private fun LobbyScreen(viewModel: OnlineGameViewModel, onGameEnd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Waiting Room", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Room: ${viewModel.roomCode}",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "You: ${viewModel.myName} (${viewModel.myColor.name})",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.connectedPlayers.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Connected players:", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    viewModel.connectedPlayers.forEach { color ->
                        val playerColor = try {
                            colorForPlayer(com.shaeed.ludo.model.PlayerColor.valueOf(color))
                        } catch (_: Exception) {
                            MaterialTheme.colorScheme.onSurface
                        }
                        Text("• $color", color = playerColor, fontSize = 15.sp)
                    }
                }
            }
        } else {
            Text(
                "Waiting for players to join…",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator(modifier = Modifier.size(28.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (viewModel.isHost) {
            Button(
                onClick = { viewModel.startGame() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.large,
            ) {
                Text("Start Game", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            Text(
                "Waiting for the host to start the game…",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        TextButton(onClick = onGameEnd) { Text("Leave Room") }
    }
}

// -------------------------------------------------------------------------------------------------
// Disconnected
// -------------------------------------------------------------------------------------------------

@Composable
private fun DisconnectedScreen(onExit: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text("Connection Lost", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "The connection to the server was lost.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onExit) { Text("Back to Menu") }
        }
    }
}
