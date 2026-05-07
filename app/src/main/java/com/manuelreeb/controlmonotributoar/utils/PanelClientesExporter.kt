package com.reeb.controlmonotributoar.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.reeb.controlmonotributoar.ui.panelclientes.ClienteStats
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.util.Date

object PanelClientesExporter {
    fun exportarRankingExcel(context: Context, clientes: List<ClienteStats>): Intent {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Ranking Riesgo")
        val header = sheet.createRow(0)
        listOf("Cliente", "Categoría", "Score", "% límite", "Facturación anual", "Días a vencimiento", "Tendencia")
            .forEachIndexed { i, title -> header.createCell(i).setCellValue(title) }

        clientes.sortedByDescending { it.scoreRiesgo }.forEachIndexed { idx, c ->
            val row = sheet.createRow(idx + 1)
            row.createCell(0).setCellValue(c.cliente.nombre)
            row.createCell(1).setCellValue(c.cliente.categoria ?: "Auto")
            row.createCell(2).setCellValue(c.scoreRiesgo.toDouble())
            row.createCell(3).setCellValue((c.porcentajeLimite * 100).toDouble())
            row.createCell(4).setCellValue(c.facturacionAnio)
            row.createCell(5).setCellValue(c.diasAVencimiento?.toString() ?: "N/D")
            row.createCell(6).setCellValue(c.tendencia)
        }
        (0..6).forEach { sheet.autoSizeColumn(it) }

        val dir = File(context.cacheDir, "panel_exports").apply { mkdirs() }
        val file = File(dir, "ranking_clientes_${DateUtils.formatearNombreArchivo(Date())}.xlsx")
        file.outputStream().use { workbook.write(it) }
        workbook.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Ranking de clientes críticos")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun exportarRankingPdf(context: Context, clientes: List<ClienteStats>): Intent {
        return exportarPdfInterno(
            context = context,
            titulo = "Ranking de clientes críticos",
            clientes = clientes
        )
    }

    fun exportarCriticosSemanaPdf(context: Context, clientes: List<ClienteStats>): Intent {
        return exportarPdfInterno(
            context = context,
            titulo = "Clientes críticos de la semana",
            clientes = clientes
        )
    }

    private fun exportarPdfInterno(
        context: Context,
        titulo: String,
        clientes: List<ClienteStats>
    ): Intent {
        val pdf = PdfDocument()
        val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas: Canvas = page.canvas
        val pHeaderBg = Paint().apply { color = Color.rgb(117, 170, 219) }
        val pTitle = Paint().apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val pRow = Paint().apply {
            color = Color.DKGRAY
            textSize = 11f
        }
        val pRisk = Paint().apply { style = Paint.Style.FILL }
        var y = 40f
        canvas.drawRect(0f, 0f, 595f, 70f, pHeaderBg)
        canvas.drawText(titulo, 24f, y, pTitle)
        y += 18f
        canvas.drawText("Generado: ${DateUtils.formatear(Date())}", 24f, y, pTitle)
        y += 28f
        canvas.drawText("Cliente | Cat. | Score | %Límite | Facturación anual | Vto", 40f, y, pRow)
        y += 12f
        clientes.sortedByDescending { it.scoreRiesgo }.take(30).forEachIndexed { idx, c ->
            y += 16f
            pRisk.color = when {
                c.scoreRiesgo >= 75 -> Color.rgb(229, 57, 53)
                c.scoreRiesgo >= 45 -> Color.rgb(245, 124, 0)
                else -> Color.rgb(67, 160, 71)
            }
            canvas.drawCircle(45f, y - 4f, 4f, pRisk)
            val line = "${idx + 1}. ${c.cliente.nombre.take(20)} | ${c.cliente.categoria ?: "A"} | " +
                "${c.scoreRiesgo} | ${(c.porcentajeLimite * 100).toInt()}% | " +
                "${CurrencyFormatter.formatearCompacto(c.facturacionAnio)} | ${c.diasAVencimiento ?: -1}d"
            canvas.drawText(line, 56f, y, pRow)
        }
        pdf.finishPage(page)

        val dir = File(context.cacheDir, "panel_exports").apply { mkdirs() }
        val file = File(dir, "ranking_clientes_${DateUtils.formatearNombreArchivo(Date())}.pdf")
        file.outputStream().use { pdf.writeTo(it) }
        pdf.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, titulo)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
