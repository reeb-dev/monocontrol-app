package com.manuelreeb.monocontrol.data.repository

import com.manuelreeb.monocontrol.data.local.ClienteDao
import com.manuelreeb.monocontrol.data.local.entity.ClienteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio offline-first de clientes.
 *
 * Room es la fuente de verdad. Cada operación de escritura marca el cliente
 * como `syncPendiente = true` y actualiza `actualizadoEn`. Un servicio aparte
 * ([com.manuelreeb.monocontrol.data.sync.ClienteSyncService]) sube esos
 * cambios a Firestore cuando hay red, y trae los cambios remotos.
 */
class ClienteRepository(private val dao: ClienteDao) {

    fun observarTodos(): Flow<List<ClienteEntity>> = dao.observarTodos()

    fun buscar(query: String): Flow<List<ClienteEntity>> =
        if (query.isBlank()) dao.observarTodos() else dao.buscar(query.trim())

    suspend fun porId(id: Long): ClienteEntity? = dao.porId(id)

    suspend fun obtenerPorId(id: Long): ClienteEntity? = dao.porId(id)

    /**
     * Guarda y marca como pendiente de sync. Refresca `actualizadoEn` para que
     * Firestore detecte el cambio.
     */
    suspend fun guardar(cliente: ClienteEntity): Long {
        val ahora = System.currentTimeMillis()
        return dao.guardar(
            cliente.copy(
                actualizadoEn = ahora,
                syncPendiente = true
            )
        )
    }

    suspend fun actualizar(cliente: ClienteEntity) {
        val ahora = System.currentTimeMillis()
        dao.actualizar(
            cliente.copy(
                actualizadoEn = ahora,
                syncPendiente = true
            )
        )
    }

    /**
     * Tombstone: marca como eliminado pero mantiene el registro local hasta
     * que el sync confirme el borrado en Firestore. Después se borra físico.
     */
    suspend fun eliminar(cliente: ClienteEntity) {
        dao.actualizar(
            cliente.copy(
                eliminado = true,
                actualizadoEn = System.currentTimeMillis(),
                syncPendiente = true
            )
        )
    }

    // ── API interna para el sync service ─────────────────────────────────

    suspend fun pendientesDeSync(): List<ClienteEntity> = dao.pendientesDeSync()

    suspend fun marcarSincronizado(id: Long) = dao.marcarSincronizado(id)

    suspend fun eliminarFisico(id: Long) = dao.eliminarFisico(id)

    /** Aplica un batch de cambios remotos sin volver a marcarlos como pending. */
    suspend fun guardarRemoto(clientes: List<ClienteEntity>) = dao.guardarVarios(clientes)
}
