package com.shaeed.ludo.ui.screen.about

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shaeed.ludo.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
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
            // App Info Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LUDO",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Classic Board Game",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Version ${BuildConfig.VERSION_NAME}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About Developer Section
            Text(
                text = "About the Developer",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Created by Shaeed",
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "This Ludo app was built with love using Kotlin and Jetpack Compose. " +
                                "It features AI opponents, customizable rules, and a clean Material Design 3 interface.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Game Rules Section
            Text(
                text = "How to Play Ludo",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            RuleSection(
                title = "Objective",
                content = "Be the first player to move all four of your tokens from the starting area to the home (center of the board)."
            )

            RuleSection(
                title = "Starting the Game",
                content = "Each player starts with 4 tokens in their colored base. Roll a 6 to move a token onto the board. " +
                        "Tokens enter the board at the starting position of their color."
            )

            RuleSection(
                title = "Moving Tokens",
                content = "Roll the dice and move one of your tokens forward by the number shown. " +
                        "Tokens move clockwise around the board, then up their home stretch to reach the center."
            )

            RuleSection(
                title = "Rolling a 6",
                content = "When you roll a 6, you get an extra turn! You can use the 6 to bring a new token onto the board " +
                        "or move an existing token. Rolling three 6s in a row forfeits your turn."
            )

            RuleSection(
                title = "Capturing",
                content = "If your token lands on a cell occupied by an opponent's token, you capture it! " +
                        "The captured token is sent back to its base and must start over. You also get a bonus turn for capturing."
            )

            RuleSection(
                title = "Safe Zones",
                content = "Cells marked with a star are safe zones. Tokens on safe zones cannot be captured. " +
                        "The colored starting cells are also safe for tokens of that color."
            )

            RuleSection(
                title = "Home Stretch",
                content = "Once a token completes a full circuit of the board, it enters the home stretch (colored path leading to center). " +
                        "Only your tokens can enter your home stretch."
            )

            RuleSection(
                title = "Winning",
                content = "The first player to move all four tokens into the home (center) wins the game!"
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RuleSection(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
