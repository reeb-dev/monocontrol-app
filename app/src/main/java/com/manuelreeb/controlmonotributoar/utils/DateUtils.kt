package com.reeb.controlmonotributoar.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("es", "AR"))
    private val sdfArchivo = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun formatear(date: Date): String = sdf.format(date)
    fun formatearNombreArchivo(date: Date): String = sdfArchivo.format(date)

    fun parseFecha(texto: String): Date? = try { sdf.parse(texto) } catch (e: Exception) { null }

    fun mesActual(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1

    fun anioActual(): Int = Calendar.getInstance().get(Calendar.YEAR)

    fun nombreMes(mes: Int): String {
        val meses = listOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
        return meses.getOrElse(mes - 1) { "?" }
    }
}

