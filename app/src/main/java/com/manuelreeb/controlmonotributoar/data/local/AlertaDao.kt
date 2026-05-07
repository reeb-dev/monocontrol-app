package com.reeb.controlmonotributoar.data.local

import androidx.room.*
import com.reeb.controlmonotributoar.data.local.entity.AlertaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertaDao {

    @Insert
    suspend fun insertar(alerta: AlertaEntity): Long

    @Query("SELECT * FROM alertas ORDER BY id DESC")
    fun obtenerTodas(): Flow<List<AlertaEntity>>

    @Query("UPDATE alertas SET leida = 1 WHERE id = :id")
    suspend fun marcarLeida(id: Long)

    @Query("DELETE FROM alertas")
    suspend fun eliminarTodas()
}

