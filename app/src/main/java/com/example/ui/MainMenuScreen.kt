package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.GameState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    state: GameState,
    onStartGame: (isVsAi: Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRules: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(16.dp))

            // Hero Header Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PastelRoseContainer,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "🥐 3-PLAYER HEX CHESS",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = PastelRoseOnContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        text = "Тактические Шахматы",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Шестиугольное поле • 3 Фракции • Ядерная стратегия",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            // Play Options
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onStartGame(true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("btn_start_ai"),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.SmartToy, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Играть против ИИ Ботов (2 ИИ)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                FilledTonalButton(
                    onClick = { onStartGame(false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("btn_start_local"),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.People, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Локальный Матч (3 Игрока)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatBadge(
                        icon = Icons.Default.Casino,
                        value = "${state.gamesPlayedCount}",
                        label = "Партий сыграно"
                    )
                    StatBadge(
                        icon = Icons.Default.Warning,
                        value = "${state.nukesLaunchedCount}",
                        label = "Ядерных ударов"
                    )
                }
            }

            // Secondary Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_menu_settings"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Настройки")
                }

                OutlinedButton(
                    onClick = onOpenRules,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_menu_rules"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Правила")
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StatBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
