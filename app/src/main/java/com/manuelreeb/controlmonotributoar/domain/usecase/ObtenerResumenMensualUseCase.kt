package com.reeb.controlmonotributoar.domain.usecase

import com.reeb.controlmonotributoar.data.repository.MovimientoRepository
import com.reeb.controlmonotributoar.domain.model.CategoriaMonotributo
import com.reeb.controlmonotributoar.domain.model.ResumenMensual
import com.reeb.controlmonotributoar.domain.model.TipoMovimiento
import com.reeb.controlmonotributoar.utils.CategoriaCalculator
import com.reeb.controlmonotributoar.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObtenerResumenMensualUseCase(private val repository: MovimientoRepository) {

    operator fun invoke(
        mes: Int = DateUtils.mesActual(),
        anio: Int = DateUtils.anioActual()
    ): Flow<ResumenMensual> {
        val movimientosMes = repository.obtenerPorMesAnio(mes, anio)
        val ingresosAnuales = repository.obtenerTotalIngresosAnuales(anio)

        return combine(movimientosMes, ingresosAnuales) { movimientos, totalAnual ->
            val ingresosMes = movimientos
                .filter { it.tipo == TipoMovimiento.INGRESO }
                .sumOf { it.monto }
            val gastosMes = movimientos
                .filter { it.tipo == TipoMovimiento.GASTO }
                .sumOf { it.monto }

            val categoria = CategoriaCalculator.calcularCategoria(totalAnual)
            val porcentaje = CategoriaCalculator.calcularPorcentajeDelLimite(totalAnual)

            ResumenMensual(
                mes = mes,
                anio = anio,
                totalIngresos = ingresosMes,
                totalGastos = gastosMes,
                totalIngresosAnual = totalAnual,
                categoriaActual = categoria,
                porcentajeDelLimite = porcentaje
            )
        }
    }
}
