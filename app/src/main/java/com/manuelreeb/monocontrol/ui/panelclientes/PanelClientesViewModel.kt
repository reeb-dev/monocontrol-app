package com.manuelreeb.monocontrol.ui.panelclientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manuelreeb.monocontrol.data.local.entity.ClienteEntity
import com.manuelreeb.monocontrol.data.repository.ClienteRepository
import com.manuelreeb.monocontrol.data.repository.MovimientoRepository
import com.manuelreeb.monocontrol.domain.model.CategoriaMonotributo
import com.manuelreeb.monocontrol.domain.model.Movimiento
import com.manuelreeb.monocontrol.domain.model.TipoMovimiento
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

// Datos class para representar estadísticas del cliente
data class ClienteStats(
    val cliente: ClienteEntity,
    val facturacionMes: Double,
    val facturacionAnio: Double,
    val categoriaRiesgo: String  // "verde", "amarillo", "rojo"
)

class PanelClientesViewModel(
    private val clienteRepo: ClienteRepository,
    private val movimientoRepo: MovimientoRepository
) : ViewModel() {

    val clientesConStats: StateFlow<List<ClienteStats>> = clienteRepo.observarTodos()
        .combine(movimientoRepo.obtenerTodos()) { clientes: List<ClienteEntity>, movimientos: List<Movimiento> ->
            val ahoraMs = System.currentTimeMillis()
            val inicioMesMs = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val facturacionMesGlobal = movimientos
                .filter { it.fecha.time in inicioMesMs..ahoraMs && it.tipo == TipoMovimiento.INGRESO }
                .sumOf { it.monto }

            clientes.map { cliente ->
                val facturacionAnio = cliente.ingresoAnualCliente
                val categoria = cliente.categoria?.let { letra ->
                    CategoriaMonotributo.entries.firstOrNull { it.letra == letra }
                }
                val categoriaRiesgo: String = when {
                    facturacionAnio == 0.0 -> "amarillo"  // Sin datos
                    categoria == null -> "amarillo"
                    facturacionAnio >= categoria.limiteAnual * 0.95 -> "rojo"  // >95% del límite
                    facturacionAnio >= categoria.limiteAnual * 0.80 -> "amarillo"  // >80% del límite
                    else -> "verde"  // OK
                }

                ClienteStats(
                    cliente = cliente,
                    facturacionMes = facturacionMesGlobal,
                    facturacionAnio = facturacionAnio,
                    categoriaRiesgo = categoriaRiesgo
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
