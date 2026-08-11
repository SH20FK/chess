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

data class ExplosionEffect(
    val id: String,
    val row: Int,
    val col: Int,
    val radius: Float,
    val colorHex: Long,
    val timestamp: Long = System.currentTimeMillis()
)

data class GameState(
    val activePlayer: PlayerColor = PlayerColor.RED,
    val selectedPos: Position? = null,
    val validMoves: List<Position> = emptyList(),
    val activeWeapon: TacticalWeapon = TacticalWeapon.NONE,
    val isVsAi: Boolean = true,
    val redEnergy: Int = 50,
    val blueEnergy: Int = 50,
    val greenEnergy: Int = 50,
    val capturedRed: List<PieceType> = emptyList(),
    val capturedBlue: List<PieceType> = emptyList(),
    val capturedGreen: List<PieceType> = emptyList(),
    val gameLogs: List<String> = listOf("Битва Трех Держав началась! Захватывайте центр для получения ядерной энергии."),
    val activeExplosions: List<ExplosionEffect> = emptyList(),
    val winner: PlayerColor? = null,
    val isAiThinking: Boolean = false,
    val turnCount: Int = 1
)

class ChessViewModel : ViewModel() {

    val board = ChessBoard()

    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    fun resetGame() {
        board.resetBoard()
        _uiState.value = GameState(
            gameLogs = listOf("Новая партия началась! Нанесите ядерный удар по противнику.")
        )
    }

    fun toggleVsAi() {
        val newAiMode = !_uiState.value.isVsAi
        resetGame()
        _uiState.value = _uiState.value.copy(
            isVsAi = newAiMode,
            gameLogs = _uiState.value.gameLogs + if (newAiMode) "Режим: Игрок против 2 ИИ Ботов!" else "Режим: Локальная игра на 3 Игрока!"
        )
    }

    fun selectCell(row: Int, col: Int) {
        val state = _uiState.value
        if (state.winner != null || state.isAiThinking) return

        val cell = board.getCell(row, col) ?: return

        // Handle active tactical weapon targeting
        if (state.activeWeapon != TacticalWeapon.NONE) {
            executeWeaponTarget(row, col)
            return
        }

        // If a piece is already selected and clicked position is in valid moves
        val selPos = state.selectedPos
        if (selPos != null && state.validMoves.contains(Position(row, col))) {
            movePiece(selPos, Position(row, col))
            return
        }

        // Selecting a piece owned by the active player
        if (cell.piece != null && cell.piece.color == state.activePlayer) {
            val moves = board.getValidMoves(Position(row, col))
            _uiState.value = state.copy(
                selectedPos = Position(row, col),
                validMoves = moves
            )
            SoundEffectsEngine.playMoveSound()
        } else {
            // Deselect
            _uiState.value = state.copy(selectedPos = null, validMoves = emptyList())
        }
    }

    private fun movePiece(from: Position, to: Position) {
        val movingPiece = board.getCell(from.row, from.col)?.piece ?: return
        val targetCell = board.getCell(to.row, to.col)
        val captured = targetCell?.piece

        // Move piece
        board.setPiece(from.row, from.col, null)
        board.setPiece(to.row, to.col, movingPiece)

        var logMsg = "${movingPiece.color.displayName}: ${movingPiece.type.displayName} -> (${to.row + 1}, ${to.col + 1})"

        // Handle capture
        if (captured != null) {
            logMsg += " (Захвачен ${captured.color.displayName} ${captured.type.displayName})"
            SoundEffectsEngine.playCaptureSound()
            addEnergy(movingPiece.color, 25)
            recordCapture(movingPiece.color, captured.type)
        } else {
            SoundEffectsEngine.playMoveSound()
        }

        // Center control energy bonus
        if (to.row in 4..7 && to.col in 4..7) {
            addEnergy(movingPiece.color, 10)
            logMsg += " ⚡ Контроль центра (+10 Энергии)"
        }

        addLog(logMsg)

        // Clear selection and end turn
        _uiState.value = _uiState.value.copy(
            selectedPos = null,
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
            // Cancel weapon selection
            _uiState.value = state.copy(activeWeapon = TacticalWeapon.NONE)
        } else {
            _uiState.value = state.copy(
                activeWeapon = weapon,
                selectedPos = null,
                validMoves = emptyList()
            )
            addLog("Выберите цель на поле для удара [${when(weapon) { TacticalWeapon.NUKE -> "ЯДЕРНАЯ БОМБА" else -> "АВИАУДАР" }}]!")
        }
    }

