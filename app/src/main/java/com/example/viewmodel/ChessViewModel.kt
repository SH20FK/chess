package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import com.example.ui.SoundEffectsEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class TacticalWeapon {
    NONE,
    NUKE,
    AIRSTRIKE
}

enum class AppScreen {
    MAIN_MENU,
    GAME,
    SETTINGS,
    RULES
}

data class SettingsState(
    val isSoundEnabled: Boolean = true,
    val soundVolume: Float = 0.8f,
    val aiDifficultyName: String = "Сбалансированный", // "Быстрый", "Сбалансированный", "Мастер"
    val aiDelayMs: Long = 800L,
    val showMoveHints: Boolean = true,
    val colorTheme: String = "Пастельный Песок", // "Пастельный Песок", "Мятный Бриз", "Лавандовый Сон"
    val particleEffects: Boolean = true
)

data class HexExplosionEffect(
    val id: String,
    val hexPos: HexPos,
    val radius: Float,
    val colorHex: Long,
    val timestamp: Long = System.currentTimeMillis()
)

data class GameState(
    val currentScreen: AppScreen = AppScreen.MAIN_MENU,
    val activePlayer: PlayerColor = PlayerColor.RED,
    val selectedHex: HexPos? = null,
    val validMoves: List<HexPos> = emptyList(),
    val activeWeapon: TacticalWeapon = TacticalWeapon.NONE,
    val isVsAi: Boolean = true,
    val redEnergy: Int = 50,
    val blueEnergy: Int = 50,
    val greenEnergy: Int = 50,
    val capturedRed: List<PieceType> = emptyList(),
    val capturedBlue: List<PieceType> = emptyList(),
    val capturedGreen: List<PieceType> = emptyList(),
    val gameLogs: List<String> = listOf("Тактические Шестиугольные Шахматы готовы к бою!"),
    val activeExplosions: List<HexExplosionEffect> = emptyList(),
    val winner: PlayerColor? = null,
    val isAiThinking: Boolean = false,
    val turnCount: Int = 1,
    val gamesPlayedCount: Int = 1,
    val nukesLaunchedCount: Int = 0,
    val settings: SettingsState = SettingsState()
)

class ChessViewModel : ViewModel() {

    val board = ChessBoard()

    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    init {
        updateAudioEngine()
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.value = _uiState.value.copy(currentScreen = screen)
    }

    fun startNewGame(isVsAiMode: Boolean) {
        board.resetBoard()
        val currentG = _uiState.value.gamesPlayedCount + 1
        _uiState.value = _uiState.value.copy(
            currentScreen = AppScreen.GAME,
            activePlayer = PlayerColor.RED,
            selectedHex = null,
            validMoves = emptyList(),
            activeWeapon = TacticalWeapon.NONE,
            isVsAi = isVsAiMode,
            redEnergy = 50,
            blueEnergy = 50,
            greenEnergy = 50,
            capturedRed = emptyList(),
            capturedBlue = emptyList(),
            capturedGreen = emptyList(),
            winner = null,
            turnCount = 1,
            gamesPlayedCount = currentG,
            gameLogs = listOf("Новая шестиугольная партия началась! Захватывайте центр для получения энергии.")
        )
    }

    fun selectHex(pos: HexPos) {
        val state = _uiState.value
        if (state.winner != null || state.isAiThinking) return

        val cell = board.getCell(pos) ?: return

        // Tactical weapon targeting
        if (state.activeWeapon != TacticalWeapon.NONE) {
            executeWeaponTarget(pos)
            return
        }

        // Move piece if target is in valid moves
        val selPos = state.selectedHex
        if (selPos != null && state.validMoves.contains(pos)) {
            movePiece(selPos, pos)
            return
        }

        // Select piece owned by active player
        if (cell.piece != null && cell.piece.color == state.activePlayer) {
            val moves = if (state.settings.showMoveHints) board.getValidMoves(pos) else emptyList()
            _uiState.value = state.copy(
                selectedHex = pos,
                validMoves = moves
            )
            SoundEffectsEngine.playMoveSound()
        } else {
            // Deselect
            _uiState.value = state.copy(selectedHex = null, validMoves = emptyList())
        }
    }

