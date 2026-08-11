package com.example.model

import kotlin.math.abs

class ChessBoard {
    val boardRadius = 5 // Hexagon radius 5 -> 91 cells total
    private val cells = mutableMapOf<HexPos, BoardCell>()

    init {
        resetBoard()
    }

    fun getAllCells(): Collection<BoardCell> = cells.values

    fun getCell(pos: HexPos): BoardCell? = cells[pos]

    fun setCellState(pos: HexPos, state: CellState) {
        val current = cells[pos] ?: return
        cells[pos] = current.copy(state = state)
    }

    fun setPiece(pos: HexPos, piece: ChessPiece?) {
        val current = cells[pos] ?: return
        cells[pos] = current.copy(piece = piece)
    }

    fun resetBoard() {
        cells.clear()

        // Generate all hex cells within boardRadius
        for (q in -boardRadius..boardRadius) {
            val r1 = maxOf(-boardRadius, -q - boardRadius)
            val r2 = minOf(boardRadius, -q + boardRadius)
            for (r in r1..r2) {
                val pos = HexPos(q, r)
                val sector = determineSector(pos)
                cells[pos] = BoardCell(pos = pos, piece = null, state = CellState.NORMAL, sector = sector)
            }
        }

        // Setup 3 armies
        setupRedArmy()
        setupBlueArmy()
        setupGreenArmy()
    }

    private fun determineSector(pos: HexPos): Int {
        val dist = pos.distanceTo(HexPos(0, 0))
        if (dist <= 1) return 3 // Center Tactical Zone

        return when {
            pos.r >= 2 && pos.s <= 1 -> 0 // Red Sector (South)
            pos.q <= -2 && pos.r <= 1 -> 1 // Blue Sector (North-West)
            pos.s >= 2 && pos.q <= 1 -> 2 // Green Sector (North-East)
            else -> 3
        }
    }

    private fun setupRedArmy() {
        // Red back row at r = 4, 5
        val backRow = listOf(
            HexPos(-2, 5), HexPos(-1, 5), HexPos(0, 5), HexPos(1, 4), HexPos(0, 4), HexPos(-1, 4), HexPos(2, 3)
        )
        val pieces = listOf(
            PieceType.ROOK, PieceType.KNIGHT, PieceType.QUEEN, PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        )
        for (i in pieces.indices) {
            if (i < backRow.size) {
                val pos = backRow[i]
                setPiece(pos, ChessPiece("RED_${pieces[i]}_$i", pieces[i], PlayerColor.RED))
            }
        }

        // Red pawns at r = 3
        val pawnRow = listOf(
            HexPos(-2, 4), HexPos(-1, 3), HexPos(0, 3), HexPos(1, 3), HexPos(2, 2), HexPos(-3, 5), HexPos(3, 2)
        )
        for (i in pawnRow.indices) {
            setPiece(pawnRow[i], ChessPiece("RED_PAWN_$i", PieceType.PAWN, PlayerColor.RED))
        }
    }

    private fun setupBlueArmy() {
        // Blue back row at q = -4, -5
        val backRow = listOf(
            HexPos(-5, 2), HexPos(-5, 1), HexPos(-5, 0), HexPos(-4, -1), HexPos(-4, 0), HexPos(-4, 1), HexPos(-3, -2)
        )
        val pieces = listOf(
            PieceType.ROOK, PieceType.KNIGHT, PieceType.QUEEN, PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        )
        for (i in pieces.indices) {
            if (i < backRow.size) {
                val pos = backRow[i]
                setPiece(pos, ChessPiece("BLUE_${pieces[i]}_$i", pieces[i], PlayerColor.BLUE))
            }
        }

        // Blue pawns
        val pawnRow = listOf(
            HexPos(-4, 2), HexPos(-3, 1), HexPos(-3, 0), HexPos(-3, -1), HexPos(-2, -2), HexPos(-5, 3), HexPos(-2, -3)
        )
        for (i in pawnRow.indices) {
            setPiece(pawnRow[i], ChessPiece("BLUE_PAWN_$i", PieceType.PAWN, PlayerColor.BLUE))
        }
    }

    private fun setupGreenArmy() {
        // Green back row at s = 4, 5
        val backRow = listOf(
            HexPos(3, -5), HexPos(4, -5), HexPos(5, -5), HexPos(5, -4), HexPos(4, -4), HexPos(3, -4), HexPos(2, -5)
        )
        val pieces = listOf(
            PieceType.ROOK, PieceType.KNIGHT, PieceType.QUEEN, PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        )
        for (i in pieces.indices) {
            if (i < backRow.size) {
                val pos = backRow[i]
                setPiece(pos, ChessPiece("GREEN_${pieces[i]}_$i", pieces[i], PlayerColor.GREEN))
            }
        }

        // Green pawns
        val pawnRow = listOf(
            HexPos(2, -4), HexPos(3, -3), HexPos(4, -3), HexPos(3, -2), HexPos(2, -2), HexPos(1, -3), HexPos(5, -3)
        )
        for (i in pawnRow.indices) {
            setPiece(pawnRow[i], ChessPiece("GREEN_PAWN_$i", PieceType.PAWN, PlayerColor.GREEN))
        }
    }

