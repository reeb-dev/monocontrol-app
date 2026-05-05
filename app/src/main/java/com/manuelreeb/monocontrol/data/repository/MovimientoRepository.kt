package com.manuelreeb.monocontrol.data.repository

import com.manuelreeb.monocontrol.data.local.MovimientoDao
import com.manuelreeb.monocontrol.data.local.entity.MovimientoEntity
import com.manuelreeb.monocontrol.domain.model.Movimiento
import com.manuelreeb.monocontrol.domain.model.TipoMovimiento
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date

class MovimientoRepository(private val dao: MovimientoDao) {

    fun obtenerTodos(): Flow<List<Movimiento>> =
        dao.obtenerTodos().map { list -> list.map { it.toDomain() } }

    fun obtenerPorMesAnio(mes: Int, anio: Int): Flow<List<Movimiento>> =
        dao.obtenerPorMesAnio(
            mes = mes.toString().padStart(2, '0'),
            anio = anio.toString()
        ).map { list -> list.map { it.toDomain() } }

    fun obtenerTotalIngresosAnuales(anio: Int): Flow<Double> =
        dao.obtenerTotalIngresosAnuales(anio.toString())

    suspend fun agregar(movimiento: Movimiento) {
        dao.insertar(movimiento.toEntity())
    }

    suspend fun eliminar(movimiento: Movimiento) {
        dao.eliminar(movimiento.toEntity())
    }

    private fun MovimientoEntity.toDomain() = Movimiento(
        id = id,
        monto = monto,
        tipo = tipo,
        fecha = Date(fecha),
        descripcion = descripcion
    )

    private fun Movimiento.toEntity() = MovimientoEntity(
        id = id,
        monto = monto,
        tipo = tipo,
        fecha = fecha.time,
        descripcion = descripcion
    )
}

