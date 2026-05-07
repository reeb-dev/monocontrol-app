package com.reeb.controlmonotributoar.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reeb.controlmonotributoar.data.repository.ClienteRepository
import com.reeb.controlmonotributoar.data.repository.AlertaRepository
import com.reeb.controlmonotributoar.data.repository.MovimientoRepository
import com.reeb.controlmonotributoar.data.repository.PerfilRepository
import com.reeb.controlmonotributoar.domain.model.Alerta
import com.reeb.controlmonotributoar.domain.model.Movimiento
import com.reeb.controlmonotributoar.domain.model.ResumenMensual
import com.reeb.controlmonotributoar.domain.model.UserRole
import com.reeb.controlmonotributoar.domain.usecase.GenerarAlertasUseCase
import com.reeb.controlmonotributoar.domain.usecase.ObtenerResumenMensualUseCase
import com.reeb.controlmonotributoar.utils.CategoriaOverride
import com.reeb.controlmonotributoar.utils.AppEventLogger
import com.reeb.controlmonotributoar.utils.HonorariosHelper
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
    val rol: UserRole = UserRole.PERSONAL,
    val totalClientes: Int = 0,
    val clientesSinEmail: Int = 0,
    val clientesVencen7Dias: Int = 0,
    val honorariosPendientesHoy: Int = 0,
    val honorariosACobrarMes: Double = 0.0
)

class HomeViewModel(
    obtenerResumenUseCase: ObtenerResumenMensualUseCase,
    private val generarAlertasUseCase: GenerarAlertasUseCase,
    perfilRepository: PerfilRepository,
    clienteRepository: ClienteRepository? = null,
    movimientoRepository: MovimientoRepository? = null,
    alertaRepository: AlertaRepository? = null
) : ViewModel() {

    val state: StateFlow<HomeState> = combine(
        obtenerResumenUseCase(),
        perfilRepository.observar(),
        clienteRepository?.observarTodos() ?: flowOf(emptyList()),
        movimientoRepository?.obtenerTodos() ?: flowOf(emptyList()),
        alertaRepository?.obtenerTodas() ?: flowOf(emptyList())
    ) { resumen, perfil, clientes, movimientos, alertas ->
        val override = CategoriaOverride.fromLetra(perfil?.categoriaOverride)
        val ahora = System.currentTimeMillis()
        val sieteDias = ahora + 7L * 24 * 60 * 60 * 1000
        val periodo = HonorariosHelper.periodoActual(ahora)
        val honorariosPendientes = clientes.filter {
            it.honorarioActivo &&
                it.honorarioMonto > 0 &&
                HonorariosHelper.esMesCobro(it, ahora) &&
                !HonorariosHelper.estaPagadoPeriodo(it, periodo)
        }
        HomeState(
            resumen = resumen?.let { CategoriaOverride.aplicar(it, override) },
            esVentaMuebles = perfil?.esVentaMuebles ?: false,
            ultimosMovimientos = movimientos.sortedByDescending { it.fecha }.take(4),
            alertasNoLeidas = alertas.filter { !it.leida }.take(3),
            rol = UserRole.fromName(perfil?.rol),
            totalClientes = clientes.size,
            clientesSinEmail = clientes.count { it.email.isBlank() },
            clientesVencen7Dias = clientes.count { it.proximoVencimientoEpoch in (ahora + 1)..sieteDias },
            honorariosPendientesHoy = honorariosPendientes.count {
                HonorariosHelper.diasHastaVencimiento(it, ahora) == 0
            },
            honorariosACobrarMes = honorariosPendientes.sumOf { it.honorarioMonto }
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
                .onSuccess { AppEventLogger.log("home_actualizar_alertas_ok") }
                .onFailure { AppEventLogger.log("home_actualizar_alertas_error", it.message.orEmpty()) }
        }
    }
}
