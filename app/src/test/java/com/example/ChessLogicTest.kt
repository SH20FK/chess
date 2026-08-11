package com.example

import com.example.model.ChessBoard
import com.example.model.PieceType
import com.example.model.PlayerColor
import com.example.model.Position
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
        val pawnCell = board.getCell(1, 2)
        assertNotNull(pawnCell?.piece)
        assertEquals(PieceType.PAWN, pawnCell?.piece?.type)

        val moves = board.getValidMoves(Position(1, 2))
        assertTrue(moves.isNotEmpty())
    }

    @Test
    fun testBasicMath() {
        assertEquals(4, 2 + 2)
    }
}
