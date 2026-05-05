package com.manuelreeb.monocontrol.domain.model

/**
 * Tipo de usuario de la app. Permite simplificar la UI según para qué la usa cada uno.
 *
 * - [PERSONAL]: monotributista que controla su propia facturación.
 *               No necesita gestionar varios clientes.
 * - [CONTADOR]: profesional que administra varios clientes y necesita
 *               cambiar entre ellos, exportar Excel, simular escenarios, etc.
 */
enum class UserRole(val label: String, val descripcion: String) {
    PERSONAL(
        label = "Personal",
        descripcion = "Controlo mi propio Monotributo"
    ),
    CONTADOR(
        label = "Contador",
        descripcion = "Administro varios clientes"
    );

    companion object {
        fun fromName(value: String?): UserRole = runCatching {
            value?.let { valueOf(it) }
        }.getOrNull() ?: PERSONAL
    }
}

