package com.manuelreeb.monocontrol.domain.model

import com.manuelreeb.monocontrol.data.remote.TarifasService

/**
 * Categorías del Monotributo Argentina.
 *
 * Los valores numéricos (limiteAnual, cuotaMensual, cuotaVentaMuebles)
 * se leen dinámicamente desde [TarifasService] (que toma los datos del
 * JSON `app_config.json`, ya sea remoto o de assets).
 *
 * Los valores `default*` definidos en el enum son únicamente un
 * **respaldo** para el caso en que el JSON aún no esté cargado
 * (vigentes 01/08/2025 - 31/01/2026, fuente ARCA/AFIP).
 */
enum class CategoriaMonotributo(
    val letra: String,
    /** Respaldo si el JSON no está cargado. */
    val defaultLimiteAnual: Double,
    val defaultCuotaServicios: Double,
    val defaultCuotaVentaMuebles: Double
) {
    A("A",  8_992_597.87,    37_085.74,   37_085.74),
    B("B", 13_175_201.52,    42_216.41,   42_216.41),
    C("C", 18_473_166.15,    49_435.58,   48_320.22),
    D("D", 22_934_610.05,    63_357.80,   61_824.18),
    E("E", 26_977_793.60,    89_714.31,   81_070.26),
    F("F", 33_809_379.57,   112_906.59,   97_291.54),
    G("G", 40_431_835.35,   172_457.38,  118_920.05),
    H("H", 61_344_853.64,   391_400.62,  238_038.48),
    I("I", 68_664_410.05,   721_650.46,  355_672.64),
    J("J", 78_632_948.76,   874_069.29,  434_895.92),
    K("K", 94_805_682.90, 1_208_890.60,  525_732.01);

    /** Ingresos brutos máximos anuales (toma el valor del JSON si está cargado). */
    val limiteAnual: Double
        get() = TarifasService.limiteAnual(letra) ?: defaultLimiteAnual

    /** Cuota mensual total para Locaciones y/o prestaciones de servicios. */
    val cuotaMensual: Double
        get() = TarifasService.cuotaServicios(letra) ?: defaultCuotaServicios

    /** Cuota mensual total para Venta de cosas muebles. */
    val cuotaVentaMuebles: Double
        get() = TarifasService.cuotaVentaMuebles(letra) ?: defaultCuotaVentaMuebles

    /** Devuelve la cuota mensual correcta según el rubro del contribuyente. */
    fun cuotaSegunRubro(esVentaMuebles: Boolean): Double =
        if (esVentaMuebles) cuotaVentaMuebles else cuotaMensual

    companion object {
        fun porLimiteAnual(facturacionAnual: Double): CategoriaMonotributo {
            return entries.firstOrNull { facturacionAnual <= it.limiteAnual }
                ?: K
        }
    }
}
