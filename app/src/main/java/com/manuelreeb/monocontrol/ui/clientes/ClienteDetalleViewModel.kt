package com.manuelreeb.monocontrol.ui.clientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manuelreeb.monocontrol.data.local.entity.ClienteEntity
import com.manuelreeb.monocontrol.data.repository.ClienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para el detalle/perfil de un cliente individual.
 * Maneja CRUD y configuración de email.
 */
class ClienteDetalleViewModel(
    private val clienteRepo: ClienteRepository,
    private val clienteSyncTrigger: (() -> Unit)? = null
) : ViewModel() {

    private val _cliente = MutableStateFlow<ClienteEntity?>(null)
    val cliente: StateFlow<ClienteEntity?> = _cliente.asStateFlow()

    private val _guardadoExitoso = MutableStateFlow(false)
    val guardadoExitoso: StateFlow<Boolean> = _guardadoExitoso.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun cargarCliente(clienteId: Long) {
        viewModelScope.launch {
            _cliente.value = clienteRepo.porId(clienteId)
        }
    }

    fun actualizar(
        nombre: String,
        cuit: String,
        email: String,
        telefono: String,
        categoria: String?,
        esVentaMuebles: Boolean,
        estadoCliente: String,
        notas: String,
        proximoVencimientoEpoch: Long,
        ingresoAnualCliente: Double = _cliente.value?.ingresoAnualCliente ?: 0.0,
        anioIngresoCliente: Int = _cliente.value?.anioIngresoCliente ?: 0,
        mesIngresoCliente: Int = _cliente.value?.mesIngresoCliente ?: 0,
        ingresosMensualesJson: String = _cliente.value?.ingresosMensualesJson ?: "",
        // Campos AFIP Argentina
        dni: String = _cliente.value?.dni ?: "",
        actividadAfip: String = _cliente.value?.actividadAfip ?: "",
        puntoVenta: Int = _cliente.value?.puntoVenta ?: 0,
        fechaAltaMonotributoEpoch: Long = _cliente.value?.fechaAltaMonotributoEpoch ?: 0L,
        obraSocial: String = _cliente.value?.obraSocial ?: "",
        domicilioFiscal: String = _cliente.value?.domicilioFiscal ?: "",
        tieneEmpleados: Boolean = _cliente.value?.tieneEmpleados ?: false,
        cantidadEmpleados: Int = _cliente.value?.cantidadEmpleados ?: 0
    ) {
        viewModelScope.launch {
            val actual = _cliente.value ?: return@launch
            if (nombre.isBlank()) { _error.value = "El nombre no puede estar vacío"; return@launch }
            try {
                val actualizado = actual.copy(
                    nombre = nombre.trim(), cuit = cuit.trim(), email = email.trim(),
                    telefono = telefono.trim(), categoria = categoria,
                    esVentaMuebles = esVentaMuebles, estadoCliente = estadoCliente,
                    notas = notas, proximoVencimientoEpoch = proximoVencimientoEpoch,
                    ingresoAnualCliente = ingresoAnualCliente,
                    anioIngresoCliente = anioIngresoCliente,
                    mesIngresoCliente = mesIngresoCliente,
                    ingresosMensualesJson = ingresosMensualesJson,
                    dni = dni.trim(),
                    actividadAfip = actividadAfip.trim(),
                    puntoVenta = puntoVenta,
                    fechaAltaMonotributoEpoch = fechaAltaMonotributoEpoch,
                    obraSocial = obraSocial.trim(),
                    domicilioFiscal = domicilioFiscal.trim(),
                    tieneEmpleados = tieneEmpleados,
                    cantidadEmpleados = cantidadEmpleados
                )
                clienteRepo.guardar(actualizado)
                _cliente.value = clienteRepo.porId(actual.id)
                _guardadoExitoso.value = true
                clienteSyncTrigger?.invoke()
            } catch (e: Exception) { _error.value = e.message ?: "Error al guardar" }
        }
    }

    /** Guarda solo los ingresos del cliente sin tocar otros campos. */
    fun actualizarIngresos(
        ingresoAnual: Double,
        anio: Int,
        mes: Int,
        ingresosMensualesJson: String
    ) {
        viewModelScope.launch {
            val actual = _cliente.value ?: return@launch
            val actualizado = actual.copy(
                ingresoAnualCliente = ingresoAnual,
                anioIngresoCliente = anio,
                mesIngresoCliente = mes,
                ingresosMensualesJson = ingresosMensualesJson
            )
            clienteRepo.guardar(actualizado)
            _cliente.value = clienteRepo.porId(actual.id)
            clienteSyncTrigger?.invoke()
        }
    }

    fun eliminar(onEliminado: () -> Unit) {
        viewModelScope.launch {
            _cliente.value?.let { cliente ->
                clienteRepo.eliminar(cliente)
                clienteSyncTrigger?.invoke()
                onEliminado()
            }
        }
    }

    fun resetMensajes() {
        _guardadoExitoso.value = false
        _error.value = null
    }
}
