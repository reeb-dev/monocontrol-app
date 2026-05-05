package com.manuelreeb.monocontrol.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.manuelreeb.monocontrol.ui.historial.DatoMensual
import java.io.File

object ExportarDatosHelper {

    /**
     * Genera un CSV con el historial y abre el selector de apps para compartirlo.
     */
    fun exportarComoCSV(context: Context, meses: List<DatoMensual>, anio: Int) {
        val contenido = buildString {
            appendLine("Mes,Facturado,Límite,Porcentaje")
            meses.forEach { dato ->
                appendLine(
                    "${dato.mes},${dato.facturado},${dato.limite}," +
                        "${String.format("%.1f", dato.porcentaje * 100)}%"
                )
            }
            appendLine(
                "TOTAL,${meses.sumOf { it.facturado }}," +
                    "${meses.firstOrNull()?.limite?.times(12) ?: 0}"
            )
        }

        val archivo = File(context.cacheDir, "historial_monotributo_$anio.csv")
        archivo.writeText(contenido)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Historial Monotributo $anio")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Exportar historial"))
    }
}

