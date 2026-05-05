package com.manuelreeb.monocontrol.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manuelreeb.monocontrol.data.local.entity.PerfilEntity
import com.manuelreeb.monocontrol.data.local.entity.ClienteEntity
import com.manuelreeb.monocontrol.data.repository.PerfilRepository
import com.manuelreeb.monocontrol.data.repository.ClienteRepository
import com.manuelreeb.monocontrol.domain.model.UserRole
import com.manuelreeb.monocontrol.utils.PagosMonotributoHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PerfilViewModel(
    private val repository: PerfilRepository,
    private val clienteRepository: ClienteRepository
) : ViewModel() {

    val perfil: StateFlow<PerfilEntity?> = repository.observar()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val clientes: StateFlow<List<ClienteEntity>> = clienteRepository.observarTodos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun guardar(nombre: String, cuit: String) {
        viewModelScope.launch {
            val actual = repository.observar().first() ?: PerfilEntity()
            repository.guardar(
                actual.copy(nombre = nombre.trim(), cuit = cuit.trim())
            )
        }
    }

    fun cambiarRubro(esVentaMuebles: Boolean) {
        viewModelScope.launch {
            val actual = repository.observar().first() ?: PerfilEntity()
            repository.guardar(actual.copy(esVentaMuebles = esVentaMuebles))
        }
    }

    /**
     * Modo prueba: forzar manualmente la categoría visible en toda la app.
     * Pasá la letra ("A".."K") o null para volver a "automática".
     */
    fun setCategoriaOverride(letra: String?) {
        viewModelScope.launch {
            val actual = repository.observar().first() ?: PerfilEntity()
            repository.guardar(actual.copy(categoriaOverride = letra))
        }
    }

    /**
     * Cambia el rol del usuario (Personal / Contador). Útil para que un
     * contador pueda alternar entre la vista completa (clientes) y la vista
     * personal simplificada para usar la app con su propio Monotributo.
     */
    fun cambiarRol(rol: UserRole) {
        viewModelScope.launch {
            val actual = repository.observar().first() ?: PerfilEntity()
            repository.guardar(actual.copy(rol = rol.name))
        }
    }

    /**
     * Actualiza el estado del pago del Monotributo de un cliente para un periodo dado.
     */
    fun actualizarPago(
        cliente: ClienteEntity,
        periodo: String,
        estado: PagosMonotributoHelper.EstadoPago?
    ) {
        viewModelScope.launch {
            val nuevoJson = PagosMonotributoHelper.actualizarPeriodo(
                cliente.pagosMonotributoJson, periodo, estado
            )
            clienteRepository.actualizar(
                cliente.copy(
                    pagosMonotributoJson = nuevoJson,
                    actualizadoEn = System.currentTimeMillis(),
                    syncPendiente = true
                )
            )
        }
    }
}
