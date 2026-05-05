package com.manuelreeb.monocontrol.ui.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class HistorialViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HistorialUiState>(HistorialUiState.Cargando)
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    private val _anioSeleccionado = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val anioSeleccionado: StateFlow<Int> = _anioSeleccionado.asStateFlow()

    init {
        cargarHistorial()
    }

    fun cambiarAnio(anio: Int) {
        _anioSeleccionado.value = anio
        cargarHistorial()
    }

    private fun cargarHistorial() {
        viewModelScope.launch {
            try {
                // TODO: conectar con repositorio real
                // Por ahora datos de ejemplo para mostrar la UI
                val mesesNombres = listOf("Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic")
                val limite = 2_000_000.0
                val meses = mesesNombres.mapIndexed { i, nombre ->
                    DatoMensual(
                        mes = nombre,
                        facturado = (100_000..200_000).random().toDouble(),
                        limite = limite / 12
                    )
                }
                _uiState.value = HistorialUiState.Exito(
                    meses = meses,
                    totalAnual = meses.sumOf { it.facturado },
                    limiteAnual = limite,
                    anio = _anioSeleccionado.value
                )
            } catch (e: Exception) {
                _uiState.value = HistorialUiState.Error(e.message ?: "Error al cargar historial")
            }
        }
    }
}

