package com.manuelreeb.monocontrol.data.remote

/**
 * Modelo en memoria del archivo `app_config.json`.
 * Las tarifas y los datos de donaciones se cargan desde:
 *   1) Caché en SharedPreferences (lo último descargado).
 *   2) Red (URL remota configurable).
 *   3) `assets/app_config.json` como fallback.
 */
data class RemoteConfig(
    val version: Int,
    val actualizadoEl: String,
    val fuente: String,
    val tarifas: List<TarifaCategoria>,
    val donaciones: Donaciones
) {
    data class TarifaCategoria(
        val letra: String,
        val limiteAnual: Double,
        val cuotaServicios: Double,
        val cuotaVentaMuebles: Double
    )

    data class Donaciones(
        val mensaje: String,
        val aliasMercadoPago: String,
        val cbu: String,
        val cafecitoUrl: String
    )
}

