package com.manuelreeb.monocontrol.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.manuelreeb.monocontrol.domain.model.TipoMovimiento

@Entity(tableName = "movimientos")
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val monto: Double,
    val tipo: TipoMovimiento,
    val fecha: Long, // epoch millis
    val descripcion: String
)

