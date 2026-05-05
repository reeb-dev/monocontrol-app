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
 * ViewModel para la pantalla de configuración de envío de recordatorios:
 * email personalizado (asunto, mensaje, adjunto), borrador, programación
 * automática y WhatsApp.
 */
class ConfigurarEnvioViewModel(
    private val clienteRepo: ClienteRepository
) : ViewModel() {

    var clienteSyncTrigger: (() -> Unit)? = null


    private val _cliente = MutableStateFlow<ClienteEntity?>(null)
    val cliente: StateFlow<ClienteEntity?> = _cliente.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun cargar(clienteId: Long) {
        viewModelScope.launch {
            _cliente.value = clienteRepo.porId(clienteId)
        }
    }

    fun guardarConfig(
        emailAsunto: String,
        emailMensaje: String,
        emailAdjuntoUri: String,
        emailAdjuntoNombre: String,
        envioAutomatico: Boolean,
        envioDiaMes: Int,
        envioHora: Int,
        envioMinuto: Int,
        whatsappNumero: String,
        whatsappEnviar: Boolean
    ) {
        val actual = _cliente.value ?: return
        viewModelScope.launch {
            val nuevo = actual.copy(
                emailAsunto = emailAsunto,
                emailMensaje = emailMensaje,
                emailAdjuntoUri = emailAdjuntoUri,
                emailAdjuntoNombre = emailAdjuntoNombre,
                // Si guardó como mensaje "real", el borrador se limpia.
                emailBorrador = "",
                envioAutomatico = envioAutomatico,
                envioDiaMes = envioDiaMes.coerceIn(1, 28),
                envioHora = envioHora.coerceIn(0, 23),
                envioMinuto = envioMinuto.coerceIn(0, 59),
                whatsappNumero = whatsappNumero.filter { it.isDigit() },
                whatsappEnviar = whatsappEnviar
            )
            clienteRepo.guardar(nuevo); clienteSyncTrigger?.invoke()
            _cliente.value = nuevo
            _mensaje.value = "Configuración guardada ✅"
        }
    }

    /** Guarda solo el borrador (mensaje a medio escribir). */
    fun guardarBorrador(borrador: String) {
        val actual = _cliente.value ?: return
        viewModelScope.launch {
            val nuevo = actual.copy(emailBorrador = borrador)
            clienteRepo.guardar(nuevo); clienteSyncTrigger?.invoke()
            _cliente.value = nuevo
            _mensaje.value = "Borrador guardado 📝"
        }
    }

    fun descartarBorrador() {
        val actual = _cliente.value ?: return
        viewModelScope.launch {
            val nuevo = actual.copy(emailBorrador = "")
            clienteRepo.guardar(nuevo); clienteSyncTrigger?.invoke()
            _cliente.value = nuevo
            _mensaje.value = "Borrador descartado"
        }
    }

    fun limpiarMensaje() { _mensaje.value = null }
}

