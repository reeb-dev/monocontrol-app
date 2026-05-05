package com.manuelreeb.monocontrol.utils

import java.util.Calendar

/**
 * Helper para manejar el historial de pagos del Monotributo.
 *
 * El JSON es un texto compacto "YYYY-MM:estado,YYYY-MM:estado,..."
 * donde estado = "pagado" | "pendiente" | "vencido".
 */
object PagosMonotributoHelper {

    enum class EstadoPago(val label: String, val emoji: String) {
        PAGADO("Pagado", "✅"),
        PENDIENTE("Pendiente", "⏳"),
        VENCIDO("Vencido", "❌");
    }

    /** Parsea el JSON a un Map de "YYYY-MM" → EstadoPago. */
    fun parsear(json: String): Map<String, EstadoPago> {
        if (json.isBlank()) return emptyMap()
        return json.split(",")
            .mapNotNull { entry ->
                val parts = entry.trim().split(":")
                if (parts.size == 2) {
                    val periodo = parts[0].trim()
                    val estado = when (parts[1].trim()) {
                        "pagado"   -> EstadoPago.PAGADO
                        "vencido"  -> EstadoPago.VENCIDO
                        else       -> EstadoPago.PENDIENTE
                    }
                    periodo to estado
                } else null
            }
            .toMap()
    }

    /** Serializa el Map de vuelta al formato JSON compacto. */
    fun serializar(pagos: Map<String, EstadoPago>): String =
        pagos.entries.joinToString(",") { "${it.key}:${it.value.name.lowercase()}" }

    /** Retorna el periodo actual en formato "YYYY-MM". */
    fun periodoActual(): String {
        val cal = Calendar.getInstance()
        val anio = cal.get(Calendar.YEAR)
        val mes = cal.get(Calendar.MONTH) + 1
        return "$anio-${mes.toString().padStart(2, '0')}"
    }

    /** Genera los últimos N periodos (incluye el actual), más reciente primero. */
    fun ultimosPeriodos(cantidad: Int = 12): List<String> {
        val cal = Calendar.getInstance()
        return (0 until cantidad).map {
            val anio = cal.get(Calendar.YEAR)
            val mes  = cal.get(Calendar.MONTH) + 1
            val periodo = "$anio-${mes.toString().padStart(2, '0')}"
            cal.add(Calendar.MONTH, -1)
            periodo
        }
    }

    /**
     * Actualiza el estado de un periodo y devuelve el nuevo JSON.
     * Si el estado es null, elimina el periodo del registro.
     */
    fun actualizarPeriodo(json: String, periodo: String, estado: EstadoPago?): String {
        val pagos = parsear(json).toMutableMap()
        if (estado == null) pagos.remove(periodo) else pagos[periodo] = estado
        return serializar(pagos)
    }

    /**
     * Dado el JSON, devuelve el estado del periodo indicado.
     * Si no hay registro, devuelve PENDIENTE.
     */
    fun estadoDe(json: String, periodo: String): EstadoPago =
        parsear(json)[periodo] ?: EstadoPago.PENDIENTE

    /** Retorna el nombre legible del periodo "YYYY-MM" → "Abril 2026". */
    fun nombrePeriodo(periodo: String): String {
        val parts = periodo.split("-")
        if (parts.size != 2) return periodo
        val anio = parts[0]
        val mes = parts[1].toIntOrNull() ?: return periodo
        val meses = listOf("", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        return "${meses.getOrElse(mes) { mes.toString() }} $anio"
    }
}

