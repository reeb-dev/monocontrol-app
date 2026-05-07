package com.reeb.controlmonotributoar.data.remote

/**
 * Servicio singleton que centraliza el acceso a las tarifas vigentes
 * del Monotributo. Los datos provienen de [RemoteConfigHolder] (JSON
 * remoto + caché + assets fallback).
 *
 * Si no hay datos cargados (improbable, porque assets siempre está),
 * los métodos devuelven null y el enum [com.reeb.controlmonotributoar.domain.model.CategoriaMonotributo]
 * cae en sus valores hardcodeados de respaldo.
 *
 * Se inicializa con [actualizar] cada vez que [RemoteConfigRepository]
 * trae un nuevo [RemoteConfig].
 */
object TarifasService {

    @Volatile private var tarifasPorLetra: Map<String, RemoteConfig.TarifaCategoria> = emptyMap()

    /** Reemplaza las tarifas en memoria con las del JSON recibido. */
    fun actualizar(config: RemoteConfig) {
        tarifasPorLetra = config.tarifas.associateBy { it.letra.uppercase() }
    }

    /** Devuelve la tarifa de la letra solicitada (ej: "A", "B", "C"...). */
    fun tarifa(letra: String): RemoteConfig.TarifaCategoria? =
        tarifasPorLetra[letra.uppercase()]

    fun limiteAnual(letra: String): Double?       = tarifa(letra)?.limiteAnual
    fun cuotaServicios(letra: String): Double?    = tarifa(letra)?.cuotaServicios
    fun cuotaVentaMuebles(letra: String): Double? = tarifa(letra)?.cuotaVentaMuebles

    /** Lista de todas las letras disponibles en el JSON, en el orden definido. */
    fun letras(): List<String> = tarifasPorLetra.keys.toList()

    /** True si las tarifas ya fueron cargadas al menos una vez. */
    fun cargado(): Boolean = tarifasPorLetra.isNotEmpty()
}

