package com.shaeed.ludo.ui.screen.online

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shaeed.ludo.model.PlayerColor

@Composable
fun OnlineSetupScreen(
    onNavigateToGame: () -> Unit,
    onBack: () -> Unit,
    viewModel: OnlineSetupViewModel = viewModel(),
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Create Room", "Join Room")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("Back") }
            Text(
                text = "Play Online",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.width(64.dp))
        }

        // Server URL field (shared)
        OutlinedTextField(
            value = viewModel.serverUrl,
            onValueChange = { viewModel.updateServerUrl(it) },
            label = { Text("Server URL") },
            placeholder = { Text("http://your-vm-ip:8080") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                        viewModel.clearError()
                    },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> CreateRoomTab(viewModel, onNavigateToGame)
            1 -> JoinRoomTab(viewModel, onNavigateToGame)
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Create Room tab
// -------------------------------------------------------------------------------------------------

@Composable
private fun CreateRoomTab(
    viewModel: OnlineSetupViewModel,
    onNavigateToGame: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val code = viewModel.createdRoomCode

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (code == null) {
            // ---- Step 1: configure and create the room ----
            PlayerIdentitySection(viewModel)

            Text("Number of Players: ${viewModel.playerCount}", fontWeight = FontWeight.Medium)
            Slider(
                value = viewModel.playerCount.toFloat(),
                onValueChange = { viewModel.updatePlayerCount(it.toInt()) },
                valueRange = 2f..4f,
                steps = 1,
            )

            Text(
                "Game rules from your Settings will apply.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Button(
                onClick = { viewModel.createRoom(onSuccess = {}) },
                enabled = !viewModel.isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.large,
            ) {
                if (viewModel.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else Text("Create Room", fontSize = 16.sp)
            }
        } else {
            // ---- Step 2: share code, then open the lobby ----
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Room Code", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = code,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 8.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { clipboard.setText(AnnotatedString(code)) }) {
                        Text("Copy Code")
                    }
                }
            }

            Text(
                "Share this code with your friends. Once they have joined, tap Open Lobby.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Button(
                onClick = { viewModel.prepareHostAndNavigate(onNavigateToGame) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.large,
            ) {
                Text("Open Lobby", fontSize = 16.sp)
            }
        }

        ErrorMessage(viewModel.errorMessage)
    }
}

// -------------------------------------------------------------------------------------------------
// Join Room tab
// -------------------------------------------------------------------------------------------------

@Composable
private fun JoinRoomTab(
    viewModel: OnlineSetupViewModel,
    onNavigateToGame: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PlayerIdentitySection(viewModel)

        OutlinedTextField(
            value = viewModel.joinCode,
            onValueChange = { viewModel.updateJoinCode(it) },
            label = { Text("Room Code") },
            placeholder = { Text("e.g. X7K2MQ") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        if (viewModel.joinCode.length < 6) {
            Text(
                "Enter the 6-character code shared by the host.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Button(
            onClick = { viewModel.joinRoom(onNavigateToGame) },
            enabled = !viewModel.isLoading && viewModel.joinCode.length == 6,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            if (viewModel.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else Text("Join Room", fontSize = 16.sp)
        }

        ErrorMessage(viewModel.errorMessage)
    }
}

// -------------------------------------------------------------------------------------------------
// Shared composables
// -------------------------------------------------------------------------------------------------

@Composable
private fun PlayerIdentitySection(viewModel: OnlineSetupViewModel) {
    OutlinedTextField(
        value = viewModel.myName,
        onValueChange = { viewModel.updateMyName(it.take(20)) },
        label = { Text("Your Name") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )

    Text("Your Color", fontWeight = FontWeight.Medium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PlayerColor.entries.forEach { color ->
            val selected = viewModel.myColor == color
            FilterChip(
                selected = selected,
                onClick = { viewModel.updateMyColor(color) },
                label = { Text(color.name) },
            )
        }
    }
}

@Composable
private fun ErrorMessage(message: String?) {
    if (message != null) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            fontSize = 13.sp,
        )
    }
}
