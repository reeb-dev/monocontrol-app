package com.reeb.controlmonotributoar.ui.panelclientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reeb.controlmonotributoar.data.local.entity.ClienteEntity
import com.reeb.controlmonotributoar.data.repository.ClienteRepository
import com.reeb.controlmonotributoar.data.repository.MovimientoRepository
import com.reeb.controlmonotributoar.domain.model.CategoriaMonotributo
import com.reeb.controlmonotributoar.domain.model.Movimiento
import com.reeb.controlmonotributoar.domain.model.TipoMovimiento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class ClienteStats(
    val cliente: ClienteEntity,
    val facturacionMes: Double,
    val facturacionAnio: Double,
    val porcentajeLimite: Float,
    val scoreRiesgo: Int,
    val diasAVencimiento: Int?,
    val tendencia: String,
    val serieMensual: List<Double>,
    val categoriaRiesgo: String // "verde", "amarillo", "rojo"
)

data class PanelClientesResumen(
    val total: Int = 0,
    val criticos: Int = 0,
    val vencenEn7Dias: Int = 0,
    val sinDatos: Int = 0
)

data class RiesgoWeights(
    val limite: Float = 0.55f,
    val vencimiento: Float = 0.30f,
    val calidadDatos: Float = 0.15f
)

class PanelClientesViewModel(
    private val clienteRepo: ClienteRepository,
    private val movimientoRepo: MovimientoRepository
) : ViewModel() {
    private val _filtro = MutableStateFlow("todos")
    val filtro: StateFlow<String> = _filtro
    private val _weights = MutableStateFlow(RiesgoWeights())
    val weights: StateFlow<RiesgoWeights> = _weights

    fun setFiltro(value: String) {
        _filtro.value = value
    }

    fun setWeights(newWeights: RiesgoWeights) {
        val sum = (newWeights.limite + newWeights.vencimiento + newWeights.calidadDatos)
            .coerceAtLeast(0.0001f)
        _weights.value = RiesgoWeights(
            limite = newWeights.limite / sum,
            vencimiento = newWeights.vencimiento / sum,
            calidadDatos = newWeights.calidadDatos / sum
        )
    }

    private val todosConStats: StateFlow<List<ClienteStats>> = clienteRepo.observarTodos()
        .combine(movimientoRepo.obtenerTodos()) { clientes: List<ClienteEntity>, movimientos: List<Movimiento> ->
            clientes to movimientos
        }
        .combine(_weights) { (clientes, movimientos), weights ->
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
                val porcentaje = if (categoria != null && categoria.limiteAnual > 0.0 && facturacionAnio > 0.0) {
                    (facturacionAnio / categoria.limiteAnual).toFloat()
                } else {
                    0f
                }
                val diasVto = diasHasta(cliente.proximoVencimientoEpoch, ahoraMs)
                val scoreRiesgo = calcularScoreRiesgo(
                    porcentaje = porcentaje,
                    diasVto = diasVto,
                    facturacionAnio = facturacionAnio,
                    categoria = categoria,
                    weights = weights
                )
                val tendencia = calcularTendencia(cliente.ingresosMensualesJson)
                val serieMensual = parseSerieMensual(cliente.ingresosMensualesJson)
                val categoriaRiesgo: String = when {
                    scoreRiesgo >= 75 -> "rojo"
                    scoreRiesgo >= 45 -> "amarillo"
                    facturacionAnio == 0.0 -> "amarillo"
                    categoria == null -> "amarillo"
                    else -> "verde"  // OK
                }

                ClienteStats(
                    cliente = cliente,
                    facturacionMes = facturacionMesGlobal,
                    facturacionAnio = facturacionAnio,
                    porcentajeLimite = porcentaje,
                    scoreRiesgo = scoreRiesgo,
                    diasAVencimiento = diasVto,
                    tendencia = tendencia,
                    serieMensual = serieMensual,
                    categoriaRiesgo = categoriaRiesgo
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val resumen: StateFlow<PanelClientesResumen> = todosConStats
        .combine(_filtro) { lista, _ ->
            PanelClientesResumen(
                total = lista.size,
                criticos = lista.count { it.scoreRiesgo >= 75 },
                vencenEn7Dias = lista.count { it.diasAVencimiento != null && it.diasAVencimiento in 0..7 },
                sinDatos = lista.count { it.facturacionAnio <= 0.0 }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PanelClientesResumen())

    val clientesConStats: StateFlow<List<ClienteStats>> = todosConStats
        .combine(_filtro) { lista, filtro ->
            val filtrados = when (filtro) {
                "criticos" -> lista.filter { it.scoreRiesgo >= 75 }
                "vencen_7" -> lista.filter { it.diasAVencimiento != null && it.diasAVencimiento in 0..7 }
                "sin_datos" -> lista.filter { it.facturacionAnio <= 0.0 }
                else -> lista
            }
            filtrados.sortedByDescending { it.scoreRiesgo }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val criticosSemana: StateFlow<List<ClienteStats>> = todosConStats
        .combine(_filtro) { lista, _ ->
            lista
                .filter { it.scoreRiesgo >= 75 || (it.diasAVencimiento != null && it.diasAVencimiento in 0..7) }
                .sortedByDescending { it.scoreRiesgo }
                .take(7)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun diasHasta(vencimientoEpoch: Long, ahora: Long): Int? {
        if (vencimientoEpoch <= 0L) return null
        return ((vencimientoEpoch - ahora) / (1000L * 60 * 60 * 24)).toInt()
    }

    private fun calcularScoreRiesgo(
        porcentaje: Float,
        diasVto: Int?,
        facturacionAnio: Double,
        categoria: CategoriaMonotributo?,
        weights: RiesgoWeights
    ): Int {
        val componenteLimite = when {
            porcentaje >= 1f -> 60
            porcentaje >= 0.90f -> 45
            porcentaje >= 0.80f -> 30
            porcentaje >= 0.70f -> 20
            else -> 5
        }
        val componenteVto = when {
            diasVto == null -> 5
            diasVto < 0 -> 30
            diasVto <= 3 -> 25
            diasVto <= 7 -> 18
            diasVto <= 15 -> 10
            else -> 2
        }
        val componenteDatos = when {
            facturacionAnio <= 0.0 && categoria == null -> 30
            facturacionAnio <= 0.0 || categoria == null -> 20
            else -> 5
        }
        val normalLim = (componenteLimite / 60f) * 100f
        val normalVto = (componenteVto / 30f) * 100f
        val normalDat = (componenteDatos / 30f) * 100f
        val weighted = normalLim * weights.limite +
            normalVto * weights.vencimiento +
            normalDat * weights.calidadDatos
        return weighted.toInt().coerceIn(0, 100)
    }

    private fun calcularTendencia(ingresosMensualesJson: String): String {
        if (ingresosMensualesJson.isBlank()) return "Sin tendencia"
        val valores = ingresosMensualesJson.split(",")
            .mapNotNull {
                val partes = it.split(":")
                partes.getOrNull(1)?.replace(".", "")?.replace(",", ".")?.toDoubleOrNull()
            }
        if (valores.size < 2) return "Sin tendencia"
        val mitad = valores.size / 2
        val prev = valores.take(mitad).average()
        val last = valores.takeLast(valores.size - mitad).average()
        return when {
            last > prev * 1.10 -> "↗ Subiendo"
            last < prev * 0.90 -> "↘ Bajando"
            else -> "→ Estable"
        }
    }

    private fun parseSerieMensual(ingresosMensualesJson: String): List<Double> {
        if (ingresosMensualesJson.isBlank()) return emptyList()
        return ingresosMensualesJson
            .split(",")
            .mapNotNull {
                val partes = it.split(":")
                partes.getOrNull(1)?.replace(".", "")?.replace(",", ".")?.toDoubleOrNull()
            }
            .takeLast(12)
    }
}