    // Hex Direction Vectors
    val orthDirs = listOf(
        HexPos(1, 0), HexPos(1, -1), HexPos(0, -1), HexPos(-1, 0), HexPos(-1, 1), HexPos(0, 1)
    )

    val diagDirs = listOf(
        HexPos(2, -1), HexPos(1, 1), HexPos(-1, 2), HexPos(-2, 1), HexPos(-1, -1), HexPos(1, -2)
    )

    val knightOffsets = listOf(
        HexPos(2, 1), HexPos(1, 2), HexPos(-1, 3), HexPos(-2, 3), HexPos(-3, 2), HexPos(-3, 1),
        HexPos(-2, -1), HexPos(-1, -2), HexPos(1, -3), HexPos(2, -3), HexPos(3, -2), HexPos(3, -1)
    )

    fun getValidMoves(pos: HexPos): List<HexPos> {
        val cell = getCell(pos) ?: return emptyList()
        val piece = cell.piece ?: return emptyList()
        if (cell.state == CellState.CRATER_DESTROYED) return emptyList()

        val validMoves = mutableListOf<HexPos>()

        when (piece.type) {
            PieceType.PAWN -> getPawnMoves(pos, piece.color, validMoves)
            PieceType.KNIGHT -> getKnightMoves(pos, piece.color, validMoves)
            PieceType.BISHOP -> getSlidingMoves(pos, piece.color, validMoves, diagDirs)
            PieceType.ROOK -> getSlidingMoves(pos, piece.color, validMoves, orthDirs)
            PieceType.QUEEN -> getSlidingMoves(pos, piece.color, validMoves, orthDirs + diagDirs)
            PieceType.KING -> getKingMoves(pos, piece.color, validMoves)
        }

        return validMoves
    }

    private fun getPawnMoves(pos: HexPos, color: PlayerColor, moves: MutableList<HexPos>) {
        // Pawns advance toward center (0,0)
        val forwardDirs = when (color) {
            PlayerColor.RED -> listOf(HexPos(0, -1), HexPos(1, -1), HexPos(-1, 0))
            PlayerColor.BLUE -> listOf(HexPos(1, 0), HexPos(1, -1), HexPos(0, 1))
            PlayerColor.GREEN -> listOf(HexPos(-1, 0), HexPos(0, 1), HexPos(-1, 1))
        }

        for (dir in forwardDirs) {
            val targetPos = HexPos(pos.q + dir.q, pos.r + dir.r)
            val targetCell = getCell(targetPos) ?: continue
            if (targetCell.state == CellState.CRATER_DESTROYED) continue

            if (targetCell.piece == null) {
                moves.add(targetPos)
            } else if (targetCell.piece.color != color) {
                // Capture
                moves.add(targetPos)
            }
        }
    }

    private fun getKnightMoves(pos: HexPos, color: PlayerColor, moves: MutableList<HexPos>) {
        for (offset in knightOffsets) {
            val targetPos = HexPos(pos.q + offset.q, pos.r + offset.r)
            if (isValidStep(targetPos, color, canCapture = true)) {
                moves.add(targetPos)
            }
        }
    }

    private fun getSlidingMoves(
        pos: HexPos,
        color: PlayerColor,
        moves: MutableList<HexPos>,
        dirs: List<HexPos>
    ) {
        for (dir in dirs) {
            var step = 1
            while (true) {
                val targetPos = HexPos(pos.q + dir.q * step, pos.r + dir.r * step)
                val targetCell = getCell(targetPos) ?: break
                if (targetCell.state == CellState.CRATER_DESTROYED) break

                if (targetCell.piece == null) {
                    moves.add(targetPos)
                } else {
                    if (targetCell.piece.color != color) {
                        moves.add(targetPos) // Capture
                    }
                    break // Blocked after encountering piece
                }
                step++
            }
        }
    }

    private fun getKingMoves(pos: HexPos, color: PlayerColor, moves: MutableList<HexPos>) {
        val allDirs = orthDirs + diagDirs
        for (dir in allDirs) {
            val targetPos = HexPos(pos.q + dir.q, pos.r + dir.r)
            if (isValidStep(targetPos, color, canCapture = true)) {
                moves.add(targetPos)
            }
        }
    }

    private fun isValidStep(targetPos: HexPos, color: PlayerColor, canCapture: Boolean): Boolean {
        val targetCell = getCell(targetPos) ?: return false
        if (targetCell.state == CellState.CRATER_DESTROYED) return false
        if (targetCell.piece == null) return true
        return canCapture && targetCell.piece.color != color
    }

    fun isKingAlive(color: PlayerColor): Boolean {
        return cells.values.any { it.piece?.color == color && it.piece?.type == PieceType.KING }
    }
}
