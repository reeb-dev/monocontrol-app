package com.reeb.controlmonotributoar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Perfil del usuario. Tabla single-row: siempre id = 1.
 */
@Entity(tableName = "perfil")
data class PerfilEntity(
    @PrimaryKey
    val id: Int = 1,
    val nombre: String = "",
    val cuit: String = "",
    /** true = venta de cosas muebles · false = locaciones / prestación de servicios */
    val esVentaMuebles: Boolean = false,
    /**
     * Modo prueba: si no es null, fuerza esta categoría (letra A-K) sin importar la facturación.
     * Útil para que un contador pueda revisar la app como si fuera otro cliente.
     */
    val categoriaOverride: String? = null,
    /** Email del usuario logueado en Firebase (solo informativo). */
    val email: String = "",
    /** Teléfono opcional de contacto para recordatorios rápidos. */
    val telefono: String = "",
    /** PERSONAL o CONTADOR. Se usa para simplificar la UI según el tipo de usuario. */
    val rol: String = "PERSONAL",
    /** Preferencias de alertas por vencimiento mensual. */
    val alertaVencimiento30d: Boolean = true,
    val alertaVencimiento15d: Boolean = true,
    val alertaVencimiento7d: Boolean = true,
    val alertaVencimiento1d: Boolean = true,
    /** Preferencias de alertas por riesgo de recategorización. */
    val alertaRiesgo70: Boolean = true,
    val alertaRiesgo80: Boolean = true,
    val alertaRiesgo90: Boolean = true
)
