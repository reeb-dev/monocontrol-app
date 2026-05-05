package com.manuelreeb.monocontrol.domain.model

data class Alerta(
    val id: Long = 0,
    val mensaje: String,
    val porcentaje: Float,
    val leida: Boolean = false
)

