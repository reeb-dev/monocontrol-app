package com.manuelreeb.monocontrol.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.manuelreeb.monocontrol.domain.model.CategoriaMonotributo
import com.manuelreeb.monocontrol.domain.model.Movimiento
import com.manuelreeb.monocontrol.domain.model.TipoMovimiento
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.util.Date

object ExcelExporter {

    /**
     * Exporta el historial completo de movimientos más un resumen por categoría.
     * Retorna un Intent para compartir el archivo generado.
     */
    fun exportarReporteCompleto(
        context: Context,
        movimientos: List<Movimiento>,
        categoriaActual: CategoriaMonotributo,
        facturacionAnual: Double
    ): Intent {
        val workbook = XSSFWorkbook()

        agregarHojaHistorial(workbook, movimientos)
        agregarHojaResumenMensual(workbook, movimientos)
        agregarHojaCategoria(workbook, categoriaActual, facturacionAnual)
        agregarHojaPlantillaImportacion(workbook)

        return guardarYCompartir(context, workbook, "MonoControl_Reporte_${DateUtils.formatearNombreArchivo(Date())}.xlsx")
    }

    // ──────────────────────────────────────────────────────────────
    // HOJA 1: Historial completo
    // ──────────────────────────────────────────────────────────────
    private fun agregarHojaHistorial(wb: XSSFWorkbook, movimientos: List<Movimiento>) {
        val sheet = wb.createSheet("Historial")
        val headerStyle = headerStyle(wb)
        val moneyStyle = moneyStyle(wb)
        val ingresoStyle = colorStyle(wb, IndexedColors.LIGHT_GREEN)
        val gastoStyle = colorStyle(wb, IndexedColors.ROSE)

        // Encabezados
        val header = sheet.createRow(0)
        listOf("Fecha", "Tipo", "Monto", "Descripción").forEachIndexed { i, title ->
            header.createCell(i).apply { setCellValue(title); cellStyle =
                headerStyle as XSSFCellStyle?
            }
        }

        // Datos
        movimientos.sortedByDescending { it.fecha }.forEachIndexed { i, mov ->
            val row = sheet.createRow(i + 1)
            row.createCell(0).setCellValue(DateUtils.formatear(mov.fecha))
            row.createCell(1).setCellValue(if (mov.tipo == TipoMovimiento.INGRESO) "INGRESO" else "GASTO")
            row.createCell(2).apply {
                setCellValue(mov.monto)
                cellStyle = (if (mov.tipo == TipoMovimiento.INGRESO) ingresoStyle else gastoStyle) as XSSFCellStyle?
            }
            row.createCell(3).setCellValue(mov.descripcion)
        }

        // Totales
        val totalRow = sheet.createRow(movimientos.size + 2)
        totalRow.createCell(1).apply { setCellValue("TOTAL INGRESOS"); cellStyle =
            headerStyle as XSSFCellStyle?
        }
        totalRow.createCell(2).apply {
            setCellValue(movimientos.filter { it.tipo == TipoMovimiento.INGRESO }.sumOf { it.monto })
            cellStyle = moneyStyle as XSSFCellStyle?
        }
        val gastoRow = sheet.createRow(movimientos.size + 3)
        gastoRow.createCell(1).apply { setCellValue("TOTAL GASTOS"); cellStyle =
            headerStyle as XSSFCellStyle?
        }
        gastoRow.createCell(2).apply {
            setCellValue(movimientos.filter { it.tipo == TipoMovimiento.GASTO }.sumOf { it.monto })
            cellStyle = moneyStyle as XSSFCellStyle?
        }

        (0..3).forEach { sheet.autoSizeColumn(it) }
    }

