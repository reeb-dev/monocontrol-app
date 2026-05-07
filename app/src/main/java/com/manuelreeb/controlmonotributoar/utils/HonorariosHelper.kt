package com.reeb.controlmonotributoar.utils

import com.reeb.controlmonotributoar.data.local.entity.ClienteEntity
import java.util.Calendar
import java.util.Locale

object HonorariosHelper {

    fun periodoActual(now: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        return String.format(Locale.US, "%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    fun parsePagos(json: String): Map<String, Long> {
        if (json.isBlank()) return emptyMap()
        return json.split(",")
            .mapNotNull { token ->
                val parts = token.split(":")
                if (parts.size == 2) {
                    val periodo = parts[0]
                    val epoch = parts[1].toLongOrNull()
                    if (periodo.isNotBlank() && epoch != null) periodo to epoch else null
                } else null
            }
            .toMap()
    }

    fun registrarPago(json: String, periodo: String, pagoEpoch: Long): String {
        val map = parsePagos(json).toMutableMap()
        map[periodo] = pagoEpoch
        return map.entries.sortedBy { it.key }.joinToString(",") { "${it.key}:${it.value}" }
    }

    fun estaPagadoPeriodo(cliente: ClienteEntity, periodo: String = periodoActual()): Boolean {
        return parsePagos(cliente.honorarioPagosJson).containsKey(periodo)
    }

    fun esMesCobro(cliente: ClienteEntity, now: Long = System.currentTimeMillis()): Boolean {
        val frecuencia = cliente.honorarioFrecuencia.lowercase(Locale.ROOT)
        if (frecuencia == "mensual") return true
        if (frecuencia != "trimestral") return true

        val base = Calendar.getInstance().apply { timeInMillis = cliente.creadoEn }
        val actual = Calendar.getInstance().apply { timeInMillis = now }
        val baseMesAbs = base.get(Calendar.YEAR) * 12 + base.get(Calendar.MONTH)
        val mesAbs = actual.get(Calendar.YEAR) * 12 + actual.get(Calendar.MONTH)
        val diff = (mesAbs - baseMesAbs).coerceAtLeast(0)
        return diff % 3 == 0
    }

    fun fechaVencimientoDelMes(cliente: ClienteEntity, now: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        cal.set(Calendar.DAY_OF_MONTH, cliente.honorarioDiaVencimiento.coerceIn(1, 28))
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun diasHastaVencimiento(cliente: ClienteEntity, now: Long = System.currentTimeMillis()): Int {
        val inicioHoy = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val venc = fechaVencimientoDelMes(cliente, now)
        return ((venc - inicioHoy) / (1000L * 60 * 60 * 24)).toInt()
    }
}
