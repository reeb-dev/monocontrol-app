package com.reeb.controlmonotributoar.domain.model

import com.reeb.controlmonotributoar.data.remote.TarifasService

/**
 * Categorías del Monotributo Argentina.
 *
 * Los valores numéricos (limiteAnual, cuotaMensual, cuotaVentaMuebles)
 * se leen dinámicamente desde [TarifasService] (que toma los datos del
 * JSON `app_config.json`, ya sea remoto o de assets).
 *
 * Los valores `default*` definidos en el enum son únicamente un
 * **respaldo** para el caso en que el JSON aún no esté cargado
 * (vigentes 01/02/2026 - 31/07/2026, fuente ARCA).
 */
enum class CategoriaMonotributo(
    val letra: String,
    /** Respaldo si el JSON no está cargado. */
    val defaultLimiteAnual: Double,
    val defaultCuotaServicios: Double,
    val defaultCuotaVentaMuebles: Double
) {
    A("A",  10_277_988.13,    42_386.74,   42_386.74),
    B("B", 15_058_447.71,    48_250.78,   48_250.78),
    C("C", 21_113_696.52,    56_501.85,   55_227.06),
    D("D", 26_212_853.42,    72_414.10,   70_661.26),
    E("E", 30_833_964.37,   102_537.97,   92_658.35),
    F("F", 38_642_048.36,   129_045.32,  111_198.27),
    G("G", 46_211_109.37,   197_108.23,  135_918.34),
    H("H", 70_113_407.33,   447_346.93,  272_063.40),
    I("I", 78_479_211.62,   824_802.26,  406_512.05),
    J("J", 89_872_640.30,   999_007.65,  497_059.41),
    K("K", 108_357_084.05, 1_381_687.90,  600_879.51);

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
