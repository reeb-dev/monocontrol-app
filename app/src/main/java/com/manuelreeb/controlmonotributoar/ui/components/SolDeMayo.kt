package com.reeb.controlmonotributoar.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin

// 🇦🇷 Colores oficiales del Sol de Mayo
private val SOL_AMARILLO = Color(0xFFFBB81C)   // Pantone 1235C
private val SOL_MARRON   = Color(0xFF843511)   // Pantone 1685C - contorno y detalles
private val SOL_CARA     = Color(0xFF843511)   // rasgos faciales

@Composable
fun SolDeMayo(
    modifier: Modifier = Modifier,
    animationAngle: Float = 0f
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r  = size.minDimension / 2f

        rotate(degrees = animationAngle, pivot = Offset(cx, cy)) {

            // ── Rayos rectos (8) ──────────────────────────────────────────
            val rayoRectoLen  = r * 0.90f
            val rayoRectoBase = r * 0.14f
            for (i in 0 until 8) {
                val angle = Math.toRadians((i * 45.0))
                drawRayoRecto(
                    cx = cx, cy = cy,
                    angle = angle.toFloat(),
                    longitud = rayoRectoLen,
                    base = rayoRectoBase,
                    fillColor = SOL_AMARILLO,
                    strokeColor = SOL_MARRON
                )
            }

            // ── Rayos ondulados (8) entre los rectos ──────────────────────
            val rayoOndLen = r * 0.82f
            for (i in 0 until 8) {
                val angle = Math.toRadians((i * 45.0 + 22.5))
                drawRayoOndulado(
                    cx = cx, cy = cy,
                    angle = angle.toFloat(),
                    longitud = rayoOndLen,
                    fillColor = SOL_AMARILLO,
                    strokeColor = SOL_MARRON
                )
            }

            // ── Disco central (relleno) ───────────────────────────────────
            val radioDisc = r * 0.42f
            drawCircle(
                color = SOL_AMARILLO,
                radius = radioDisc,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = SOL_MARRON,
                radius = radioDisc,
                center = Offset(cx, cy),
                style = Stroke(width = r * 0.04f)
            )

            // ── Cara del Sol ──────────────────────────────────────────────
            val eyeR   = radioDisc * 0.13f
            val eyeOffX = radioDisc * 0.32f
            val eyeOffY = radioDisc * 0.18f

            // Ojos
            drawCircle(color = SOL_CARA, radius = eyeR, center = Offset(cx - eyeOffX, cy - eyeOffY))
            drawCircle(color = SOL_CARA, radius = eyeR, center = Offset(cx + eyeOffX, cy - eyeOffY))

            // Nariz
            val narizPath = Path().apply {
                moveTo(cx, cy - eyeOffY * 0.1f)
                lineTo(cx - eyeR * 0.6f, cy + eyeOffY * 0.5f)
                lineTo(cx + eyeR * 0.6f, cy + eyeOffY * 0.5f)
                close()
            }
            drawPath(narizPath, color = SOL_CARA)

            // Sonrisa
            val sonrisaR = radioDisc * 0.30f
            drawArc(
                color = SOL_CARA,
                startAngle = 10f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(cx - sonrisaR, cy - sonrisaR * 0.1f),
                size = androidx.compose.ui.geometry.Size(sonrisaR * 2, sonrisaR * 1.1f),
                style = Stroke(width = r * 0.035f)
            )

            // Cejas (líneas curvas)
            val cejaDx = eyeOffX * 1.1f
            val cejaDy = eyeOffY * 1.7f
            drawLine(
                color = SOL_CARA,
                start = Offset(cx - cejaDx - eyeR * 0.8f, cy - cejaDy),
                end   = Offset(cx - cejaDx + eyeR * 0.8f, cy - cejaDy - eyeR * 0.5f),
                strokeWidth = r * 0.035f
            )
            drawLine(
                color = SOL_CARA,
                start = Offset(cx + cejaDx - eyeR * 0.8f, cy - cejaDy - eyeR * 0.5f),
                end   = Offset(cx + cejaDx + eyeR * 0.8f, cy - cejaDy),
                strokeWidth = r * 0.035f
            )
        }
    }
}

// ── Rayo recto (trapezoidal) ──────────────────────────────────────────────────
private fun DrawScope.drawRayoRecto(
    cx: Float, cy: Float,
    angle: Float,
    longitud: Float,
    base: Float,
    fillColor: Color,
    strokeColor: Color
) {
    val r0 = size.minDimension * 0.20f   // radio desde donde arranca el rayo
    val perp = angle + (Math.PI / 2).toFloat()

    val x1 = cx + r0 * cos(angle) - (base / 2) * cos(perp)
    val y1 = cy + r0 * sin(angle) - (base / 2) * sin(perp)
    val x2 = cx + r0 * cos(angle) + (base / 2) * cos(perp)
    val y2 = cy + r0 * sin(angle) + (base / 2) * sin(perp)
    val x3 = cx + longitud * cos(angle) + (base * 0.05f) * cos(perp)
    val y3 = cy + longitud * sin(angle) + (base * 0.05f) * sin(perp)
    val x4 = cx + longitud * cos(angle) - (base * 0.05f) * cos(perp)
    val y4 = cy + longitud * sin(angle) - (base * 0.05f) * sin(perp)

    val path = Path().apply {
        moveTo(x1, y1); lineTo(x2, y2); lineTo(x3, y3); lineTo(x4, y4); close()
    }
    drawPath(path, color = fillColor)
    drawPath(path, color = strokeColor, style = Stroke(width = size.minDimension * 0.012f))
}

// ── Rayo ondulado (punta de flecha curva) ─────────────────────────────────────
private fun DrawScope.drawRayoOndulado(
    cx: Float, cy: Float,
    angle: Float,
    longitud: Float,
    fillColor: Color,
    strokeColor: Color
) {
    val r0   = size.minDimension * 0.20f
    val ancho = size.minDimension * 0.06f
    val perp  = angle + (Math.PI / 2).toFloat()

    // Punta
    val tipX = cx + longitud * cos(angle)
    val tipY = cy + longitud * sin(angle)
    // Base izquierda/derecha
    val bLx = cx + r0 * cos(angle) - ancho * cos(perp)
    val bLy = cy + r0 * sin(angle) - ancho * sin(perp)
    val bRx = cx + r0 * cos(angle) + ancho * cos(perp)
    val bRy = cy + r0 * sin(angle) + ancho * sin(perp)
    // Puntos de control para ondas
    val mid = (r0 + longitud) / 2
    val cp1x = cx + mid * cos(angle) - ancho * 1.4f * cos(perp)
    val cp1y = cy + mid * sin(angle) - ancho * 1.4f * sin(perp)
    val cp2x = cx + mid * cos(angle) + ancho * 1.4f * cos(perp)
    val cp2y = cy + mid * sin(angle) + ancho * 1.4f * sin(perp)

    val path = Path().apply {
        moveTo(bLx, bLy)
        quadraticTo(cp1x, cp1y, tipX, tipY)
        quadraticTo(cp2x, cp2y, bRx, bRy)
        close()
    }
    drawPath(path, color = fillColor)
    drawPath(path, color = strokeColor, style = Stroke(width = size.minDimension * 0.012f))
}

