package com.reeb.controlmonotributoar.domain.usecase

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.reeb.controlmonotributoar.data.local.entity.ClienteEntity
import com.reeb.controlmonotributoar.domain.model.CategoriaMonotributo
import com.reeb.controlmonotributoar.utils.ArcaInfo
import java.text.NumberFormat
import java.util.Locale

/**
 * Genera un Intent para enviar email de recordatorio de pago a un cliente.
 *
 * Acepta personalización del asunto, cuerpo del mensaje y adjuntos
 * (factura PDF generada + adjunto extra del contador, foto/PDF).
 */
class EnviarRecordatorioEmailUseCase {

    /**
     * @param asuntoCustom     si no está vacío, sobrescribe el asunto default
     * @param mensajeCustom    si no está vacío, sobrescribe el cuerpo default
     * @param adjuntos         lista de Uris a adjuntar al email (PDF, fotos, etc.)
     */
    operator fun invoke(
        context: Context,
        cliente: ClienteEntity,
        categoriaActual: CategoriaMonotributo,
        totalFacturadoAnual: Double,
        asuntoCustom: String = "",
        mensajeCustom: String = "",
        adjuntos: List<Uri> = emptyList()
    ): Intent? {
        if (cliente.email.isBlank()) return null

        val cuota = categoriaActual.cuotaSegunRubro(cliente.esVentaMuebles)
        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
        val cuotaFormateada = formatter.format(cuota)
        val totalFormateado = formatter.format(totalFacturadoAnual)

        val calendario = java.util.Calendar.getInstance()
        val mes = calendario.getDisplayName(
            java.util.Calendar.MONTH,
            java.util.Calendar.LONG,
            Locale("es", "AR")
        )
        val anio = calendario.get(java.util.Calendar.YEAR)

        val asunto = asuntoCustom.ifBlank {
            "Recordatorio: Vencimiento Monotributo - $mes $anio"
        }

        val cuerpo = mensajeCustom.ifBlank {
            """
            Hola ${cliente.nombre},

            Te recordamos que el vencimiento del Monotributo es el día 20 de $mes.

            📊 Resumen de tu situación:
            • Categoría actual: ${categoriaActual.letra}
            • Cuota mensual: $cuotaFormateada
            • Total facturado en el año: $totalFormateado
            • Límite anual de tu categoría: ${formatter.format(categoriaActual.limiteAnual)}

            💳 Podés pagar desde:
            ${ArcaInfo.PAGO_MONOTRIBUTO}

            Si tenés alguna duda, no dudes en consultarme.

            Saludos,
            Tu Contador
            """.trimIndent()
        }

        // Si hay adjuntos uso ACTION_SEND/SEND_MULTIPLE, sino mailto:
        return when {
            adjuntos.size > 1 -> Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(cliente.email))
                putExtra(Intent.EXTRA_SUBJECT, asunto)
                putExtra(Intent.EXTRA_TEXT, cuerpo)
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(adjuntos))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            adjuntos.size == 1 -> Intent(Intent.ACTION_SEND).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(cliente.email))
                putExtra(Intent.EXTRA_SUBJECT, asunto)
                putExtra(Intent.EXTRA_TEXT, cuerpo)
                putExtra(Intent.EXTRA_STREAM, adjuntos.first())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            else -> Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(cliente.email))
                putExtra(Intent.EXTRA_SUBJECT, asunto)
                putExtra(Intent.EXTRA_TEXT, cuerpo)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }
}