    private fun movePiece(from: HexPos, to: HexPos) {
        val movingPiece = board.getCell(from)?.piece ?: return
        val targetCell = board.getCell(to)
        val captured = targetCell?.piece

        board.setPiece(from, null)
        board.setPiece(to, movingPiece)

        var logMsg = "${movingPiece.color.displayName}: ${movingPiece.type.displayName} -> (${to.q}, ${to.r})"

        if (captured != null) {
            logMsg += " (Захвачен ${captured.color.displayName} ${captured.type.displayName})"
            SoundEffectsEngine.playCaptureSound()
            addEnergy(movingPiece.color, 25)
            recordCapture(movingPiece.color, captured.type)
        } else {
            SoundEffectsEngine.playMoveSound()
        }

        // Center control bonus
        if (to.distanceTo(HexPos(0, 0)) <= 1) {
            addEnergy(movingPiece.color, 15)
            logMsg += " ⚡ Контроль центральной гекс-зоны (+15 Энергии)"
        }

        addLog(logMsg)

        _uiState.value = _uiState.value.copy(
            selectedHex = null,
            validMoves = emptyList()
        )

        checkWinAndAdvanceTurn()
    }

    fun activateWeapon(weapon: TacticalWeapon) {
        val state = _uiState.value
        val cost = when (weapon) {
            TacticalWeapon.NUKE -> 100
            TacticalWeapon.AIRSTRIKE -> 50
            TacticalWeapon.NONE -> 0
        }

        val currEnergy = getEnergy(state.activePlayer)
        if (currEnergy < cost) {
            addLog("Недостаточно ядерной энергии! Нужно $cost%, у вас $currEnergy%")
            return
        }

        if (state.activeWeapon == weapon) {
            _uiState.value = state.copy(activeWeapon = TacticalWeapon.NONE)
        } else {
            _uiState.value = state.copy(
                activeWeapon = weapon,
                selectedHex = null,
                validMoves = emptyList()
            )
            addLog("Выберите цель на шестиугольной доске для удара [${if (weapon == TacticalWeapon.NUKE) "ЯДЕРНАЯ БОМБА" else "АВИАУДАР"}]!")
        }
    }

    private fun executeWeaponTarget(centerPos: HexPos) {
        val state = _uiState.value
        val player = state.activePlayer
        val weapon = state.activeWeapon

        if (weapon == TacticalWeapon.NUKE) {
            deductEnergy(player, 100)
            SoundEffectsEngine.playNukeExplosionSound()

            // Nuke destroys target hex + all 6 neighbors (radius 1 ring, 7 hexes total)
            for (cell in board.getAllCells()) {
                if (cell.pos.distanceTo(centerPos) <= 1) {
                    board.setPiece(cell.pos, null)
                    board.setCellState(cell.pos, CellState.CRATER_DESTROYED)
                }
            }

            val exp = HexExplosionEffect(
                id = "nuke_${System.currentTimeMillis()}",
                hexPos = centerPos,
                radius = 2.2f,
                colorHex = 0xFFFF7043
            )

            _uiState.value = _uiState.value.copy(
                activeWeapon = TacticalWeapon.NONE,
                nukesLaunchedCount = _uiState.value.nukesLaunchedCount + 1,
                activeExplosions = _uiState.value.activeExplosions + exp
            )

            addLog("🚀 ${player.displayName} нанёс ЯДЕРНЫЙ УДАР по гексу (${centerPos.q}, ${centerPos.r})! Образовался кратер!")

        } else if (weapon == TacticalWeapon.AIRSTRIKE) {
            deductEnergy(player, 50)
            SoundEffectsEngine.playAirstrikeSound()

            // Airstrike along hex line (same r coordinate)
            for (cell in board.getAllCells()) {
                if (cell.pos.r == centerPos.r) {
                    if (cell.piece != null) {
                        board.setPiece(cell.pos, null)
                        board.setCellState(cell.pos, CellState.RADIOACTIVE_HAZARD)
                    }
                }
            }

            val exp = HexExplosionEffect(
                id = "airstrike_${System.currentTimeMillis()}",
                hexPos = centerPos,
                radius = 1.5f,
                colorHex = 0xFFFFCA28
            )

            _uiState.value = _uiState.value.copy(
                activeWeapon = TacticalWeapon.NONE,
                activeExplosions = _uiState.value.activeExplosions + exp
            )

            addLog("✈️ ${player.displayName} вызвал АВИАУДАР по линии r=${centerPos.r}!")
        }

        viewModelScope.launch {
            delay(1500)
            _uiState.value = _uiState.value.copy(
                activeExplosions = _uiState.value.activeExplosions.filter { it.timestamp > System.currentTimeMillis() - 1500 }
            )
        }

        checkWinAndAdvanceTurn()
    }

