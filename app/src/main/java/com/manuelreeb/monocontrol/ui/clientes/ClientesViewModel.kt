package com.manuelreeb.monocontrol.ui.clientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.manuelreeb.monocontrol.data.local.entity.ClienteEntity
import com.manuelreeb.monocontrol.data.local.entity.PerfilEntity
import com.manuelreeb.monocontrol.data.repository.ClienteRepository
import com.manuelreeb.monocontrol.data.repository.MovimientoRepository
import com.manuelreeb.monocontrol.data.repository.PerfilRepository
import com.manuelreeb.monocontrol.data.work.ClienteSyncWorker
import com.manuelreeb.monocontrol.domain.usecase.EnviarRecordatorioEmailUseCase
import com.manuelreeb.monocontrol.domain.usecase.EnviarRecordatorioWhatsAppUseCase
import com.manuelreeb.monocontrol.domain.usecase.GenerarFacturaPDFUseCase
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
import java.util.Calendar

data class PanelStats(
    val total: Int = 0,
    val activos: Int = 0,
    val porVencer: Int = 0,   // vencen en <= 30 días
    val vencidos: Int = 0,
    val sinEmail: Int = 0,
    val conRecateg: Int = 0   // necesitan recategorización este período
)

data class FiltroClientes(val tipo: String = "todos")  // todos, activos, por_vencer, vencidos, sin_email