    private fun executeWeaponTarget(centerR: Int, centerC: Int) {
        val state = _uiState.value
        val player = state.activePlayer
        val weapon = state.activeWeapon

        if (weapon == TacticalWeapon.NUKE) {
            deductEnergy(player, 100)
            SoundEffectsEngine.playNukeExplosionSound()

            // Destroy 3x3 area
            val affectedCells = mutableListOf<Position>()
            for (dr in -1..1) {
                for (dc in -1..1) {
                    val r = centerR + dr
                    val c = centerC + dc
                    if (r in 0 until board.rows && c in 0 until board.cols) {
                        board.setPiece(r, c, null) // vaporize piece
                        board.setCellState(r, c, CellState.CRATER_DESTROYED) // create crater
                        affectedCells.add(Position(r, c))
                    }
                }
            }

            val exp = ExplosionEffect(
                id = "nuke_${System.currentTimeMillis()}",
                row = centerR,
                col = centerC,
                radius = 3.5f,
                colorHex = 0xFFFF4400
            )

            _uiState.value = _uiState.value.copy(
                activeWeapon = TacticalWeapon.NONE,
                activeExplosions = _uiState.value.activeExplosions + exp
            )

            addLog("🚀 ${player.displayName} нанёс ЯДЕРНЫЙ УДАР по секции (${centerR + 1}, ${centerC + 1})! Клетки уничтожены в кратер!")

        } else if (weapon == TacticalWeapon.AIRSTRIKE) {
            deductEnergy(player, 50)
            SoundEffectsEngine.playAirstrikeSound()

            // Bomb row
            for (c in 0 until board.cols) {
                val p = board.getCell(centerR, c)?.piece
                if (p != null) {
                    board.setPiece(centerR, c, null)
                    board.setCellState(centerR, c, CellState.RADIOACTIVE_HAZARD)
                }
            }

            val exp = ExplosionEffect(
                id = "airstrike_${System.currentTimeMillis()}",
                row = centerR,
                col = centerC,
                radius = 2.0f,
                colorHex = 0xFFFFD700
            )

            _uiState.value = _uiState.value.copy(
                activeWeapon = TacticalWeapon.NONE,
                activeExplosions = _uiState.value.activeExplosions + exp
            )

            addLog("✈️ ${player.displayName} вызвал АВИАУДАР по горизонтали ${centerR + 1}! Вражеские позиции разбомблены!")
        }

        // Remove explosion graphics after 1.5s
        viewModelScope.launch {
            delay(1500)
            _uiState.value = _uiState.value.copy(
                activeExplosions = _uiState.value.activeExplosions.filter { it.timestamp > System.currentTimeMillis() - 1500 }
            )
        }

        checkWinAndAdvanceTurn()
    }

    private fun checkWinAndAdvanceTurn() {
        // Check alive players
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
            addLog("🏆 ПОБЕДА! ${winner.displayName} уничтожил всех соперников и захватил господство!")
            return
        }

        // Advance next player (skip eliminated ones)
        var nextPlayer = _uiState.value.activePlayer.next()
        while (!isPlayerAlive(nextPlayer)) {
            nextPlayer = nextPlayer.next()
        }

        // Add turn energy
        addEnergy(nextPlayer, 15)

        val newTurn = _uiState.value.turnCount + 1
        _uiState.value = _uiState.value.copy(
            activePlayer = nextPlayer,
            turnCount = newTurn
        )

        // Trigger AI if applicable
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
            delay(800) // Realistic AI thinking delay

            val aiEnergy = getEnergy(aiColor)

            // AI decision: 25% chance to drop a Nuke if energy is 100%
            if (aiEnergy >= 100 && Random.nextFloat() < 0.35f) {
                // Target opponent king or heavy cluster
                val targetR = Random.nextInt(2, 10)
                val targetC = Random.nextInt(2, 10)
                _uiState.value = _uiState.value.copy(activeWeapon = TacticalWeapon.NUKE)
                executeWeaponTarget(targetR, targetC)
                _uiState.value = _uiState.value.copy(isAiThinking = false)
                return@launch
            }

            // Find all possible valid moves for AI
            val allAiPieces = mutableListOf<Pair<Position, List<Position>>>()
            for (r in 0 until board.rows) {
                for (c in 0 until board.cols) {
                    val p = board.getCell(r, c)?.piece
                    if (p != null && p.color == aiColor) {
                        val moves = board.getValidMoves(Position(r, c))
                        if (moves.isNotEmpty()) {
                            allAiPieces.add(Pair(Position(r, c), moves))
                        }
                    }
                }
            }

            if (allAiPieces.isNotEmpty()) {
                // Prefer capture moves
                val captureMoves = mutableListOf<Pair<Position, Position>>()
                val regularMoves = mutableListOf<Pair<Position, Position>>()

                for ((from, moves) in allAiPieces) {
                    for (to in moves) {
                        val targetPiece = board.getCell(to.row, to.col)?.piece
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
                addLog("🤖 ИИ ${aiColor.displayName} пропустил ход (нет доступных ходов).")
                checkWinAndAdvanceTurn()
            }

            _uiState.value = _uiState.value.copy(isAiThinking = false)
        }
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
        logs.add(0, text) // Most recent first
        if (logs.size > 20) logs.removeAt(logs.lastIndex)
        _uiState.value = _uiState.value.copy(gameLogs = logs)
    }
}
