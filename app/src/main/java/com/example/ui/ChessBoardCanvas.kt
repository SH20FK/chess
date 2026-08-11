package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.HexExplosionEffect
import com.example.viewmodel.TacticalWeapon
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalTextApi::class)
@Composable
fun ChessBoardCanvas(
    board: ChessBoard,
    selectedHex: HexPos?,
    validMoves: List<HexPos>,
    activeWeapon: TacticalWeapon,
    activeExplosions: List<HexExplosionEffect>,
    activePlayer: PlayerColor,
    colorTheme: String,
    onHexClick: (HexPos) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    val infiniteTransition = rememberInfiniteTransition(label = "hexPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(board, selectedHex, activeWeapon) {
                detectTapGestures { tapOffset ->
                    val width = size.width
                    val height = size.height
                    val centerX = width / 2f
                    val centerY = height / 2f
                    val hexRadius = width / 20f

                    var closestHex: HexPos? = null
                    var minDistance = Float.MAX_VALUE

                    for (cell in board.getAllCells()) {
                        val pos = cell.pos
                        val px = centerX + hexRadius * sqrt(3f) * (pos.q + pos.r / 2.0f)
                        val py = centerY + hexRadius * 1.5f * pos.r

                        val dx = tapOffset.x - px
                        val dy = tapOffset.y - py
                        val dist = dx * dx + dy * dy

                        if (dist < minDistance && dist <= (hexRadius * 1.2f) * (hexRadius * 1.2f)) {
                            minDistance = dist
                            closestHex = pos
                        }
                    }

                    closestHex?.let { onHexClick(it) }
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val centerY = height / 2f
        val hexRadius = width / 20f

        // Draw Board Background Surface
        drawCircle(
            color = Color(0xFFF2ECE4),
            center = Offset(centerX, centerY),
            radius = width * 0.48f
        )

        // 1. Draw Hex Cells
        for (cell in board.getAllCells()) {
            val pos = cell.pos
            val px = centerX + hexRadius * sqrt(3f) * (pos.q + pos.r / 2.0f)
            val py = centerY + hexRadius * 1.5f * pos.r

            val hexPath = createPointyHexPath(px, py, hexRadius * 0.94f)

            // Base Pastel Color
            val colorIdx = ((pos.q - pos.r) % 3 + 3) % 3
            val baseColor = when (colorTheme) {
                "Мятный Бриз" -> when (colorIdx) {
                    0 -> Color(0xFFF1F8E9)
                    1 -> Color(0xFFDCEDC8)
                    else -> Color(0xFFC5E1A5)
                }
                "Лавандовый Сон" -> when (colorIdx) {
                    0 -> Color(0xFFF3E5F5)
                    1 -> Color(0xFFE1BEE7)
                    else -> Color(0xFFD1C4E9)
                }
                else -> when (colorIdx) { // "Пастельный Песок"
                    0 -> HexTileLight
                    1 -> HexTileMedium
                    else -> HexTileDark
                }
            }

            drawPath(path = hexPath, color = baseColor)

            // Sector Accent Overlay
            val sectorTint = when (cell.sector) {
                0 -> Color(0x18E57373) // Red
                1 -> Color(0x1864B5F6) // Blue
                2 -> Color(0x1881C784) // Green
                else -> Color(0x20FFB74D) // Center
            }
            drawPath(path = hexPath, color = sectorTint)

            // Border
            drawPath(
                path = hexPath,
                color = Color(0x22000000),
                style = Stroke(width = 1.5f)
            )

            // Crater state
            if (cell.state == CellState.CRATER_DESTROYED) {
                drawPath(path = hexPath, color = Color(0xFFD7CCC8))
                drawCircle(
                    color = Color(0xFF8D6E63),
                    center = Offset(px, py),
                    radius = hexRadius * 0.4f,
                    style = Stroke(width = 3f)
                )
            } else if (cell.state == CellState.RADIOACTIVE_HAZARD) {
                drawPath(path = hexPath, color = Color(0x55FFF59D))
            }

            // Selection Highlight
            if (selectedHex == pos) {
                drawPath(path = hexPath, color = Color(0x55FFB74D))
                drawPath(
                    path = hexPath,
                    color = Color(0xFFE65100),
                    style = Stroke(width = 4f)
                )
            }

            // Valid Move Hint
            if (validMoves.contains(pos)) {
                if (cell.piece != null) {
                    drawPath(
                        path = hexPath,
                        color = Color(0x88E57373),
                        style = Stroke(width = 4f)
                    )
                } else {
                    drawCircle(
                        color = Color(0xFF81C784),
                        center = Offset(px, py),
                        radius = hexRadius * 0.22f
                    )
                }
            }

            // Draw Piece
            cell.piece?.let { piece ->
                drawHexPiece(
                    scope = this,
                    textMeasurer = textMeasurer,
                    piece = piece,
                    center = Offset(px, py),
                    hexRadius = hexRadius
                )
            }
        }

        // 2. Tactical Weapon Crosshair Shader Overlay
        if (activeWeapon != TacticalWeapon.NONE) {
            val strokeColor = if (activeWeapon == TacticalWeapon.NUKE) Color(0xFFE57373) else Color(0xFFFFB74D)
            drawCircle(
                color = strokeColor.copy(alpha = pulseAlpha * 0.15f),
                center = Offset(centerX, centerY),
                radius = width * 0.46f
            )
        }

        // 3. Shockwave Particle Explosions
        for (exp in activeExplosions) {
            val px = centerX + hexRadius * sqrt(3f) * (exp.hexPos.q + exp.hexPos.r / 2.0f)
            val py = centerY + hexRadius * 1.5f * exp.hexPos.r
            val rad = exp.radius * hexRadius * 2.2f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color(exp.colorHex), Color.Transparent),
                    center = Offset(px, py),
                    radius = rad
                ),
                center = Offset(px, py),
                radius = rad
            )
        }
    }
}

