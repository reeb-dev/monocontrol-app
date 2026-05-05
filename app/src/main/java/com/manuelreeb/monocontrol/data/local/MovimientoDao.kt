package com.manuelreeb.monocontrol.data.local

import androidx.room.*
import com.manuelreeb.monocontrol.data.local.entity.MovimientoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoDao {

    @Insert
    suspend fun insertar(movimiento: MovimientoEntity): Long

    @Delete
    suspend fun eliminar(movimiento: MovimientoEntity)

    @Query("SELECT * FROM movimientos ORDER BY fecha DESC")
    fun obtenerTodos(): Flow<List<MovimientoEntity>>

    @Query("""
        SELECT * FROM movimientos 
        WHERE strftime('%m', fecha / 1000, 'unixepoch') = :mes 
          AND strftime('%Y', fecha / 1000, 'unixepoch') = :anio
        ORDER BY fecha DESC
    """)
    fun obtenerPorMesAnio(mes: String, anio: String): Flow<List<MovimientoEntity>>

    @Query("""
        SELECT COALESCE(SUM(monto), 0) FROM movimientos
        WHERE tipo = 'INGRESO'
          AND strftime('%Y', fecha / 1000, 'unixepoch') = :anio
    """)
    fun obtenerTotalIngresosAnuales(anio: String): Flow<Double>
}

