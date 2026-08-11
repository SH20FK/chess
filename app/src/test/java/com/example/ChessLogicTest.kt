package com.example

import com.example.model.ChessBoard
import com.example.model.HexPos
import com.example.model.PieceType
import com.example.model.PlayerColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChessLogicTest {

    @Test
    fun testBoardInitialization() {
        val board = ChessBoard()
        assertTrue(board.isKingAlive(PlayerColor.RED))
        assertTrue(board.isKingAlive(PlayerColor.BLUE))
        assertTrue(board.isKingAlive(PlayerColor.GREEN))
    }

    @Test
    fun testPawnValidMoves() {
        val board = ChessBoard()
        val pawnPos = HexPos(0, 3)
        val pawnCell = board.getCell(pawnPos)
        assertNotNull(pawnCell?.piece)
        assertEquals(PieceType.PAWN, pawnCell?.piece?.type)

        val moves = board.getValidMoves(pawnPos)
        assertTrue(moves.isNotEmpty())
    }

    @Test
    fun testBasicMath() {
        assertEquals(4, 2 + 2)
    }
}
