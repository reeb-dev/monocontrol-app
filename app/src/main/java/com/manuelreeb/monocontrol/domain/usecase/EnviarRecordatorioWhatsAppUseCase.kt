package com.manuelreeb.monocontrol.domain.usecase

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.manuelreeb.monocontrol.data.local.entity.ClienteEntity
import com.manuelreeb.monocontrol.domain.model.CategoriaMonotributo
import java.text.NumberFormat
import java.util.Locale

/**
 * Genera un Intent para enviar un mensaje de WhatsApp al cliente.
 *
 * Si WhatsApp está instalado usa el deep-link nativo `whatsapp://send?phone=...`.
 * Si no, cae al deep-link universal `https://wa.me/...` que abre el browser.
 *
 * IMPORTANTE: WhatsApp NO permite adjuntar archivos vía deep-link.
 * Para adjuntar PDF/imagen se usa ACTION_SEND con `package = "com.whatsapp"`
 * lo que abre el selector de chat de WhatsApp con el archivo prelistado.
 */
class EnviarRecordatorioWhatsAppUseCase {

    /**
     * @param numero        número con código de país, sin "+" ni espacios. Ej: 5491122334455
     * @param mensajeCustom mensaje a enviar. Si está vacío usa uno por defecto.
     * @param adjunto       Uri opcional. Si está presente, abre el composer
     *                      de WhatsApp con el archivo adjunto.
     */
    operator fun invoke(
        context: Context,
        cliente: ClienteEntity,
        categoriaActual: CategoriaMonotributo,
        totalFacturadoAnual: Double,
        numero: String,
        mensajeCustom: String = "",
        adjunto: Uri? = null
    ): Intent? {
        val numeroLimpio = numero.filter { it.isDigit() }
        if (numeroLimpio.isBlank()) return null

        val mensaje = mensajeCustom.ifBlank {
            buildMensajeDefault(cliente, categoriaActual, totalFacturadoAnual)
        }

        // Caso 1: hay adjunto → usar ACTION_SEND con package whatsapp.
        if (adjunto != null) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_TEXT, mensaje)
                putExtra(Intent.EXTRA_STREAM, adjunto)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            // Preferir WhatsApp si está instalado, sino dejar al usuario elegir.
            if (estaInstalado(context, "com.whatsapp")) {
                intent.setPackage("com.whatsapp")
            } else if (estaInstalado(context, "com.whatsapp.w4b")) {
                intent.setPackage("com.whatsapp.w4b")
            }
            return intent
        }

        // Caso 2: sólo mensaje → deep-link directo al chat.
        val urlNativa = "whatsapp://send?phone=$numeroLimpio&text=${Uri.encode(mensaje)}"
        val urlUniversal = "https://wa.me/$numeroLimpio?text=${Uri.encode(mensaje)}"

        val intentNativo = Intent(Intent.ACTION_VIEW, Uri.parse(urlNativa)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return if (intentNativo.resolveActivity(context.packageManager) != null) {
            intentNativo
        } else {
            Intent(Intent.ACTION_VIEW, Uri.parse(urlUniversal)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }

    private fun estaInstalado(context: Context, pkg: String): Boolean = try {
        context.packageManager.getPackageInfo(pkg, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    private fun buildMensajeDefault(
        cliente: ClienteEntity,
        cat: CategoriaMonotributo,
        totalAnual: Double
    ): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
        val cuota = cat.cuotaSegunRubro(cliente.esVentaMuebles)
        val cal = java.util.Calendar.getInstance()
        val mes = cal.getDisplayName(
            java.util.Calendar.MONTH,
            java.util.Calendar.LONG,
            Locale("es", "AR")
        )
        return """
            Hola ${cliente.nombre} 👋

            Recordatorio: el día 20 de $mes vence tu Monotributo.

            • Categoría: ${cat.letra}
            • Cuota: ${formatter.format(cuota)}
            • Facturado en el año: ${formatter.format(totalAnual)}

            💳 Pagar: https://monotributo.afip.gob.ar/

            Cualquier duda me avisás.
        """.trimIndent()
    }
}

