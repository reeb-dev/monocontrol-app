package com.manuelreeb.monocontrol.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alertas")
data class AlertaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mensaje: String,
    val porcentaje: Float,
    val leida: Boolean = false
)

