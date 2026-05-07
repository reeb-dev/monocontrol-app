package com.reeb.controlmonotributoar.data.repository

import com.reeb.controlmonotributoar.data.local.PerfilDao
import com.reeb.controlmonotributoar.data.local.entity.PerfilEntity
import kotlinx.coroutines.flow.Flow

class PerfilRepository(private val dao: PerfilDao) {

    fun observar(): Flow<PerfilEntity?> = dao.observar()

    suspend fun guardar(perfil: PerfilEntity) = dao.guardar(perfil)

    suspend fun eliminarTodo() = dao.eliminarTodo()
}