    // ──────────────────────────────────────────────────────────────
    // HOJA 2: Resumen mensual
    // ──────────────────────────────────────────────────────────────
    private fun agregarHojaResumenMensual(wb: XSSFWorkbook, movimientos: List<Movimiento>) {
        val sheet = wb.createSheet("Resumen Mensual")
        val headerStyle = headerStyle(wb)
        val moneyStyle = moneyStyle(wb)

        val header = sheet.createRow(0)
        listOf("Mes", "Año", "Ingresos", "Gastos", "Balance").forEachIndexed { i, t ->
            header.createCell(i).apply { setCellValue(t); cellStyle =
                headerStyle as XSSFCellStyle?
            }
        }

        // Agrupar por mes/año
        val grupos = movimientos.groupBy {
            val cal = java.util.Calendar.getInstance()
            cal.time = it.fecha
            Pair(cal.get(java.util.Calendar.MONTH) + 1, cal.get(java.util.Calendar.YEAR))
        }

        grupos.entries.sortedWith(compareByDescending<Map.Entry<Pair<Int, Int>, List<Movimiento>>> { it.key.second }
            .thenByDescending { it.key.first })
            .forEachIndexed { i, (mesAnio, movs) ->
                val row = sheet.createRow(i + 1)
                row.createCell(0).setCellValue(DateUtils.nombreMes(mesAnio.first))
                row.createCell(1).setCellValue(mesAnio.second.toDouble())
                val ingresos = movs.filter { it.tipo == TipoMovimiento.INGRESO }.sumOf { it.monto }
                val gastos = movs.filter { it.tipo == TipoMovimiento.GASTO }.sumOf { it.monto }
                row.createCell(2).apply { setCellValue(ingresos); cellStyle =
                    moneyStyle as XSSFCellStyle?
                }
                row.createCell(3).apply { setCellValue(gastos); cellStyle =
                    moneyStyle as XSSFCellStyle?
                }
                row.createCell(4).apply { setCellValue(ingresos - gastos); cellStyle =
                    moneyStyle as XSSFCellStyle?
                }
            }

        (0..4).forEach { sheet.autoSizeColumn(it) }
    }

