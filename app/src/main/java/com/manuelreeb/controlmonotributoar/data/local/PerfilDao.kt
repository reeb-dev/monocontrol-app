package com.reeb.controlmonotributoar.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.reeb.controlmonotributoar.data.local.entity.PerfilEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PerfilDao {

    @Query("SELECT * FROM perfil WHERE id = 1 LIMIT 1")
    fun observar(): Flow<PerfilEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(perfil: PerfilEntity)

    @Query("DELETE FROM perfil")
    suspend fun eliminarTodo()
}
