package com.reeb.controlmonotributoar.domain.model

import java.util.Date

data class Movimiento(
    val id: Long = 0,
    val monto: Double,
    val tipo: TipoMovimiento,
    val fecha: Date,
    val descripcion: String
)

