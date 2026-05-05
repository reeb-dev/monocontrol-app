package com.manuelreeb.monocontrol.ui.home

import com.manuelreeb.monocontrol.domain.model.ResumenMensual

/**
 * @deprecated Mantenido sólo por compatibilidad de imports anteriores.
 * HomeScreen consume directamente `HomeViewModel.resumen`.
 */
@Suppress("unused")
sealed class HomeUiState {
    data object Cargando : HomeUiState()
    data class Exito(val resumen: ResumenMensual) : HomeUiState()
    data class Error(val mensaje: String) : HomeUiState()
}
