package com.reeb.controlmonotributoar.ui.excel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reeb.controlmonotributoar.data.repository.AlertaRepository
import com.reeb.controlmonotributoar.data.repository.MovimientoRepository
import com.reeb.controlmonotributoar.domain.model.CategoriaMonotributo
import com.reeb.controlmonotributoar.domain.usecase.GenerarAlertasUseCase
import com.reeb.controlmonotributoar.utils.CategoriaCalculator
import com.reeb.controlmonotributoar.utils.DateUtils
import com.reeb.controlmonotributoar.utils.ExcelExporter
import com.reeb.controlmonotributoar.utils.ExcelImporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ExcelUiState {
    object Idle : ExcelUiState()
    object Loading : ExcelUiState()
    data class ImportSuccess(val importados: Int, val errores: List<String>) : ExcelUiState()
    data class ExportSuccess(val intent: Intent) : ExcelUiState()
    data class Error(val mensaje: String) : ExcelUiState()
}

class ExcelViewModel(
    private val movimientoRepo: MovimientoRepository,
    private val alertaRepo: AlertaRepository,
    private val generarAlertasUseCase: GenerarAlertasUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExcelUiState>(ExcelUiState.Idle)
    val uiState: StateFlow<ExcelUiState> = _uiState.asStateFlow()

    fun importar(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = ExcelUiState.Loading
            withContext(Dispatchers.IO) {
                try {
                    val resultado = ExcelImporter.importarDesdeUri(context, uri)
                    resultado.movimientos.forEach { movimientoRepo.agregar(it) }
                    generarAlertasUseCase()
                    _uiState.value = ExcelUiState.ImportSuccess(
                        importados = resultado.movimientos.size,
                        errores = resultado.errores
                    )
                } catch (e: Exception) {
                    _uiState.value = ExcelUiState.Error("Error al importar: ${e.message}")
                }
            }
        }
    }

    fun exportar(context: Context) {
        viewModelScope.launch {
            _uiState.value = ExcelUiState.Loading
            withContext(Dispatchers.IO) {
                try {
                    val movimientos = movimientoRepo.obtenerTodos().first()
                    val totalAnual = movimientoRepo.obtenerTotalIngresosAnuales(DateUtils.anioActual()).first()
                    val categoria = CategoriaCalculator.calcularCategoria(totalAnual)

                    val shareIntent = ExcelExporter.exportarReporteCompleto(
                        context = context,
                        movimientos = movimientos,
                        categoriaActual = categoria,
                        facturacionAnual = totalAnual
                    )
                    _uiState.value = ExcelUiState.ExportSuccess(shareIntent)
                } catch (e: Exception) {
                    _uiState.value = ExcelUiState.Error("Error al exportar: ${e.message}")
                }
            }
        }
    }

    fun resetState() { _uiState.value = ExcelUiState.Idle }
}

