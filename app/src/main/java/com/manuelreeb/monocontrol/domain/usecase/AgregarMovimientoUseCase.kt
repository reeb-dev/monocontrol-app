package com.manuelreeb.monocontrol.domain.usecase

import com.manuelreeb.monocontrol.data.repository.MovimientoRepository
import com.manuelreeb.monocontrol.domain.model.Movimiento
import com.manuelreeb.monocontrol.domain.model.TipoMovimiento
import java.util.Date

class AgregarMovimientoUseCase(private val repository: MovimientoRepository) {
    suspend operator fun invoke(
        monto: Double,
        tipo: TipoMovimiento,
        descripcion: String,
        fecha: Date = Date()
    ) {
        require(monto > 0) { "El monto debe ser mayor a cero" }
        repository.agregar(
            Movimiento(monto = monto, tipo = tipo, fecha = fecha, descripcion = descripcion)
        )
    }
}

