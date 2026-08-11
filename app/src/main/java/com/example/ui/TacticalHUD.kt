package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PieceType
import com.example.model.PlayerColor
import com.example.viewmodel.GameState
import com.example.viewmodel.TacticalWeapon

@Composable
fun TacticalHUD(
    state: GameState,
    onActivateWeapon: (TacticalWeapon) -> Unit,
    onResetGame: () -> Unit,
    onToggleAi: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF121324))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Player Status Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerBadge(
                color = PlayerColor.RED,
                isActive = state.activePlayer == PlayerColor.RED,
                energy = state.redEnergy,
                captured = state.capturedRed
            )
            PlayerBadge(
                color = PlayerColor.BLUE,
                isActive = state.activePlayer == PlayerColor.BLUE,
                energy = state.blueEnergy,
                captured = state.capturedBlue
            )
            PlayerBadge(
                color = PlayerColor.GREEN,
                isActive = state.activePlayer == PlayerColor.GREEN,
                energy = state.greenEnergy,
                captured = state.capturedGreen
            )
        }

        // Winner Banner
        state.winner?.let { winner ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(winner.hexColor), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🏆 ПОБЕДИТЕЛЬ: ${winner.displayName}!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        // Weapon Control Panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Nuclear Bomb Button
            val nukeActive = state.activeWeapon == TacticalWeapon.NUKE
            Button(
                onClick = { onActivateWeapon(TacticalWeapon.NUKE) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_nuke"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (nukeActive) Color(0xFFFF3333) else Color(0xFF331A1A),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF4D4D))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Nuclear")
                    Text(
                        text = if (nukeActive) "ОТМЕНА" else "ЯДЕРНЫЙ УДАР (100%)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            // Airstrike Button
            val airActive = state.activeWeapon == TacticalWeapon.AIRSTRIKE
            Button(
                onClick = { onActivateWeapon(TacticalWeapon.AIRSTRIKE) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_airstrike"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (airActive) Color(0xFFFFD700) else Color(0xFF332D1A),
                    contentColor = if (airActive) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.AirplanemodeActive, contentDescription = "Airstrike")
                    Text(
                        text = if (airActive) "ОТМЕНА" else "АВИАУДАР (50%)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Mode and Reset Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onToggleAi,
                modifier = Modifier.testTag("btn_toggle_ai"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E676))
            ) {
                Icon(Icons.Default.SmartToy, contentDescription = "AI Mode", modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (state.isVsAi) "Режим: vs ИИ Боты" else "Режим: 3 Игрока", fontSize = 12.sp)
            }

            IconButton(
                onClick = onResetGame,
                modifier = Modifier.testTag("btn_reset_game")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset Game", tint = Color.White)
            }
        }

        // Tactical Event Console Log
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF0A0B14))
                .border(1.dp, Color(0xFF2A2C42), RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(state.gameLogs) { log ->
                    Text(
                        text = "> $log",
                        color = Color(0xFF80D8FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerBadge(
    color: PlayerColor,
    isActive: Boolean,
    energy: Int,
    captured: List<PieceType>
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) Color(color.hexColor).copy(alpha = 0.3f) else Color(0xFF1E2038))
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) Color(color.hexColor) else Color(0xFF3B3E5E),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = color.displayName.substringBefore(" "),
            color = Color(color.hexColor),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "⚡ $energy%",
            color = Color.Yellow,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
