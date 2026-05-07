package com.reeb.controlmonotributoar.ui.configuracion

import com.reeb.controlmonotributoar.domain.model.CategoriaMonotributo

data class ConfiguracionUiState(
    val categoriaActual: CategoriaMonotributo? = null,
    val notificacionesActivas: Boolean = true,
    val umbralAlerta: Int = 80,          // % para disparar notificación
    val actividades: List<String> = emptyList(),
    val guardando: Boolean = false,
    val mensajeExito: String? = null,
    val error: String? = null
)

