package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.viewmodel.ExplosionEffect
import com.example.viewmodel.TacticalWeapon
import kotlin.math.sin

@OptIn(ExperimentalTextApi::class)
@Composable
fun ChessBoardCanvas(
    board: ChessBoard,
    selectedPos: Position?,
    validMoves: List<Position>,
    activeWeapon: TacticalWeapon,
    activeExplosions: List<ExplosionEffect>,
    activePlayer: PlayerColor,
    onCellClick: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // Pulse animation for selection and weapon target
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(activeWeapon, selectedPos) {
                detectTapGestures { offset ->
                    val tileSize = size.width / board.cols.toFloat()
                    val c = (offset.x / tileSize).toInt().coerceIn(0, board.cols - 1)
                    val r = (offset.y / tileSize).toInt().coerceIn(0, board.rows - 1)
                    onCellClick(r, c)
                }
            }
    ) {
        val tileSize = size.width / board.cols.toFloat()

        // 1. Draw Grid Cells
        for (r in 0 until board.rows) {
            for (c in 0 until board.cols) {
                val cell = board.getCell(r, c) ?: continue
                val topLeft = Offset(c * tileSize, r * tileSize)
                val cellSize = Size(tileSize, tileSize)

                // Base tile color according to sector and checkerboard
                val isLight = (r + c) % 2 == 0
                val tileColor = when (cell.Sector) {
                    0 -> if (isLight) Color(0xFF3B1E1E) else Color(0xFF281313) // Red base
                    1 -> if (isLight) Color(0xFF1E2B3B) else Color(0xFF131A28) // Blue base
                    2 -> if (isLight) Color(0xFF1E3B2B) else Color(0xFF13281A) // Green base
                    else -> if (isLight) Color(0xFF323242) else Color(0xFF222230) // Center Tactical Zone
                }

                drawRect(color = tileColor, topLeft = topLeft, size = cellSize)

                // Draw cell borders
                drawRect(
                    color = Color(0x33FFFFFF),
                    topLeft = topLeft,
                    size = cellSize,
                    style = Stroke(width = 1f)
                )

                // Render Destructible Crater state
                if (cell.state == CellState.CRATER_DESTROYED) {
                    drawRect(color = Color(0xFF110A0A), topLeft = topLeft, size = cellSize)
                    drawCircle(
                        color = Color(0xFFE65100),
                        center = Offset(topLeft.x + tileSize / 2, topLeft.y + tileSize / 2),
                        radius = tileSize * 0.4f,
                        style = Stroke(width = 3f)
                    )
                    drawCircle(
                        color = Color(0xFF212121),
                        center = Offset(topLeft.x + tileSize / 2, topLeft.y + tileSize / 2),
                        radius = tileSize * 0.35f
                    )
                } else if (cell.state == CellState.RADIOACTIVE_HAZARD) {
                    drawRect(color = Color(0x44FFD700), topLeft = topLeft, size = cellSize)
                }

                // Render Highlight for Selected Tile
                if (selectedPos != null && selectedPos.row == r && selectedPos.col == c) {
                    drawRect(
                        color = Color(0xAAFFD700),
                        topLeft = topLeft,
                        size = cellSize,
                        style = Stroke(width = 6f)
                    )
                    drawRect(
                        color = Color(0x33FFD700),
                        topLeft = topLeft,
                        size = cellSize
                    )
                }

                // Render Valid Move Indicators
                if (validMoves.contains(Position(r, c))) {
                    if (cell.piece != null) {
                        // Capture indicator
                        drawRect(
                            color = Color(0xAAFF3333),
                            topLeft = topLeft,
                            size = cellSize,
                            style = Stroke(width = 5f)
                        )
                    } else {
                        // Move dot indicator
                        drawCircle(
                            color = Color(0xBB00E676),
                            center = Offset(topLeft.x + tileSize / 2, topLeft.y + tileSize / 2),
                            radius = tileSize * 0.2f
                        )
                    }
                }

                // 2. Render Pieces
                cell.piece?.let { piece ->
                    drawPiece(
                        scope = this,
                        textMeasurer = textMeasurer,
                        piece = piece,
                        topLeft = topLeft,
                        tileSize = tileSize
                    )
                }
            }
        }

        // 3. Render Active Weapon Crosshair Overlay
        if (activeWeapon != TacticalWeapon.NONE) {
            val strokeColor = if (activeWeapon == TacticalWeapon.NUKE) Color(0xFFFF3333) else Color(0xFFFFD700)
            drawRect(
                color = strokeColor.copy(alpha = pulseAlpha * 0.3f),
                topLeft = Offset(0f, 0f),
                size = Size(size.width, size.height)
            )
        }

        // 4. Render Explosion Shader Effects
        for (exp in activeExplosions) {
            val center = Offset((exp.col + 0.5f) * tileSize, (exp.row + 0.5f) * tileSize)
            val rad = exp.radius * tileSize

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(exp.colorHex), Color.Transparent),
                    center = center,
                    radius = rad
                ),
                center = center,
                radius = rad
            )

            drawCircle(
                color = Color(exp.colorHex),
                center = center,
                radius = rad,
                style = Stroke(width = 8f)
            )
        }
    }
}

private fun drawPiece(
    scope: DrawScope,
    textMeasurer: TextMeasurer,
    piece: ChessPiece,
    topLeft: Offset,
    tileSize: Float
) {
    scope.apply {
        val center = Offset(topLeft.x + tileSize / 2, topLeft.y + tileSize / 2)
        val pieceColor = Color(piece.color.hexColor)

        // Draw background badge glow
        drawCircle(
            color = pieceColor.copy(alpha = 0.25f),
            center = center,
            radius = tileSize * 0.42f
        )
        drawCircle(
            color = pieceColor,
            center = center,
            radius = tileSize * 0.38f,
            style = Stroke(width = 3f)
        )

        if (piece.type == PieceType.PAWN) {
            // Draw Chocolate Cornet Pawn (Шоколадный Рогалик) 🥐
            val cornetPath = Path().apply {
                val cx = center.x
                val cy = center.y
                val r = tileSize * 0.28f
                moveTo(cx - r, cy + r * 0.6f)
                cubicTo(cx - r, cy - r, cx + r * 0.5f, cy - r * 1.2f, cx + r, cy - r * 0.2f)
                cubicTo(cx + r * 1.1f, cy + r * 0.6f, cx + r * 0.2f, cy + r, cx - r, cy + r * 0.6f)
                close()
            }

            // Crust gradient (Gold/Chocolate)
            drawPath(
                path = cornetPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFD7CCC8), Color(0xFF795548), Color(0xFF4E342E))
                )
            )
            drawPath(
                path = cornetPath,
                color = pieceColor,
                style = Stroke(width = 2.5f)
            )

            // Chocolate filling dip at head
            drawCircle(
                color = Color(0xFF3E2723),
                center = Offset(center.x + tileSize * 0.12f, center.y - tileSize * 0.05f),
                radius = tileSize * 0.1f
            )
        } else {
            // Standard / Tactical Chess Symbols
            val textLayoutResult = textMeasurer.measure(
                text = AnnotatedString(piece.type.symbol),
                style = TextStyle(
                    color = pieceColor,
                    fontSize = (tileSize * 0.48f).sp
                )
            )

            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    center.x - textLayoutResult.size.width / 2f,
                    center.y - textLayoutResult.size.height / 2f
                )
            )
        }
    }
}
