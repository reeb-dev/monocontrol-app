package com.manuelreeb.monocontrol.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manuelreeb.monocontrol.data.repository.AlertaRepository
import com.manuelreeb.monocontrol.data.repository.MovimientoRepository
import com.manuelreeb.monocontrol.data.repository.PerfilRepository
import com.manuelreeb.monocontrol.domain.model.Alerta
import com.manuelreeb.monocontrol.domain.model.Movimiento
import com.manuelreeb.monocontrol.domain.model.ResumenMensual
import com.manuelreeb.monocontrol.domain.model.UserRole
import com.manuelreeb.monocontrol.domain.usecase.GenerarAlertasUseCase
import com.manuelreeb.monocontrol.domain.usecase.ObtenerResumenMensualUseCase
import com.manuelreeb.monocontrol.utils.CategoriaOverride
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Estado del Home: resumen mensual + flag de rubro para mostrar la cuota correcta.
 */
data class HomeState(
    val resumen: ResumenMensual? = null,
    val esVentaMuebles: Boolean = false,
    val ultimosMovimientos: List<Movimiento> = emptyList(),
    val alertasNoLeidas: List<Alerta> = emptyList(),
    val rol: UserRole = UserRole.PERSONAL
)

class HomeViewModel(
    obtenerResumenUseCase: ObtenerResumenMensualUseCase,
    private val generarAlertasUseCase: GenerarAlertasUseCase,
    perfilRepository: PerfilRepository,
    movimientoRepository: MovimientoRepository? = null,
    alertaRepository: AlertaRepository? = null
) : ViewModel() {

    val state: StateFlow<HomeState> = combine(
        obtenerResumenUseCase(),
        perfilRepository.observar(),
        movimientoRepository?.obtenerTodos() ?: flowOf(emptyList()),
        alertaRepository?.obtenerTodas() ?: flowOf(emptyList())
    ) { resumen, perfil, movimientos, alertas ->
        val override = CategoriaOverride.fromLetra(perfil?.categoriaOverride)
        HomeState(
            resumen = resumen?.let { CategoriaOverride.aplicar(it, override) },
            esVentaMuebles = perfil?.esVentaMuebles ?: false,
            ultimosMovimientos = movimientos.sortedByDescending { it.fecha }.take(4),
            alertasNoLeidas = alertas.filter { !it.leida }.take(3),
            rol = UserRole.fromName(perfil?.rol)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeState()
    )

    /** Compatibilidad con la pantalla actual (lee sólo resumen). */
    val resumen: StateFlow<ResumenMensual?> =
        kotlinx.coroutines.flow.MutableStateFlow<ResumenMensual?>(null).also { mf ->
            viewModelScope.launch { state.collect { mf.value = it.resumen } }
        }

    fun actualizarAlertas() {
        viewModelScope.launch {
            runCatching { generarAlertasUseCase() }
        }
    }
}
