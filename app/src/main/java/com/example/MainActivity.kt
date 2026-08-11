package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ChessViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ChessViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val state by viewModel.uiState.collectAsState()

                when (state.currentScreen) {
                    AppScreen.MAIN_MENU -> {
                        MainMenuScreen(
                            state = state,
                            onStartGame = { isVsAi -> viewModel.startNewGame(isVsAi) },
                            onOpenSettings = { viewModel.navigateTo(AppScreen.SETTINGS) },
                            onOpenRules = { viewModel.navigateTo(AppScreen.RULES) }
                        )
                    }

                    AppScreen.SETTINGS -> {
                        SettingsScreen(
                            settings = state.settings,
                            onUpdateSettings = { sound, vol, aiDiff, hints, theme, particles ->
                                viewModel.updateSettings(
                                    isSoundEnabled = sound,
                                    soundVolume = vol,
                                    aiDifficultyName = aiDiff,
                                    showMoveHints = hints,
                                    colorTheme = theme,
                                    particleEffects = particles
                                )
                            },
                            onBack = { viewModel.navigateTo(AppScreen.MAIN_MENU) }
                        )
                    }

                    AppScreen.RULES -> {
                        RulesScreen(
                            onBack = { viewModel.navigateTo(AppScreen.MAIN_MENU) }
                        )
                    }

                    AppScreen.GAME -> {
                        GameScreen(
                            viewModel = viewModel,
                            state = state
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameScreen(
    viewModel: ChessViewModel,
    state: com.example.viewmodel.GameState
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Tactical HUD Header & Badges
            TacticalHUD(
                state = state,
                onBackToMenu = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                onRestartGame = { viewModel.startNewGame(state.isVsAi) },
                onActivateWeapon = { weapon -> viewModel.activateWeapon(weapon) }
            )

            // Hexagonal Board Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                ChessBoardCanvas(
                    board = viewModel.board,
                    selectedHex = state.selectedHex,
                    validMoves = state.validMoves,
                    activeWeapon = state.activeWeapon,
                    activeExplosions = state.activeExplosions,
                    activePlayer = state.activePlayer,
                    colorTheme = state.settings.colorTheme,
                    onHexClick = { pos -> viewModel.selectHex(pos) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
            }
        }
    }
}
