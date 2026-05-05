package com.manuelreeb.monocontrol.data.local.entity

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
    /** PERSONAL o CONTADOR. Se usa para simplificar la UI según el tipo de usuario. */
    val rol: String = "PERSONAL"
)
