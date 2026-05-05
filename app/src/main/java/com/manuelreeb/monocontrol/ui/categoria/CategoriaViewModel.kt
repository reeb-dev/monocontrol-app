package com.manuelreeb.monocontrol.ui.categoria

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manuelreeb.monocontrol.data.local.entity.ClienteEntity
import com.manuelreeb.monocontrol.data.local.entity.PerfilEntity
import com.manuelreeb.monocontrol.data.repository.ClienteRepository
import com.manuelreeb.monocontrol.data.repository.MovimientoRepository
import com.manuelreeb.monocontrol.data.repository.PerfilRepository
import com.manuelreeb.monocontrol.domain.model.CategoriaMonotributo
import com.manuelreeb.monocontrol.utils.CategoriaCalculator
import com.manuelreeb.monocontrol.utils.CategoriaOverride
import com.manuelreeb.monocontrol.utils.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriaViewModel(
    repository: MovimientoRepository,
    private val perfilRepository: PerfilRepository,
    private val clienteRepository: ClienteRepository
) : ViewModel() {

    val estado: StateFlow<CategoriaState?> = combine(
        repository.obtenerTotalIngresosAnuales(DateUtils.anioActual()),
        perfilRepository.observar(),
        clienteRepository.observarTodos()
    ) { totalAnual, perfil, todosClientes ->
        val override = CategoriaOverride.fromLetra(perfil?.categoriaOverride)
        val categoria = override ?: CategoriaCalculator.calcularCategoria(totalAnual)
        val porcentaje = if (override != null) {
            (totalAnual / override.limiteAnual).toFloat().coerceIn(0f, 1.5f)
        } else {
            CategoriaCalculator.calcularPorcentajeDelLimite(totalAnual)
        }
        val margen = (categoria.limiteAnual - totalAnual).coerceAtLeast(0.0)

        // Detectar si el "perfil activo" corresponde a un cliente guardado.
        // Matching: CUIT exacto si está cargado; si no, por nombre exacto.
        val clienteActivo: ClienteEntity? = perfil?.let { p ->
            when {
                p.cuit.isNotBlank() -> todosClientes.firstOrNull { it.cuit == p.cuit && !it.eliminado }
                p.nombre.isNotBlank() -> todosClientes.firstOrNull { it.nombre.equals(p.nombre, ignoreCase = true) && !it.eliminado }
                else -> null
            }
        }

        CategoriaState(
            categoriaActual = categoria,
            facturacionAnual = totalAnual,
            porcentaje = porcentaje,
            margenRestante = margen,
            todasCategorias = CategoriaMonotributo.entries,
            esOverride = override != null,
            esVentaMuebles = perfil?.esVentaMuebles == true,
            nombreCliente = perfil?.nombre.orEmpty(),
            clienteActivoId = clienteActivo?.id,
            esPerfilPersonal = clienteActivo == null
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // ---------- Buscador de clientes ----------
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val clientes: StateFlow<List<ClienteEntity>> = _query
        .flatMapLatest { clienteRepository.buscar(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChange(q: String) { _query.value = q }

    /** Cambia (o limpia) la categoría forzada del perfil activo. */
    fun setCategoria(letra: String?) {
        viewModelScope.launch {
            val actual = perfilRepository.observar().first() ?: PerfilEntity()
            perfilRepository.guardar(actual.copy(categoriaOverride = letra))

            // Si el perfil activo corresponde a un cliente guardado,
            // también actualizamos su categoría persistida.
            sincronizarConClienteActivo(actual.copy(categoriaOverride = letra)) { cliente ->
                cliente.copy(categoria = letra)
            }
        }
    }

    /**
     * Aplica simultáneamente una categoría y el rubro (servicios o muebles).
     * - Actualiza el perfil personal.
     * - Si el perfil pertenece a un cliente guardado, también lo actualiza
     *   en la tabla de clientes para que persista en su ficha.
     */
    fun aplicarCambios(letra: String?, esVentaMuebles: Boolean) {
        viewModelScope.launch {
            val actual = perfilRepository.observar().first() ?: PerfilEntity()
            val nuevo = actual.copy(
                categoriaOverride = letra,
                esVentaMuebles = esVentaMuebles
            )
            perfilRepository.guardar(nuevo)

            sincronizarConClienteActivo(nuevo) { cliente ->
                cliente.copy(
                    categoria = letra,
                    esVentaMuebles = esVentaMuebles,
                    actualizadoEn = System.currentTimeMillis(),
                    syncPendiente = true
                )
            }
        }
    }

    /** Cambia solo el rubro del perfil activo (y del cliente si corresponde). */
    fun setRubro(esVentaMuebles: Boolean) {
        viewModelScope.launch {
            val actual = perfilRepository.observar().first() ?: PerfilEntity()
            val nuevo = actual.copy(esVentaMuebles = esVentaMuebles)
            perfilRepository.guardar(nuevo)

            sincronizarConClienteActivo(nuevo) { cliente ->
                cliente.copy(
                    esVentaMuebles = esVentaMuebles,
                    actualizadoEn = System.currentTimeMillis(),
                    syncPendiente = true
                )
            }
        }
    }

    /** Carga los datos de un cliente guardado en el perfil activo. */
    fun aplicarCliente(cliente: ClienteEntity) {
        viewModelScope.launch {
            val actual = perfilRepository.observar().first() ?: PerfilEntity()
            perfilRepository.guardar(
                actual.copy(
                    nombre = cliente.nombre,
                    cuit = cliente.cuit,
                    categoriaOverride = cliente.categoria,
                    esVentaMuebles = cliente.esVentaMuebles
                )
            )
        }
    }

    /**
     * Si el perfil pasado corresponde a un cliente guardado (matching por
     * CUIT o nombre), aplica `transform` y persiste el cliente.
     */
    private suspend fun sincronizarConClienteActivo(
        perfil: PerfilEntity,
        transform: (ClienteEntity) -> ClienteEntity
    ) {
        val todos = clienteRepository.observarTodos().first()
        val cliente = when {
            perfil.cuit.isNotBlank() -> todos.firstOrNull { it.cuit == perfil.cuit && !it.eliminado }
            perfil.nombre.isNotBlank() -> todos.firstOrNull {
                it.nombre.equals(perfil.nombre, ignoreCase = true) && !it.eliminado
            }
            else -> null
        } ?: return
        clienteRepository.actualizar(transform(cliente))
    }
}

data class CategoriaState(
    val categoriaActual: CategoriaMonotributo,
    val facturacionAnual: Double,
    val porcentaje: Float,
    val margenRestante: Double,
    val todasCategorias: List<CategoriaMonotributo>,
    val esOverride: Boolean = false,
    val esVentaMuebles: Boolean = false,
    val nombreCliente: String = "",
    val clienteActivoId: Long? = null,
    val esPerfilPersonal: Boolean = true
)