    // ──────────────────────────────────────────────────────────────
    // HOJA 3: Categoría actual y tabla de límites
    // ──────────────────────────────────────────────────────────────
    private fun agregarHojaCategoria(
        wb: XSSFWorkbook,
        categoriaActual: CategoriaMonotributo,
        facturacionAnual: Double
    ) {
        val sheet = wb.createSheet("Categoría Monotributo")
        val headerStyle = headerStyle(wb)
        val highlightStyle = colorStyle(wb, IndexedColors.LIGHT_YELLOW)
        val moneyStyle = moneyStyle(wb)

        // Título
        sheet.createRow(0).createCell(0).apply {
            setCellValue("REPORTE MONOTRIBUTO — ${DateUtils.anioActual()}")
            cellStyle = headerStyle as XSSFCellStyle?
        }
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 3))

        sheet.createRow(2).apply {
            createCell(0).apply { setCellValue("Categoría actual"); cellStyle =
                headerStyle as XSSFCellStyle?
            }
            createCell(1).setCellValue("Monotributo ${categoriaActual.letra}")
        }
        sheet.createRow(3).apply {
            createCell(0).apply { setCellValue("Facturación anual"); cellStyle =
                headerStyle as XSSFCellStyle?
            }
            createCell(1).apply { setCellValue(facturacionAnual); cellStyle =
                moneyStyle as XSSFCellStyle?
            }
        }
        sheet.createRow(4).apply {
            createCell(0).apply { setCellValue("Límite categoría"); cellStyle =
                headerStyle as XSSFCellStyle?
            }
            createCell(1).apply { setCellValue(categoriaActual.limiteAnual); cellStyle =
                moneyStyle as XSSFCellStyle?
            }
        }
        sheet.createRow(5).apply {
            createCell(0).apply { setCellValue("Margen restante"); cellStyle =
                headerStyle as XSSFCellStyle?
            }
            createCell(1).apply {
                setCellValue(categoriaActual.limiteAnual - facturacionAnual)
                cellStyle = moneyStyle as XSSFCellStyle?
            }
        }
        sheet.createRow(6).apply {
            createCell(0).apply { setCellValue("% utilizado"); cellStyle =
                headerStyle as XSSFCellStyle?
            }
            createCell(1).setCellValue("${"%.1f".format(facturacionAnual / categoriaActual.limiteAnual * 100)}%")
        }

        // Tabla completa de categorías
        val tablaHeader = sheet.createRow(8)
        listOf("Categoría", "Límite Anual", "Cuota Mensual").forEachIndexed { i, t ->
            tablaHeader.createCell(i).apply { setCellValue(t); cellStyle =
                headerStyle as XSSFCellStyle?
            }
        }
        CategoriaMonotributo.entries.forEachIndexed { i, cat ->
            val row = sheet.createRow(9 + i)
            row.createCell(0).apply {
                setCellValue("Monotributo ${cat.letra}")
                if (cat == categoriaActual) cellStyle = highlightStyle as XSSFCellStyle?
            }
            row.createCell(1).apply { setCellValue(cat.limiteAnual); cellStyle =
                moneyStyle as XSSFCellStyle?
            }
            row.createCell(2).apply { setCellValue(cat.cuotaMensual); cellStyle =
                moneyStyle as XSSFCellStyle?
            }
        }

        (0..3).forEach { sheet.autoSizeColumn(it) }
    }

    // ──────────────────────────────────────────────────────────────
    // HOJA 4: Plantilla para importar
    // ──────────────────────────────────────────────────────────────
    private fun agregarHojaPlantillaImportacion(wb: XSSFWorkbook) {
        val sheet = wb.createSheet("Plantilla Importar")
        val headerStyle = headerStyle(wb)
        val header = sheet.createRow(0)
        listOf("Monto", "Tipo (INGRESO/GASTO)", "Fecha (dd/MM/yyyy)", "Descripción")
            .forEachIndexed { i, t ->
                header.createCell(i).apply { setCellValue(t); cellStyle =
                    headerStyle as XSSFCellStyle?
                }
            }
        // Fila de ejemplo
        val ejemplo = sheet.createRow(1)
        ejemplo.createCell(0).setCellValue(50000.0)
        ejemplo.createCell(1).setCellValue("INGRESO")
        ejemplo.createCell(2).setCellValue("15/04/2026")
        ejemplo.createCell(3).setCellValue("Factura cliente ABC")
        (0..3).forEach { sheet.autoSizeColumn(it) }
    }

    // ──────────────────────────────────────────────────────────────
    // Guardar y compartir
    // ──────────────────────────────────────────────────────────────
    private fun guardarYCompartir(context: Context, wb: XSSFWorkbook, nombre: String): Intent {
        val dir = File(context.cacheDir, "excel_exports").apply { mkdirs() }
        val file = File(dir, nombre)
        file.outputStream().use { wb.write(it) }
        wb.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Reporte MonoControl")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Estilos
    // ──────────────────────────────────────────────────────────────
    private fun headerStyle(wb: XSSFWorkbook): CellStyle = wb.createCellStyle().apply {
        fillForegroundColor = IndexedColors.DARK_BLUE.index
        fillPattern = FillPatternType.SOLID_FOREGROUND
        val font = wb.createFont().apply {
            bold = true
            color = IndexedColors.WHITE.index
        }
        setFont(font)
    }

    private fun moneyStyle(wb: XSSFWorkbook): CellStyle = wb.createCellStyle().apply {
        val format = wb.createDataFormat()
        dataFormat = format.getFormat("#,##0.00")
    }

    private fun colorStyle(wb: XSSFWorkbook, color: IndexedColors): CellStyle = wb.createCellStyle().apply {
        fillForegroundColor = color.index
        fillPattern = FillPatternType.SOLID_FOREGROUND
        val format = wb.createDataFormat()
        dataFormat = format.getFormat("#,##0.00")
    }
}

