package com.manuelreeb.monocontrol.utils

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

object CurrencyFormatter {
    private val format = NumberFormat.getCurrencyInstance(Locale("es", "AR"))

    fun formatear(monto: Double): String = format.format(monto)

    /**
     * Formato compacto y legible para mostrar en cards/chips.
     * Siempre incluye el símbolo "$" y el signo si es negativo.
     * Ejemplos:
     *   1234         → "$1.234"
     *   12_500       → "$12,5K"
     *   999_000      → "$999K"
     *   1_234_567    → "$1,23M"
     *   1_500_000_000 → "$1,5MM"
     *   -50_000      → "-$50K"
     */
    fun formatearCompacto(monto: Double): String {
        if (monto == 0.0) return "$0"
        val signo = if (monto < 0) "-" else ""
        val abs = abs(monto)

        return when {
            abs >= 1_000_000_000 -> "$signo$${"%.1f".format(abs / 1_000_000_000).reemplazarPunto()}MM"
            abs >= 1_000_000     -> "$signo$${"%.2f".format(abs / 1_000_000).reemplazarPunto().sinCerosFinales()}M"
            abs >= 100_000       -> "$signo$${"%.0f".format(abs / 1_000)}K"
            abs >= 10_000        -> "$signo$${"%.1f".format(abs / 1_000).reemplazarPunto().sinCerosFinales()}K"
            abs >= 1_000         -> "$signo$${"%.1f".format(abs / 1_000).reemplazarPunto().sinCerosFinales()}K"
            else                 -> "$signo$${"%.0f".format(abs)}"
        }
    }

    private fun String.reemplazarPunto(): String = replace('.', ',')
    private fun String.sinCerosFinales(): String =
        if (contains(',')) trimEnd('0').trimEnd(',') else this
}
