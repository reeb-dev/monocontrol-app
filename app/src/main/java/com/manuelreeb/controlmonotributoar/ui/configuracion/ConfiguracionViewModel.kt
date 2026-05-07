package com.reeb.controlmonotributoar.ui.configuracion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reeb.controlmonotributoar.domain.model.CategoriaMonotributo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ConfiguracionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ConfiguracionUiState())
    val uiState: StateFlow<ConfiguracionUiState> = _uiState.asStateFlow()

    fun cambiarCategoria(categoria: CategoriaMonotributo) {
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            try {
                // TODO: guardar en DataStore/Repository
                _uiState.update {
                    it.copy(
                        categoriaActual = categoria,
                        guardando = false,
                        mensajeExito = "Categoría actualizada a ${categoria.name}"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, error = e.message) }
            }
        }
    }

    fun toggleNotificaciones(activar: Boolean) {
        _uiState.update { it.copy(notificacionesActivas = activar) }
        // TODO: guardar preferencia en DataStore
    }

    fun cambiarUmbralAlerta(porcentaje: Int) {
        _uiState.update { it.copy(umbralAlerta = porcentaje) }
    }

    fun agregarActividad(nombre: String) {
        if (nombre.isBlank()) return
        _uiState.update {
            it.copy(actividades = it.actividades + nombre)
        }
    }

    fun eliminarActividad(nombre: String) {
        _uiState.update {
            it.copy(actividades = it.actividades - nombre)
        }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(mensajeExito = null, error = null) }
    }
}

