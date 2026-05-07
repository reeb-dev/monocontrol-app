package com.reeb.controlmonotributoar.ui.movimientos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reeb.controlmonotributoar.data.repository.MovimientoRepository
import com.reeb.controlmonotributoar.data.repository.PerfilRepository
import com.reeb.controlmonotributoar.domain.model.CategoriaMonotributo
import com.reeb.controlmonotributoar.domain.model.Movimiento
import com.reeb.controlmonotributoar.domain.model.TipoMovimiento
import com.reeb.controlmonotributoar.domain.usecase.AgregarMovimientoUseCase
import com.reeb.controlmonotributoar.domain.usecase.GenerarAlertasUseCase
import com.reeb.controlmonotributoar.utils.CategoriaCalculator
import com.reeb.controlmonotributoar.utils.CategoriaOverride
import com.reeb.controlmonotributoar.utils.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Estado tarifario del usuario que necesitamos para validar el monto:
 * cuánto lleva facturado en el año y en qué categoría está hoy.
 */
data class TopeState(
    val totalAnual: Double = 0.0,
    val categoriaActual: CategoriaMonotributo = CategoriaMonotributo.A
)

@OptIn(ExperimentalCoroutinesApi::class)
class MovimientoViewModel(
    private val agregarMovimientoUseCase: AgregarMovimientoUseCase,
    private val generarAlertasUseCase: GenerarAlertasUseCase,
    private val repository: MovimientoRepository,
    perfilRepository: PerfilRepository? = null
) : ViewModel() {

    val movimientos = repository.obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Combina lo facturado en el año con la categoría real del usuario
     * (o la forzada por “modo prueba”). La categoría auto sube hasta K
     * a medida que aumentan los ingresos.
     */
    val tope: StateFlow<TopeState> = combine(
        repository.obtenerTotalIngresosAnuales(DateUtils.anioActual()),
        perfilRepository?.observar() ?: flowOf(null)
    ) { total, perfil ->
        val override = CategoriaOverride.fromLetra(perfil?.categoriaOverride)
        val categoria = override ?: CategoriaCalculator.calcularCategoria(total)
        TopeState(totalAnual = total, categoriaActual = categoria)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TopeState())

    private val _guardadoExitoso = MutableStateFlow(false)
    val guardadoExitoso: StateFlow<Boolean> = _guardadoExitoso.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun agregar(monto: Double, tipo: TipoMovimiento, descripcion: String, fecha: Date = Date()) {
        viewModelScope.launch {
            try {
                agregarMovimientoUseCase(monto, tipo, descripcion, fecha)
                generarAlertasUseCase()
                _guardadoExitoso.value = true
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun eliminar(movimiento: Movimiento) {
        viewModelScope.launch {
            repository.eliminar(movimiento)
            generarAlertasUseCase()
        }
    }

    fun resetGuardado() { _guardadoExitoso.value = false }
    fun resetError() { _error.value = null }
}
