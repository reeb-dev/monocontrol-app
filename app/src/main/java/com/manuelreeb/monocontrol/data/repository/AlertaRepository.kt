package com.manuelreeb.monocontrol.data.repository

import com.manuelreeb.monocontrol.data.local.AlertaDao
import com.manuelreeb.monocontrol.data.local.entity.AlertaEntity
import com.manuelreeb.monocontrol.domain.model.Alerta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AlertaRepository(private val dao: AlertaDao) {

    fun obtenerTodas(): Flow<List<Alerta>> =
        dao.obtenerTodas().map { list -> list.map { it.toDomain() } }

    suspend fun insertar(alerta: Alerta) {
        dao.insertar(alerta.toEntity())
    }

    suspend fun marcarLeida(id: Long) = dao.marcarLeida(id)

    suspend fun eliminarTodas() = dao.eliminarTodas()

    private fun AlertaEntity.toDomain() = Alerta(id, mensaje, porcentaje, leida)
    private fun Alerta.toEntity() = AlertaEntity(id, mensaje, porcentaje, leida)
}

