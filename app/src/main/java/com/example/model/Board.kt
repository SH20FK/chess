package com.example.model

import kotlin.math.abs

class ChessBoard {
    val rows = 12
    val cols = 12

    // Grid representing the board
    private val cells = Array(rows) { r ->
        Array(cols) { c ->
            BoardCell(
                pos = Position(r, c),
                piece = null,
                state = CellState.NORMAL,
                Sector = getSectorForPosition(r, c)
            )
        }
    }

    init {
        resetBoard()
    }

    private fun getSectorForPosition(r: Int, c: Int): Int {
        return when {
            r < 4 && c in 2..9 -> 0 // Red sector
            c < 6 && r in 6..11 -> 1 // Blue sector
            c >= 6 && r in 6..11 -> 2 // Green sector
            else -> 3 // Center / Neutral
        }
    }

    fun getCell(r: Int, c: Int): BoardCell? {
        if (r !in 0 until rows || c !in 0 until cols) return null
        return cells[r][c]
    }

    fun setCellState(r: Int, c: Int, state: CellState) {
        if (r in 0 until rows && c in 0 until cols) {
            cells[r][c] = cells[r][c].copy(state = state)
        }
    }

    fun setPiece(r: Int, c: Int, piece: ChessPiece?) {
        if (r in 0 until rows && c in 0 until cols) {
            cells[r][c] = cells[r][c].copy(piece = piece)
        }
    }

    fun resetBoard() {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                cells[r][c] = BoardCell(Position(r, c), null, CellState.NORMAL, getSectorForPosition(r, c))
            }
        }

        // Initialize Red Player (Bottom, Sector 0)
        setupPlayerArmy(PlayerColor.RED, mainRow = 0, pawnRow = 1, startCol = 2)

        // Initialize Blue Player (Top Left, Sector 1)
        setupPlayerArmy(PlayerColor.BLUE, mainRow = 11, pawnRow = 10, startCol = 0)

        // Initialize Green Player (Top Right, Sector 2)
        setupPlayerArmy(PlayerColor.GREEN, mainRow = 11, pawnRow = 10, startCol = 4)
    }

    private fun setupPlayerArmy(color: PlayerColor, mainRow: Int, pawnRow: Int, startCol: Int) {
        val piecesOrder = listOf(
            PieceType.ROOK,
            PieceType.KNIGHT,
            PieceType.BISHOP,
            PieceType.QUEEN,
            PieceType.KING,
            PieceType.BISHOP,
            PieceType.KNIGHT,
            PieceType.ROOK
        )

        for (i in piecesOrder.indices) {
            val col = startCol + i
            if (col in 0 until cols) {
                val piece = ChessPiece("${color.name}_${piecesOrder[i]}_$i", piecesOrder[i], color)
                setPiece(mainRow, col, piece)
            }
        }

        // Pawns (Chocolate Cornets)
        for (i in 0 until 8) {
            val col = startCol + i
            if (col in 0 until cols) {
                val pawn = ChessPiece("${color.name}_PAWN_$i", PieceType.PAWN, color)
                setPiece(pawnRow, col, pawn)
            }
        }
    }

    fun getValidMoves(pos: Position): List<Position> {
        val cell = getCell(pos.row, pos.col) ?: return emptyList()
        val piece = cell.piece ?: return emptyList()
        if (cell.state == CellState.CRATER_DESTROYED) return emptyList()

        val validMoves = mutableListOf<Position>()

        when (piece.type) {
            PieceType.PAWN -> getPawnMoves(pos, piece.color, validMoves)
            PieceType.KNIGHT -> getKnightMoves(pos, piece.color, validMoves)
            PieceType.BISHOP -> getSlidingMoves(pos, piece.color, validMoves, listOf(Pair(1,1), Pair(1,-1), Pair(-1,1), Pair(-1,-1)))
            PieceType.ROOK -> getSlidingMoves(pos, piece.color, validMoves, listOf(Pair(1,0), Pair(-1,0), Pair(0,1), Pair(0,-1)))
            PieceType.QUEEN -> getSlidingMoves(pos, piece.color, validMoves, listOf(
                Pair(1,0), Pair(-1,0), Pair(0,1), Pair(0,-1),
                Pair(1,1), Pair(1,-1), Pair(-1,1), Pair(-1,-1)
            ))
            PieceType.KING -> getKingMoves(pos, piece.color, validMoves)
        }

        return validMoves
    }

    private fun getPawnMoves(pos: Position, color: PlayerColor, moves: MutableList<Position>) {
        val forwardDir = when (color) {
            PlayerColor.RED -> 1
            PlayerColor.BLUE -> -1
            PlayerColor.GREEN -> -1
        }

        val nextR = pos.row + forwardDir
        if (isValidStep(nextR, pos.col, color, canCapture = false)) {
            moves.add(Position(nextR, pos.col))
        }

        // Diagonal captures
        for (dc in listOf(-1, 1)) {
            val capC = pos.col + dc
            val targetCell = getCell(nextR, capC)
            if (targetCell != null && targetCell.state != CellState.CRATER_DESTROYED) {
                if (targetCell.piece != null && targetCell.piece.color != color) {
                    moves.add(Position(nextR, capC))
                }
            }
        }
    }

    private fun getKnightMoves(pos: Position, color: PlayerColor, moves: MutableList<Position>) {
        val offsets = listOf(
            Pair(2, 1), Pair(2, -1), Pair(-2, 1), Pair(-2, -1),
            Pair(1, 2), Pair(1, -2), Pair(-1, 2), Pair(-1, -2)
        )
        for ((dr, dc) in offsets) {
            val nr = pos.row + dr
            val nc = pos.col + dc
            if (isValidStep(nr, nc, color, canCapture = true)) {
                moves.add(Position(nr, nc))
            }
        }
    }

    private fun getSlidingMoves(
        pos: Position,
        color: PlayerColor,
        moves: MutableList<Position>,
        dirs: List<Pair<Int, Int>>
    ) {
        for ((dr, dc) in dirs) {
            var currR = pos.row + dr
            var currC = pos.col + dc
            while (currR in 0 until rows && currC in 0 until cols) {
                val targetCell = getCell(currR, currC) ?: break
                if (targetCell.state == CellState.CRATER_DESTROYED) break // Blocked by crater

                if (targetCell.piece == null) {
                    moves.add(Position(currR, currC))
                } else {
                    if (targetCell.piece.color != color) {
                        moves.add(Position(currR, currC)) // Capture
                    }
                    break // Stop sliding after encountering any piece
                }
                currR += dr
                currC += dc
            }
        }
    }

    private fun getKingMoves(pos: Position, color: PlayerColor, moves: MutableList<Position>) {
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val nr = pos.row + dr
                val nc = pos.col + dc
                if (isValidStep(nr, nc, color, canCapture = true)) {
                    moves.add(Position(nr, nc))
                }
            }
        }
    }

    private fun isValidStep(r: Int, c: Int, color: PlayerColor, canCapture: Boolean): Boolean {
        val targetCell = getCell(r, c) ?: return false
        if (targetCell.state == CellState.CRATER_DESTROYED) return false
        if (targetCell.piece == null) return true
        return canCapture && targetCell.piece.color != color
    }

    fun isKingAlive(color: PlayerColor): Boolean {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val piece = cells[r][c].piece
                if (piece != null && piece.color == color && piece.type == PieceType.KING) {
                    return true
                }
            }
        }
        return false
    }
}
