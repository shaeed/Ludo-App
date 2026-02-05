package com.shaeed.ludo.ui.screen.setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shaeed.ludo.model.AiDifficulty
import com.shaeed.ludo.model.GameConfigHolder
import com.shaeed.ludo.ui.components.colorForPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSetupScreen(
    onStartGame: () -> Unit,
    onBack: () -> Unit,
    viewModel: GameSetupViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Setup") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Player count
            Text("Players", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (count in 2..4) {
                    FilterChip(
                        selected = viewModel.playerCount == count,
                        onClick = { viewModel.updatePlayerCount(count) },
                        label = { Text("$count Players") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Player configuration
            for (i in 0 until viewModel.playerCount) {
                val config = viewModel.playerConfigs[i]
                val playerColor = colorForPlayer(config.color)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = playerColor.copy(alpha = 0.1f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = config.name,
                                onValueChange = { viewModel.setPlayerName(i, it) },
                                label = { Text("Name") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("AI", fontSize = 14.sp)
                                Switch(
                                    checked = config.isAI,
                                    onCheckedChange = { viewModel.toggleAI(i) }
                                )
                            }
                        }

                        if (config.isAI) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AiDifficulty.entries.forEach { difficulty ->
                                    FilterChip(
                                        selected = config.difficulty == difficulty,
                                        onClick = { viewModel.setAiDifficulty(i, difficulty) },
                                        label = { Text(difficulty.name.lowercase().replaceFirstChar { it.uppercase() }) }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // House rules
            Text("House Rules", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))

            RuleToggle("Enter board on 6 only", viewModel.enterOnSixOnly) {
                viewModel.toggleEnterOnSixOnly()
            }
            RuleToggle("Safe zones enabled", viewModel.safeZonesEnabled) {
                viewModel.toggleSafeZones()
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Max consecutive 6s: ${viewModel.maxConsecutiveSixes}", modifier = Modifier.weight(1f))
                Row {
                    TextButton(onClick = { viewModel.updateMaxConsecutiveSixes(viewModel.maxConsecutiveSixes - 1) }) {
                        Text("-")
                    }
                    TextButton(onClick = { viewModel.updateMaxConsecutiveSixes(viewModel.maxConsecutiveSixes + 1) }) {
                        Text("+")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    GameConfigHolder.current = viewModel.buildConfig()
                    onStartGame()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Start Game", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RuleToggle(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = { onToggle() })
    }
}
