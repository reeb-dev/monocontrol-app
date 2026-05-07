package com.reeb.controlmonotributoar.utils

import com.reeb.controlmonotributoar.domain.model.Alerta
import com.reeb.controlmonotributoar.domain.model.CategoriaMonotributo

object AlertGenerator {

    /**
     * Genera las alertas a mostrar en pantalla teniendo en cuenta no sólo el
     * porcentaje, sino también la categoría actual y la siguiente, así el
     * mensaje le dice al usuario CUÁL es la categoría en la que está y a
     * cuál podría pasar si sigue facturando al ritmo actual.
     */
    fun generarAlertas(
        porcentaje: Float,
        categoriaActual: CategoriaMonotributo? = null,
        proxima: CategoriaMonotributo? = null,
        alertaRiesgo70: Boolean = true,
        alertaRiesgo80: Boolean = true,
        alertaRiesgo90: Boolean = true
    ): List<Alerta> {
        val alertas = mutableListOf<Alerta>()
        val pct = (porcentaje * 100).toInt()
        val catTxt = categoriaActual?.let { " (cat. ${it.letra})" } ?: ""
        val proxTxt = proxima?.let { " Próxima categoría: ${it.letra}." }
            ?: " Estás en la categoría más alta (K): si la superás quedás fuera del régimen."

        when {
            porcentaje >= 1.0f -> alertas.add(
                Alerta(
                    mensaje = "⛔ Superaste el límite de tu categoría$catTxt. Tenés que recategorizarte.$proxTxt",
                    porcentaje = porcentaje
                )
            )
            porcentaje >= 0.90f && alertaRiesgo90 -> alertas.add(
                Alerta(
                    mensaje = "🚨 Estás al $pct% del límite$catTxt. ¡Muy cerca de recategorizarte!$proxTxt",
                    porcentaje = porcentaje
                )
            )
            porcentaje >= 0.80f && alertaRiesgo80 -> alertas.add(
                Alerta(
                    mensaje = "⚠️ Estás al $pct% del límite$catTxt. Revisá tu facturación.$proxTxt",
                    porcentaje = porcentaje
                )
            )
            porcentaje >= 0.70f && alertaRiesgo70 -> alertas.add(
                Alerta(
                    mensaje = "🔔 Llegaste al $pct% del límite anual$catTxt. Empezá a monitorear tus ingresos.$proxTxt",
                    porcentaje = porcentaje
                )
            )
        }

        return alertas
    }
}
