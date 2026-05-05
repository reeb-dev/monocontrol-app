package com.manuelreeb.monocontrol.domain.usecase

import android.content.Context
import com.manuelreeb.monocontrol.data.repository.AlertaRepository
import com.manuelreeb.monocontrol.data.repository.MovimientoRepository
import com.manuelreeb.monocontrol.utils.AlertGenerator
import com.manuelreeb.monocontrol.utils.CategoriaCalculator
import com.manuelreeb.monocontrol.utils.DateUtils
import com.manuelreeb.monocontrol.utils.NotificacionHelper
import kotlinx.coroutines.flow.first

class GenerarAlertasUseCase(
    private val movimientoRepository: MovimientoRepository,
    private val alertaRepository: AlertaRepository,
    /**
     * Si se pasa un Context, además de guardar las alertas en la base
     * se dispara la notificación del sistema con el detalle de la
     * categoría actual y la próxima categoría a la que se pasaría.
     */
    private val appContext: Context? = null
) {
    suspend operator fun invoke() {
        val anio = DateUtils.anioActual()
        val totalAnual = movimientoRepository.obtenerTotalIngresosAnuales(anio).first()
        val porcentaje = CategoriaCalculator.calcularPorcentajeDelLimite(totalAnual)
        val categoriaActual = CategoriaCalculator.calcularCategoria(totalAnual)
        val proxima = CategoriaCalculator.proximaCategoria(totalAnual)

        val nuevasAlertas = AlertGenerator.generarAlertas(porcentaje, categoriaActual, proxima)
        alertaRepository.eliminarTodas()
        nuevasAlertas.forEach { alertaRepository.insertar(it) }

        // Si llegó al 70% o más, mostramos también notificación del sistema
        // aclarando categoría actual y a cuál podría pasar.
        if (porcentaje >= 0.70f) {
            appContext?.let { ctx ->
                NotificacionHelper.mostrarAlertaLimite(
                    context = ctx,
                    porcentaje = (porcentaje * 100).toInt(),
                    categoriaActual = categoriaActual,
                    proxima = proxima
                )
            }
        }
    }
}
