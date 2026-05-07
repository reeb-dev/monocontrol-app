package com.reeb.controlmonotributoar.utils

import com.reeb.controlmonotributoar.domain.model.CategoriaMonotributo
import java.util.Calendar

object CategoriaCalculator {

    /**
     * Calcula la categoría actual basándose en la facturación anual acumulada.
     */
    fun calcularCategoria(facturacionAnual: Double): CategoriaMonotributo =
        CategoriaMonotributo.porLimiteAnual(facturacionAnual)

    /**
     * Calcula qué porcentaje del límite de la categoría actual se ha utilizado.
     */
    fun calcularPorcentajeDelLimite(facturacionAnual: Double): Float {
        val categoria = calcularCategoria(facturacionAnual)
        return (facturacionAnual / categoria.limiteAnual).toFloat().coerceIn(0f, 1f)
    }

    /**
     * Simula a qué categoría llegarías si facturás X pesos más.
     */
    fun simularRecategorizacion(facturacionActual: Double, montoExtra: Double): CategoriaMonotributo =
        CategoriaMonotributo.porLimiteAnual(facturacionActual + montoExtra)

    /**
     * Cuánto dinero podés facturar todavía dentro de tu categoría actual
     * antes de pasar a la siguiente. Nunca devuelve negativo.
     */
    fun margenHastaSiguienteCategoria(facturacionAnual: Double): Double {
        val categoriaActual = calcularCategoria(facturacionAnual)
        return (categoriaActual.limiteAnual - facturacionAnual).coerceAtLeast(0.0)
    }

    /**
     * Devuelve la siguiente categoría (la que seguiría en orden alfabético).
     * Si ya estás en K devuelve null (quedaste fuera del régimen).
     */
    fun proximaCategoria(facturacionAnual: Double): CategoriaMonotributo? {
        val actual = calcularCategoria(facturacionAnual)
        val todas = CategoriaMonotributo.entries
        val idx = todas.indexOf(actual)
        return if (idx in 0 until todas.size - 1) todas[idx + 1] else null
    }

    /**
     * Cuánto podés facturar en promedio por mes en lo que resta del año
     * sin superar el límite de la categoría actual.
     */
    fun promedioMensualPermitido(
        facturacionAnual: Double,
        mesActual: Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    ): Double {
        val margen = margenHastaSiguienteCategoria(facturacionAnual)
        val mesesRestantes = (12 - mesActual + 1).coerceIn(1, 12)
        return margen / mesesRestantes
    }
}
