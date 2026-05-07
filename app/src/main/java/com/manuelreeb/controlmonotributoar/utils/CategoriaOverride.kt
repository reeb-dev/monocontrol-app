package com.reeb.controlmonotributoar.utils

import com.reeb.controlmonotributoar.domain.model.CategoriaMonotributo
import com.reeb.controlmonotributoar.domain.model.ResumenMensual

/**
 * Permite forzar una categoría manualmente (modo prueba), útil para que
 * un contador o probador pueda ver cómo se comporta la app como si fuese
 * otro cliente, sin tocar los movimientos reales.
 */
object CategoriaOverride {

    /** Convierte la letra guardada (ej. "C") en su enum, o null si no aplica. */
    fun fromLetra(letra: String?): CategoriaMonotributo? =
        letra?.let { l -> CategoriaMonotributo.entries.firstOrNull { it.letra == l } }

    /**
     * Devuelve un nuevo ResumenMensual con la categoría sobrescrita
     * y el porcentaje recalculado contra el límite de la categoría forzada.
     */
    fun aplicar(resumen: ResumenMensual, override: CategoriaMonotributo?): ResumenMensual {
        if (override == null) return resumen
        val pct = (resumen.totalIngresos / override.limiteAnual)
            .toFloat()
            .coerceIn(0f, 1.5f)
        return resumen.copy(categoriaActual = override, porcentajeDelLimite = pct)
    }
}