private fun createPointyHexPath(centerX: Float, centerY: Float, radius: Float): Path {
    val path = Path()
    for (i in 0..5) {
        val angleRad = Math.toRadians((60 * i - 30).toDouble())
        val x = centerX + radius * cos(angleRad).toFloat()
        val y = centerY + radius * sin(angleRad).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private fun drawHexPiece(
    scope: DrawScope,
    textMeasurer: TextMeasurer,
    piece: ChessPiece,
    center: Offset,
    hexRadius: Float
) {
    scope.apply {
        val pieceColor = Color(piece.color.hexColor)
        val containerColor = Color(piece.color.containerColor)

        // Soft M3 Expressive Badge Container
        drawCircle(
            color = containerColor,
            center = center,
            radius = hexRadius * 0.55f
        )
        drawCircle(
            color = pieceColor,
            center = center,
            radius = hexRadius * 0.55f,
            style = Stroke(width = 2.5f)
        )

        if (piece.type == PieceType.PAWN) {
            // Chocolate Cornet Pawn 🥐
            val cornetPath = Path().apply {
                val cx = center.x
                val cy = center.y
                val r = hexRadius * 0.35f
                moveTo(cx - r, cy + r * 0.5f)
                cubicTo(cx - r, cy - r * 0.8f, cx + r * 0.5f, cy - r * 1.1f, cx + r, cy - r * 0.2f)
                cubicTo(cx + r * 1.1f, cy + r * 0.5f, cx + r * 0.2f, cy + r * 0.8f, cx - r, cy + r * 0.5f)
                close()
            }

            drawPath(
                path = cornetPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFE0D0C0), Color(0xFFA1887F), Color(0xFF5D4037))
                )
            )
            drawPath(
                path = cornetPath,
                color = pieceColor,
                style = Stroke(width = 2f)
            )

            // Chocolate filling dip
            drawCircle(
                color = Color(0xFF3E2723),
                center = Offset(center.x + hexRadius * 0.15f, center.y - hexRadius * 0.05f),
                radius = hexRadius * 0.12f
            )
        } else {
            val textLayoutResult = textMeasurer.measure(
                text = AnnotatedString(piece.type.symbol),
                style = TextStyle(
                    color = pieceColor,
                    fontSize = (hexRadius * 0.65f).sp
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
