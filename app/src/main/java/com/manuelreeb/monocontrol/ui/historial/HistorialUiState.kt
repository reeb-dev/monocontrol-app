package com.manuelreeb.monocontrol.ui.historial

data class DatoMensual(
    val mes: String,        // "Ene", "Feb", etc.
    val facturado: Double,
    val limite: Double
) {
    val porcentaje: Float get() = (facturado / limite).toFloat().coerceIn(0f, 1f)
}

sealed class HistorialUiState {
    object Cargando : HistorialUiState()
    data class Exito(
        val meses: List<DatoMensual>,
        val totalAnual: Double,
        val limiteAnual: Double,
        val anio: Int
    ) : HistorialUiState()
    data class Error(val mensaje: String) : HistorialUiState()
}

