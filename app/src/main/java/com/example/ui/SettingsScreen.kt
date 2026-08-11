package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.SettingsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: SettingsState,
    onUpdateSettings: (
        isSoundEnabled: Boolean?,
        soundVolume: Float?,
        aiDifficultyName: String?,
        showMoveHints: Boolean?,
        colorTheme: String?,
        particleEffects: Boolean?
    ) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки Игры", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_settings_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sound Effects Section Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(10.dp))
                            Text("Звуковые эффекты", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                        Switch(
                            checked = settings.isSoundEnabled,
                            onCheckedChange = { onUpdateSettings(it, null, null, null, null, null) },
                            modifier = Modifier.testTag("switch_sound_enabled")
                        )
                    }

                    if (settings.isSoundEnabled) {
                        Text(
                            text = "Громкость звука: ${(settings.soundVolume * 100).toInt()}%",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = settings.soundVolume,
                            onValueChange = { vol ->
                                onUpdateSettings(null, vol, null, null, null, null)
                                SoundEffectsEngine.playMoveSound()
                            },
                            valueRange = 0f..1f,
                            modifier = Modifier.testTag("slider_sound_volume")
                        )
                    }
                }
            }

            // AI Speed & Difficulty Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Text("Скорость мышления ИИ", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Быстрый", "Сбалансированный", "Мастер").forEach { diff ->
                            FilterChip(
                                selected = settings.aiDifficultyName == diff,
                                onClick = { onUpdateSettings(null, null, diff, null, null, null) },
                                label = { Text(diff, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Board Theme Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Text("Цветовая палитра доски", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Пастельный Песок", "Мятный Бриз", "Лавандовый Сон").forEach { themeName ->
                            FilterChip(
                                selected = settings.colorTheme == themeName,
                                onClick = { onUpdateSettings(null, null, null, null, themeName, null) },
                                label = { Text(themeName, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Gameplay Assists Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(10.dp))
                            Text("Подсветка возможных ходов", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                        Switch(
                            checked = settings.showMoveHints,
                            onCheckedChange = { onUpdateSettings(null, null, null, it, null, null) },
                            modifier = Modifier.testTag("switch_move_hints")
                        )
                    }
                }
            }
        }
    }
}