    private fun checkWinAndAdvanceTurn() {
        val redAlive = board.isKingAlive(PlayerColor.RED)
        val blueAlive = board.isKingAlive(PlayerColor.BLUE)
        val greenAlive = board.isKingAlive(PlayerColor.GREEN)

        val aliveList = listOf(
            Pair(PlayerColor.RED, redAlive),
            Pair(PlayerColor.BLUE, blueAlive),
            Pair(PlayerColor.GREEN, greenAlive)
        ).filter { it.second }

        if (aliveList.size == 1) {
            val winner = aliveList.first().first
            _uiState.value = _uiState.value.copy(winner = winner)
            addLog("🏆 ПОБЕДА! ${winner.displayName} разгромил соперников!")
            return
        }

        var nextPlayer = _uiState.value.activePlayer.next()
        while (!isPlayerAlive(nextPlayer)) {
            nextPlayer = nextPlayer.next()
        }

        addEnergy(nextPlayer, 15)

        val newTurn = _uiState.value.turnCount + 1
        _uiState.value = _uiState.value.copy(
            activePlayer = nextPlayer,
            turnCount = newTurn
        )

        if (_uiState.value.isVsAi && (nextPlayer == PlayerColor.BLUE || nextPlayer == PlayerColor.GREEN)) {
            triggerAiTurn(nextPlayer)
        }
    }

    private fun isPlayerAlive(color: PlayerColor): Boolean {
        return board.isKingAlive(color)
    }

