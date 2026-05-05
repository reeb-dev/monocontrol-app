package com.manuelreeb.monocontrol.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.manuelreeb.monocontrol.data.local.entity.ClienteEntity
import com.manuelreeb.monocontrol.domain.model.CategoriaMonotributo
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Genera un PDF con la factura/recordatorio de pago del Monotributo.
 *
 * El PDF incluye:
 * - Encabezado con logo/título
 * - Datos del cliente (nombre, CUIT)
 * - Categoría y cuota mensual
 * - Fecha de vencimiento (día 20 del mes actual)
 * - Código de barras ficticio o QR (simplificado)
 * - Instrucciones de pago
 *
 * @return Uri del archivo PDF generado, listo para adjuntar al email
 */
class GenerarFacturaPDFUseCase {

    companion object {
        private const val PAGE_WIDTH = 595 // A4 width in points
        private const val PAGE_HEIGHT = 842 // A4 height in points
        private const val MARGIN = 50f
    }

    operator fun invoke(
        context: Context,
        cliente: ClienteEntity,
        categoria: CategoriaMonotributo,
        totalFacturadoAnual: Double
    ): Uri? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            drawFactura(canvas, cliente, categoria, totalFacturadoAnual)

            pdfDocument.finishPage(page)

            // Guardar en directorio de caché
            val fileName = "Factura_Monotributo_${cliente.nombre.replace(" ", "_")}_${
                SimpleDateFormat("yyyyMM", Locale.getDefault()).format(Date())
            }.pdf"

            val cacheDir = File(context.cacheDir, "facturas")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val file = File(cacheDir, fileName)
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()

            // Generar Uri con FileProvider para compartir
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun drawFactura(
        canvas: Canvas,
        cliente: ClienteEntity,
        categoria: CategoriaMonotributo,
        totalAnual: Double
    ) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
        val cuota = categoria.cuotaSegunRubro(cliente.esVentaMuebles)

        // Obtener fecha de vencimiento (día 20 del mes actual)
        val calendario = Calendar.getInstance()
        val mes = calendario.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale("es", "AR"))
        val año = calendario.get(Calendar.YEAR)
        val fechaVencimiento = "20 de $mes de $año"

        var y = MARGIN

        // === ENCABEZADO ===
        Paint().apply {
            color = Color.rgb(117, 170, 219) // Celeste
            style = Paint.Style.FILL
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 100f, this)
        }

        Paint().apply {
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            canvas.drawText("MONOTRIBUTO - RECORDATORIO DE PAGO", PAGE_WIDTH / 2f, 50f, this)
        }

        Paint().apply {
            color = Color.WHITE
            textSize = 14f
            textAlign = Paint.Align.CENTER
            canvas.drawText("Vencimiento: $fechaVencimiento", PAGE_WIDTH / 2f, 80f, this)
        }

        y = 130f

        // === DATOS DEL CLIENTE ===
        drawSection(canvas, "DATOS DEL CONTRIBUYENTE", y)
        y += 40f

        drawField(canvas, "Nombre/Razón Social:", cliente.nombre, y)
        y += 30f

        if (cliente.cuit.isNotBlank()) {
            drawField(canvas, "CUIT:", cliente.cuit, y)
            y += 30f
        }

        if (cliente.email.isNotBlank()) {
            drawField(canvas, "Email:", cliente.email, y)
            y += 30f
        }

        y += 20f

        // === DETALLE DE LA FACTURA ===
        drawSection(canvas, "DETALLE DEL PERÍODO", y)
        y += 40f

        drawField(canvas, "Categoría:", categoria.letra, y)
        y += 30f

        drawField(canvas, "Rubro:", if (cliente.esVentaMuebles) "Venta de cosas muebles" else "Locaciones y servicios", y)
        y += 30f

        drawField(canvas, "Cuota Mensual:", formatter.format(cuota), y)
        y += 30f

        drawField(canvas, "Total Facturado en el Año:", formatter.format(totalAnual), y)
        y += 30f

        drawField(canvas, "Límite Anual Categoría ${categoria.letra}:", formatter.format(categoria.limiteAnual), y)
        y += 40f

        // === TOTAL A PAGAR ===
        Paint().apply {
            color = Color.rgb(251, 184, 28) // Amarillo
            style = Paint.Style.FILL
            canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 50f, this)
        }

        Paint().apply {
            color = Color.rgb(13, 42, 74) // Texto oscuro
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            canvas.drawText("TOTAL A PAGAR: ${formatter.format(cuota)}", PAGE_WIDTH / 2f, y + 35f, this)
        }

        y += 80f

        // === INSTRUCCIONES DE PAGO ===
        drawSection(canvas, "INSTRUCCIONES DE PAGO", y)
        y += 40f

        Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            val instrucciones = listOf(
                "1. Ingresá a https://monotributo.afip.gob.ar/ con tu CUIT y Clave Fiscal",
                "2. Seleccioná 'Pagar Cuota Mensual'",
                "3. Elegí el período correspondiente",
                "4. Descargá el VEP (Volante Electrónico de Pago)",
                "5. Pagá en cualquier banco, cajero automático o homebanking",
                "",
                "Recordá que el vencimiento es el día 20 de cada mes."
            )

            instrucciones.forEach { linea ->
                canvas.drawText(linea, MARGIN + 10f, y, this)
                y += 25f
            }
        }

        // === FOOTER ===
        y = PAGE_HEIGHT - 80f
        Paint().apply {
            color = Color.LTGRAY
            textSize = 10f
            textAlign = Paint.Align.CENTER
            canvas.drawText("Este documento es informativo. Para realizar el pago oficial, ingresá a www.afip.gob.ar", PAGE_WIDTH / 2f, y, this)
        }

        Paint().apply {
            color = Color.LTGRAY
            textSize = 9f
            textAlign = Paint.Align.CENTER
            canvas.drawText("Generado el ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}",
                PAGE_WIDTH / 2f, y + 15f, this)
        }
    }

    private fun drawSection(canvas: Canvas, title: String, y: Float) {
        Paint().apply {
            color = Color.rgb(74, 134, 200) // Celeste oscuro
            style = Paint.Style.FILL
            canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 30f, this)
        }

        Paint().apply {
            color = Color.WHITE
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(title, MARGIN + 10f, y + 20f, this)
        }
    }

    private fun drawField(canvas: Canvas, label: String, value: String, y: Float) {
        Paint().apply {
            color = Color.rgb(13, 42, 74)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(label, MARGIN + 10f, y, this)
        }

        Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(value, MARGIN + 200f, y, this)
        }
    }
}

