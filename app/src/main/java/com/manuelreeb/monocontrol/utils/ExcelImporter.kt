package com.manuelreeb.monocontrol.utils

import android.content.Context
import android.net.Uri
import com.manuelreeb.monocontrol.domain.model.Movimiento
import com.manuelreeb.monocontrol.domain.model.TipoMovimiento
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.util.Date

data class ResultadoImportacion(
    val movimientos: List<Movimiento>,
    val errores: List<String>
)

object ExcelImporter {

    /**
     * Lee un archivo .xlsx o .xls desde una URI y retorna los movimientos encontrados.
     *
     * Formato esperado del Excel (fila 1 = encabezados):
     * | Monto | Tipo (INGRESO/GASTO) | Fecha (dd/MM/yyyy) | Descripción |
     */
    fun importarDesdeUri(context: Context, uri: Uri): ResultadoImportacion {
        val movimientos = mutableListOf<Movimiento>()
        val errores = mutableListOf<String>()

        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return ResultadoImportacion(emptyList(), listOf("No se pudo abrir el archivo."))

            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)

            // Saltar fila de encabezados (fila 0)
            val rows = sheet.drop(1)

            rows.forEachIndexed { index, row ->
                val fila = index + 2 // para mensajes de error (con encabezado en fila 1)
                try {
                    val monto = row.getCell(0)?.numericValue()
                        ?: run { errores.add("Fila $fila: monto inválido"); return@forEachIndexed }

                    val tipoStr = row.getCell(1)?.stringValue()?.uppercase()?.trim()
                        ?: "INGRESO"
                    val tipo = when (tipoStr) {
                        "INGRESO", "I", "ENTRADA" -> TipoMovimiento.INGRESO
                        "GASTO", "G", "EGRESO", "SALIDA" -> TipoMovimiento.GASTO
                        else -> TipoMovimiento.INGRESO
                    }

                    val fecha = row.getCell(2)?.dateValue() ?: Date()
                    val descripcion = row.getCell(3)?.stringValue() ?: ""

                    if (monto > 0) {
                        movimientos.add(
                            Movimiento(
                                monto = monto,
                                tipo = tipo,
                                fecha = fecha,
                                descripcion = descripcion
                            )
                        )
                    }
                } catch (e: Exception) {
                    errores.add("Fila $fila: ${e.message ?: "error desconocido"}")
                }
            }

            workbook.close()
            inputStream.close()

        } catch (e: Exception) {
            errores.add("Error al leer el archivo: ${e.message}")
        }

        return ResultadoImportacion(movimientos, errores)
    }

    private fun Cell.numericValue(): Double? = when (cellType) {
        CellType.NUMERIC -> numericCellValue
        CellType.STRING -> stringCellValue.replace(",", ".").toDoubleOrNull()
        else -> null
    }

    private fun Cell.stringValue(): String = when (cellType) {
        CellType.STRING -> stringCellValue
        CellType.NUMERIC -> numericCellValue.toLong().toString()
        CellType.BOOLEAN -> booleanCellValue.toString()
        else -> ""
    }

    private fun Cell.dateValue(): Date? = try {
        when (cellType) {
            CellType.NUMERIC -> org.apache.poi.ss.usermodel.DateUtil.getJavaDate(numericCellValue)
            CellType.STRING -> DateUtils.parseFecha(stringCellValue)
            else -> null
        }
    } catch (e: Exception) { null }
}