    private fun triggerAiTurn(aiColor: PlayerColor) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAiThinking = true)
            delay(_uiState.value.settings.aiDelayMs)

            val aiEnergy = getEnergy(aiColor)

            if (aiEnergy >= 100 && Random.nextFloat() < 0.35f) {
                val allHexes = board.getAllCells().map { it.pos }
                val targetPos = allHexes.random()
                _uiState.value = _uiState.value.copy(activeWeapon = TacticalWeapon.NUKE)
                executeWeaponTarget(targetPos)
                _uiState.value = _uiState.value.copy(isAiThinking = false)
                return@launch
            }

            val allAiPieces = mutableListOf<Pair<HexPos, List<HexPos>>>()
            for (cell in board.getAllCells()) {
                val p = cell.piece
                if (p != null && p.color == aiColor) {
                    val moves = board.getValidMoves(cell.pos)
                    if (moves.isNotEmpty()) {
                        allAiPieces.add(Pair(cell.pos, moves))
                    }
                }
            }

            if (allAiPieces.isNotEmpty()) {
                val captureMoves = mutableListOf<Pair<HexPos, HexPos>>()
                val regularMoves = mutableListOf<Pair<HexPos, HexPos>>()

                for ((from, moves) in allAiPieces) {
                    for (to in moves) {
                        val targetPiece = board.getCell(to)?.piece
                        if (targetPiece != null && targetPiece.color != aiColor) {
                            captureMoves.add(Pair(from, to))
                        } else {
                            regularMoves.add(Pair(from, to))
                        }
                    }
                }

                val chosenMove = if (captureMoves.isNotEmpty()) {
                    captureMoves.random()
                } else {
                    regularMoves.random()
                }

                movePiece(chosenMove.first, chosenMove.second)
            } else {
                addLog("🤖 ИИ ${aiColor.displayName} пропустил ход.")
                checkWinAndAdvanceTurn()
            }

            _uiState.value = _uiState.value.copy(isAiThinking = false)
        }
    }

    // Settings Updates
    fun updateSettings(
        isSoundEnabled: Boolean? = null,
        soundVolume: Float? = null,
        aiDifficultyName: String? = null,
        showMoveHints: Boolean? = null,
        colorTheme: String? = null,
        particleEffects: Boolean? = null
    ) {
        val currSettings = _uiState.value.settings
        val newSound = isSoundEnabled ?: currSettings.isSoundEnabled
        val newVol = soundVolume ?: currSettings.soundVolume
        val newAiDiff = aiDifficultyName ?: currSettings.aiDifficultyName

        val newAiDelay = when (newAiDiff) {
            "Быстрый" -> 350L
            "Мастер" -> 1400L
            else -> 800L
        }

        val updatedSettings = currSettings.copy(
            isSoundEnabled = newSound,
            soundVolume = newVol,
            aiDifficultyName = newAiDiff,
            aiDelayMs = newAiDelay,
            showMoveHints = showMoveHints ?: currSettings.showMoveHints,
            colorTheme = colorTheme ?: currSettings.colorTheme,
            particleEffects = particleEffects ?: currSettings.particleEffects
        )

        _uiState.value = _uiState.value.copy(settings = updatedSettings)
        updateAudioEngine()
    }

    private fun updateAudioEngine() {
        val s = _uiState.value.settings
        SoundEffectsEngine.isSoundEnabled = s.isSoundEnabled
        SoundEffectsEngine.soundVolume = s.soundVolume
    }

    private fun getEnergy(color: PlayerColor): Int {
        val s = _uiState.value
        return when (color) {
            PlayerColor.RED -> s.redEnergy
            PlayerColor.BLUE -> s.blueEnergy
            PlayerColor.GREEN -> s.greenEnergy
        }
    }

    private fun addEnergy(color: PlayerColor, amount: Int) {
        val s = _uiState.value
        _uiState.value = when (color) {
            PlayerColor.RED -> s.copy(redEnergy = (s.redEnergy + amount).coerceAtMost(100))
            PlayerColor.BLUE -> s.copy(blueEnergy = (s.blueEnergy + amount).coerceAtMost(100))
            PlayerColor.GREEN -> s.copy(greenEnergy = (s.greenEnergy + amount).coerceAtMost(100))
        }
    }

    private fun deductEnergy(color: PlayerColor, amount: Int) {
        val s = _uiState.value
        _uiState.value = when (color) {
            PlayerColor.RED -> s.copy(redEnergy = (s.redEnergy - amount).coerceAtLeast(0))
            PlayerColor.BLUE -> s.copy(blueEnergy = (s.blueEnergy - amount).coerceAtLeast(0))
            PlayerColor.GREEN -> s.copy(greenEnergy = (s.greenEnergy - amount).coerceAtLeast(0))
        }
    }

    private fun recordCapture(player: PlayerColor, piece: PieceType) {
        val s = _uiState.value
        _uiState.value = when (player) {
            PlayerColor.RED -> s.copy(capturedRed = s.capturedRed + piece)
            PlayerColor.BLUE -> s.copy(capturedBlue = s.capturedBlue + piece)
            PlayerColor.GREEN -> s.copy(capturedGreen = s.capturedGreen + piece)
        }
    }

    private fun addLog(text: String) {
        val logs = _uiState.value.gameLogs.toMutableList()
        logs.add(0, text)
        if (logs.size > 20) logs.removeAt(logs.lastIndex)
        _uiState.value = _uiState.value.copy(gameLogs = logs)
    }
}
