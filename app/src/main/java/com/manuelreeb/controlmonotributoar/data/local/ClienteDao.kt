package com.reeb.controlmonotributoar.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.reeb.controlmonotributoar.data.local.entity.ClienteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {

    /** Sólo lista clientes NO eliminados (los eliminados quedan tombstoned para sync). */
    @Query("SELECT * FROM cliente WHERE eliminado = 0 ORDER BY nombre COLLATE NOCASE ASC")
    fun observarTodos(): Flow<List<ClienteEntity>>

    @Query(
        "SELECT * FROM cliente " +
            "WHERE eliminado = 0 AND (" +
            "       nombre LIKE '%' || :q || '%' COLLATE NOCASE " +
            "    OR cuit   LIKE '%' || :q || '%' " +
            ")" +
            "ORDER BY nombre COLLATE NOCASE ASC"
    )
    fun buscar(q: String): Flow<List<ClienteEntity>>

    @Query("SELECT * FROM cliente WHERE id = :id LIMIT 1")
    suspend fun porId(id: Long): ClienteEntity?

    /** Para sync: TODOS los clientes con cambios pendientes (incluye eliminados). */
    @Query("SELECT * FROM cliente WHERE syncPendiente = 1")
    suspend fun pendientesDeSync(): List<ClienteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(cliente: ClienteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarVarios(clientes: List<ClienteEntity>)

    @Update
    suspend fun actualizar(cliente: ClienteEntity)

    @Delete
    suspend fun eliminar(cliente: ClienteEntity)

    /** Borrado físico definitivo (sólo después de confirmar sync). */
    @Query("DELETE FROM cliente WHERE id = :id")
    suspend fun eliminarFisico(id: Long)

    @Query("UPDATE cliente SET syncPendiente = 0 WHERE id = :id")
    suspend fun marcarSincronizado(id: Long)

    @Query("DELETE FROM cliente")
    suspend fun eliminarTodo()
}
