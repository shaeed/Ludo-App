package com.shaeed.ludo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shaeed.ludo.model.Cell
import com.shaeed.ludo.model.Player

@Composable
fun PlayerPanel(
    player: Player,
    isCurrentTurn: Boolean,
    modifier: Modifier = Modifier
) {
    val playerColor = colorForPlayer(player.color)
    val borderColor = if (isCurrentTurn) playerColor else Color.Transparent
    val bgColor = if (isCurrentTurn) {
        lightColorForPlayer(player.color).copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = modifier
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TokenPiece(color = player.color, size = 20.dp)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = player.name,
                fontSize = 13.sp,
                fontWeight = if (isCurrentTurn) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val inBase = player.tokens.count { it.cell is Cell.Base }
                val onBoard = player.tokens.count { it.cell is Cell.Normal || it.cell is Cell.HomeStretch }
                val atHome = player.tokens.count { it.cell is Cell.Home }
                Text(text = "B:$inBase", fontSize = 10.sp, color = Color.Gray)
                Text(text = "P:$onBoard", fontSize = 10.sp, color = Color.Gray)
                Text(text = "H:$atHome", fontSize = 10.sp, color = Color.Gray)
            }
        }

        if (player.isAI) {
            Text(
                text = "AI",
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}
