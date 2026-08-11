package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.example.model.PlayerColor
import com.example.ui.theme.*
import com.example.viewmodel.GameState
import com.example.viewmodel.TacticalWeapon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TacticalHUD(
    state: GameState,
    onBackToMenu: () -> Unit,
    onRestartGame: () -> Unit,
    onActivateWeapon: (TacticalWeapon) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Top Navigation & Status Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackToMenu, modifier = Modifier.testTag("btn_hud_back")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Меню")
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = when (state.activePlayer) {
                        PlayerColor.RED -> PastelRoseContainer
                        PlayerColor.BLUE -> PastelSkyContainer
                        PlayerColor.GREEN -> PastelMintContainer
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(10.dp),
                            shape = RoundedCornerShape(5.dp),
                            color = Color(state.activePlayer.hexColor)
                        ) {}
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Ход: ${state.activePlayer.displayName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                IconButton(onClick = onRestartGame, modifier = Modifier.testTag("btn_hud_restart")) {
                    Icon(Icons.Default.Refresh, contentDescription = "Заново")
                }
            }
        }

        // 2. 3-Player Energy & Status Badges Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlayerBadgeCard(
                color = PlayerColor.RED,
                energy = state.redEnergy,
                isActive = state.activePlayer == PlayerColor.RED,
                modifier = Modifier.weight(1f)
            )
            PlayerBadgeCard(
                color = PlayerColor.BLUE,
                energy = state.blueEnergy,
                isActive = state.activePlayer == PlayerColor.BLUE,
                modifier = Modifier.weight(1f)
            )
            PlayerBadgeCard(
                color = PlayerColor.GREEN,
                energy = state.greenEnergy,
                isActive = state.activePlayer == PlayerColor.GREEN,
                modifier = Modifier.weight(1f)
            )
        }

        // 3. Tactical Weapons Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val currEnergy = when (state.activePlayer) {
                PlayerColor.RED -> state.redEnergy
                PlayerColor.BLUE -> state.blueEnergy
                PlayerColor.GREEN -> state.greenEnergy
            }

            Button(
                onClick = { onActivateWeapon(TacticalWeapon.NUKE) },
                enabled = currEnergy >= 100 && !state.isAiThinking,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("btn_weapon_nuke"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.activeWeapon == TacticalWeapon.NUKE) Color(0xFFE57373) else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (state.activeWeapon == TacticalWeapon.NUKE) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("ЯДЕРНЫЙ (100%)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { onActivateWeapon(TacticalWeapon.AIRSTRIKE) },
                enabled = currEnergy >= 50 && !state.isAiThinking,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("btn_weapon_airstrike"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.activeWeapon == TacticalWeapon.AIRSTRIKE) Color(0xFFFFB74D) else MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (state.activeWeapon == TacticalWeapon.AIRSTRIKE) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.Default.FlightTakeoff, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("АВИАУДАР (50%)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 4. Game Log Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                reverseLayout = false
            ) {
                items(state.gameLogs) { log ->
                    Text(
                        text = "• $log",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerBadgeCard(
    color: PlayerColor,
    energy: Int,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(color.containerColor) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 3.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = color.displayName.split(" ")[0],
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp,
                color = Color(color.hexColor)
            )
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { energy / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(color.hexColor),
                trackColor = Color(color.containerColor)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "$energy%",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
