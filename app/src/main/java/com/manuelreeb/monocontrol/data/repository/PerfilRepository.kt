package com.manuelreeb.monocontrol.data.repository

import com.manuelreeb.monocontrol.data.local.PerfilDao
import com.manuelreeb.monocontrol.data.local.entity.PerfilEntity
import kotlinx.coroutines.flow.Flow

class PerfilRepository(private val dao: PerfilDao) {

    fun observar(): Flow<PerfilEntity?> = dao.observar()

    suspend fun guardar(perfil: PerfilEntity) = dao.guardar(perfil)
}