@OptIn(ExperimentalCoroutinesApi::class)
class ClientesViewModel(
    private val clienteRepo: ClienteRepository,
    private val perfilRepo: PerfilRepository,
    private val movimientoRepo: MovimientoRepository,
    /**
     * Callback opcional que se invoca después de cada cambio para encolar el
     * sync con Firestore. Se inyecta desde AppNavGraph (`{ ClienteSyncWorker.encolar(ctx) }`).
     */
    private val clienteSyncTrigger: (() -> Unit)? = null
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _filtro = MutableStateFlow("todos")
    val filtro: StateFlow<String> = _filtro

    private val _todosClientes = clienteRepo.observarTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val stats: StateFlow<PanelStats> = _todosClientes
        .combine(_todosClientes) { lista, _ ->
            val hoy = Calendar.getInstance().timeInMillis
            val treintaDias = hoy + 30L * 24 * 60 * 60 * 1000
            PanelStats(
                total      = lista.size,
                activos    = lista.count { it.estadoCliente == "activo" },
                porVencer  = lista.count { it.proximoVencimientoEpoch in (hoy + 1)..treintaDias },
                vencidos   = lista.count {
                    it.proximoVencimientoEpoch in 1..<hoy ||
                    it.estadoCliente == "vencido"
                },
                sinEmail   = lista.count { it.email.isBlank() },
                conRecateg = lista.count { cli ->
                    if (cli.ingresoAnualCliente <= 0 || cli.categoria == null) false
                    else {
                        val catAct = com.manuelreeb.monocontrol.domain.model.CategoriaMonotributo
                            .entries.firstOrNull { it.letra == cli.categoria }
                        val catCalc = com.manuelreeb.monocontrol.domain.model.CategoriaMonotributo
                            .porLimiteAnual(cli.ingresoAnualCliente)
                        catAct != null && catCalc != catAct
                    }
                }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PanelStats())

    val clientes: StateFlow<List<ClienteEntity>> = _query
        .combine(_filtro) { q, f -> q to f }
        .flatMapLatest { (q, f) ->
            clienteRepo.buscar(q).combine(clienteRepo.observarTodos()) { filtrados, todos ->
                val base = if (q.isBlank()) todos else filtrados
                val hoy = Calendar.getInstance().timeInMillis
                val treintaDias = hoy + 30L * 24 * 60 * 60 * 1000
                when (f) {
                    "por_vencer" -> base.filter { it.proximoVencimientoEpoch in (hoy + 1)..treintaDias }
                    "vencidos"   -> base.filter {
                        it.proximoVencimientoEpoch in 1..<hoy || it.estadoCliente == "vencido"
                    }
                    "activos"    -> base.filter { it.estadoCliente == "activo" }
                    "sin_email"  -> base.filter { it.email.isBlank() }
                    "recateg"    -> base.filter { cli ->
                        if (cli.ingresoAnualCliente <= 0 || cli.categoria == null) false
                        else {
                            val catAct = com.manuelreeb.monocontrol.domain.model.CategoriaMonotributo
                                .entries.firstOrNull { it.letra == cli.categoria }
                            val catCalc = com.manuelreeb.monocontrol.domain.model.CategoriaMonotributo
                                .porLimiteAnual(cli.ingresoAnualCliente)
                            catAct != null && catCalc != catAct
                        }
                    }
                    else         -> base
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChange(q: String) { _query.value = q }
    fun onFiltroChange(f: String) { _filtro.value = f }

    /** Envía recordatorio a todos los clientes que tienen email configurado. */
    fun enviarRecordatorioMasivo(context: Context, onProgreso: (Int, Int) -> Unit) {
        viewModelScope.launch {
            val lista = _todosClientes.value.filter { it.email.isNotBlank() }
            lista.forEachIndexed { idx, cliente ->
                runCatching { enviarRecordatorio(context, cliente)?.let { context.startActivity(it) } }
                onProgreso(idx + 1, lista.size)
            }
        }
    }

    fun guardarNuevo(
        nombre: String,
        cuit: String,
        email: String,
        telefono: String = "",
        categoria: String?,
        esVentaMuebles: Boolean,
        onSaved: (Long) -> Unit = {}
    ) {
        if (nombre.isBlank()) return
        viewModelScope.launch {
            val id = clienteRepo.guardar(
                ClienteEntity(
                    nombre = nombre.trim(),
                    cuit = cuit.trim(),
                    email = email.trim(),
                    telefono = telefono.trim(),
                    categoria = categoria,
                    esVentaMuebles = esVentaMuebles
                )
            )
            // Subir cambio a Firestore (en background, cuando haya red).
            clienteSyncTrigger?.invoke()
            onSaved(id)
        }
    }

    fun eliminar(cliente: ClienteEntity) {
        viewModelScope.launch {
            clienteRepo.eliminar(cliente)
            clienteSyncTrigger?.invoke()
        }
    }

    /**
     * Actualiza el estado del pago del Monotributo de un cliente para un periodo dado.
     * @param cliente  El cliente a actualizar.
     * @param periodo  Formato "YYYY-MM", ej: "2026-04".
     * @param estado   Nuevo estado ("pagado", "pendiente", "vencido"), o null para borrar.
     */
    fun actualizarPago(
        cliente: ClienteEntity,
        periodo: String,
        estado: com.manuelreeb.monocontrol.utils.PagosMonotributoHelper.EstadoPago?
    ) {
        viewModelScope.launch {
            val nuevoJson = com.manuelreeb.monocontrol.utils.PagosMonotributoHelper
                .actualizarPeriodo(cliente.pagosMonotributoJson, periodo, estado)
            clienteRepo.actualizar(
                cliente.copy(
                    pagosMonotributoJson = nuevoJson,
                    actualizadoEn = System.currentTimeMillis(),
                    syncPendiente = true
                )
            )
            clienteSyncTrigger?.invoke()
        }
    }

    /** Carga el cliente en el perfil activo de la app. */
    fun aplicarComoActivo(cliente: ClienteEntity) {
        viewModelScope.launch {
            val actual = perfilRepo.observar().first() ?: PerfilEntity()
            perfilRepo.guardar(
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
     * Genera el Intent de email con asunto/mensaje personalizado del cliente
     * (si los configuró) y el PDF de la factura como adjunto.
     */
    suspend fun enviarRecordatorio(context: Context, cliente: ClienteEntity): Intent? {
        if (cliente.email.isBlank()) return null

        val totalAnual = movimientoRepo.obtenerTotalIngresosAnuales(DateUtils.anioActual()).first()
        val override = CategoriaOverride.fromLetra(cliente.categoria)
        val categoria = override ?: CategoriaCalculator.calcularCategoria(totalAnual)

        // Generar PDF de la factura
        val pdfUri = GenerarFacturaPDFUseCase().invoke(context, cliente, categoria, totalAnual)

        // Adjuntos: PDF generado + adjunto extra del contador (foto/PDF)
        val adjuntos = mutableListOf<Uri>()
        pdfUri?.let { adjuntos.add(it) }
        if (cliente.emailAdjuntoUri.isNotBlank()) {
            runCatching { Uri.parse(cliente.emailAdjuntoUri) }.getOrNull()?.let { adjuntos.add(it) }
        }

        // Si hay borrador editado, lo prefiero sobre el mensaje guardado.
        val mensajeFinal = when {
            cliente.emailBorrador.isNotBlank() -> cliente.emailBorrador
            cliente.emailMensaje.isNotBlank()  -> cliente.emailMensaje
            else -> ""
        }

        return EnviarRecordatorioEmailUseCase().invoke(
            context = context,
            cliente = cliente,
            categoriaActual = categoria,
            totalFacturadoAnual = totalAnual,
            asuntoCustom = cliente.emailAsunto,
            mensajeCustom = mensajeFinal,
            adjuntos = adjuntos
        )
    }

    /** Genera el Intent para abrir WhatsApp pre-cargado para el cliente. */
    suspend fun enviarRecordatorioWhatsApp(context: Context, cliente: ClienteEntity): Intent? {
        if (cliente.whatsappNumero.isBlank()) return null

        val totalAnual = movimientoRepo.obtenerTotalIngresosAnuales(DateUtils.anioActual()).first()
        val override = CategoriaOverride.fromLetra(cliente.categoria)
        val categoria = override ?: CategoriaCalculator.calcularCategoria(totalAnual)

        val mensaje = when {
            cliente.emailBorrador.isNotBlank() -> cliente.emailBorrador
            cliente.emailMensaje.isNotBlank()  -> cliente.emailMensaje
            else -> ""
        }

        // En WhatsApp sólo se puede adjuntar 1 archivo: priorizamos el adjunto del
        // contador (foto/PDF). Si no hay, generamos el PDF de la factura.
        val adjunto: Uri? = when {
            cliente.emailAdjuntoUri.isNotBlank() ->
                runCatching { Uri.parse(cliente.emailAdjuntoUri) }.getOrNull()
            else ->
                GenerarFacturaPDFUseCase().invoke(context, cliente, categoria, totalAnual)
        }

        return EnviarRecordatorioWhatsAppUseCase().invoke(
            context = context,
            cliente = cliente,
            categoriaActual = categoria,
            totalFacturadoAnual = totalAnual,
            numero = cliente.whatsappNumero,
            mensajeCustom = mensaje,
            adjunto = adjunto
        )
    }
}
