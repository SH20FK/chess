package com.example.model

enum class PlayerColor(val displayName: String, val hexColor: Long, val containerColor: Long, val tag: String) {
    RED("Розовый (Альфа)", 0xFFE57373, 0xFFFFEBEE, "RED"),
    BLUE("Небесный (Бета)", 0xFF64B5F6, 0xFFE3F2FD, "BLUE"),
    GREEN("Мятный (Гамма)", 0xFF81C784, 0xFFE8F5E9, "GREEN");

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
    val color: PlayerColor
)

// Cube/Axial Hexagonal Coordinates (q + r + s = 0)
data class HexPos(val q: Int, val r: Int) {
    val s: Int get() = -q - r

    fun distanceTo(other: HexPos): Int {
        return (kotlin.math.abs(q - other.q) + kotlin.math.abs(r - other.r) + kotlin.math.abs(s - other.s)) / 2
    }

    fun isWithinRadius(radius: Int): Boolean {
        return distanceTo(HexPos(0, 0)) <= radius
    }
}

enum class CellState {
    NORMAL,
    CRATER_DESTROYED,
    RADIOACTIVE_HAZARD
}

data class BoardCell(
    val pos: HexPos,
    val piece: ChessPiece? = null,
    val state: CellState = CellState.NORMAL,
    val sector: Int = 0 // 0: Red, 1: Blue, 2: Green, 3: Center Tactical
)
