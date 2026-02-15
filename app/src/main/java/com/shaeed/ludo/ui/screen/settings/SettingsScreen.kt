package com.shaeed.ludo.ui.screen.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.shaeed.ludo.audio.SoundManagerHolder
import com.shaeed.ludo.data.UserPreferences
import com.shaeed.ludo.model.PlayerColor
import com.shaeed.ludo.model.TokenStyle
import com.shaeed.ludo.model.TokenStyleHolder
import com.shaeed.ludo.ui.components.TokenPiece

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onAboutClicked: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }

    var soundEnabled by remember { mutableStateOf(prefs.soundEnabled) }
    var shakeToRollEnabled by remember { mutableStateOf(prefs.shakeToRollEnabled) }
    var activePreset by remember { mutableStateOf(prefs.activePreset) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            SettingRow(
                title = "Sound Effects",
                description = "Play sound effects during the game",
                checked = soundEnabled,
                onCheckedChange = {
                    soundEnabled = it
                    prefs.soundEnabled = it
                    SoundManagerHolder.instance.setEnabled(it)
                }
            )

            HorizontalDivider()

            SettingRow(
                title = "Shake to Roll",
                description = "Shake your device to roll the dice",
                checked = shakeToRollEnabled,
                onCheckedChange = {
                    shakeToRollEnabled = it
                    prefs.shakeToRollEnabled = it
                }
            )

            HorizontalDivider()

            Spacer(modifier = Modifier.height(24.dp))

            // ── Token Style picker ──
            Text(
                text = "Token Style",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            val selectedStyle = TokenStyleHolder.current

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(TokenStyle.entries.toList()) { style ->
                    val isSelected = style == selectedStyle
                    val border = if (isSelected) {
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    }

                    OutlinedCard(
                        onClick = {
                            TokenStyleHolder.current = style
                            prefs.tokenStyle = style
                        },
                        border = border,
                        modifier = Modifier.width(80.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 4.dp)
                        ) {
                            TokenPiece(
                                color = PlayerColor.RED,
                                size = 36.dp,
                                tokenStyle = style
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = style.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Rule Presets",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Sets the rules for new games",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            val isClassic = activePreset == "classic"
            val isCasual = activePreset == "casual"

            if (isClassic) {
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Classic Rules (Enter on 6, Safe Zones)")
                }
            } else {
                OutlinedButton(
                    onClick = {
                        prefs.applyClassicPreset()
                        activePreset = "classic"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Classic Rules (Enter on 6, Safe Zones)")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isCasual) {
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Casual Rules (Enter on 1 or 6)")
                }
            } else {
                OutlinedButton(
                    onClick = {
                        prefs.applyCasualPreset()
                        activePreset = "casual"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Casual Rules (Enter on 1 or 6)")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onAboutClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("About & Game Rules")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
