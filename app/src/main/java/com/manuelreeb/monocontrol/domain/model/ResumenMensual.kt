package com.manuelreeb.monocontrol.domain.model

data class ResumenMensual(
    val mes: Int,
    val anio: Int,
    /** Ingresos solo del mes en curso. */
    val totalIngresos: Double,
    /** Gastos solo del mes en curso. */
    val totalGastos: Double,
    /** Suma de TODOS los ingresos del año (la base para categoría y planificación). */
    val totalIngresosAnual: Double,
    val categoriaActual: CategoriaMonotributo,
    val porcentajeDelLimite: Float
) {
    val balance: Double get() = totalIngresos - totalGastos
}
