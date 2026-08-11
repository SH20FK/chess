package com.example.model

enum class PlayerColor(val displayName: String, val hexColor: Long, val tag: String) {
    RED("Красный (Альфа)", 0xFFFF3333, "RED"),
    BLUE("Синий (Бета)", 0xFF3388FF, "BLUE"),
    GREEN("Зелёный (Гамма)", 0xFF22CC66, "GREEN");

    fun next(): PlayerColor {
        return when (this) {
            RED -> BLUE
            BLUE -> GREEN
            GREEN -> RED
        }
    }
}

enum class PieceType(val displayName: String, val symbol: String, val value: Int) {
    PAWN("Шоколадный Рогалик", "🥐", 1),
    KNIGHT("Конь", "♘", 3),
    BISHOP("Слон", "♗", 3),
    ROOK("Ладья-Башня", "♖", 5),
    QUEEN("Ферзь", "♕", 9),
    KING("Король", "♔", 1000)
}

data class ChessPiece(
    val id: String,
    val type: PieceType,
    val color: PlayerColor,
    val isFortified: Boolean = false
)

data class Position(val row: Int, val col: Int)

enum class CellState {
    NORMAL,
    CRATER_DESTROYED,
    RADIOACTIVE_HAZARD
}

data class BoardCell(
    val pos: Position,
    val piece: ChessPiece? = null,
    val state: CellState = CellState.NORMAL,
    val Sector: Int = 0 // 0: Red, 1: Blue, 2: Green, 3: Center Chaos
)
