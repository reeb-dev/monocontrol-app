package com.reeb.controlmonotributoar.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.reeb.controlmonotributoar.data.local.AppDatabase
import com.reeb.controlmonotributoar.data.notifications.RecordatorioNotificacionHelper
import com.reeb.controlmonotributoar.data.repository.MovimientoRepository
import com.reeb.controlmonotributoar.utils.CategoriaCalculator
import com.reeb.controlmonotributoar.utils.CategoriaOverride
import com.reeb.controlmonotributoar.utils.DateUtils
import com.reeb.controlmonotributoar.utils.HonorariosHelper
import kotlinx.coroutines.flow.first
import java.text.NumberFormat
import java.util.Locale

/**
 * Worker que se ejecuta periódicamente. Para cada cliente con
 * `envioAutomatico = true`, si hoy coincide con el día configurado y la
 * hora ya pasó, dispara una notificación al contador para que envíe.
 *
 * No envía emails sin intervención humana (no hay servidor SMTP propio):
 * el contador toca la notificación y se abre la pantalla de envío con todo
 * pre-cargado (asunto, mensaje, adjunto). Esto cumple Play Store policy y
 * privacidad (GDPR).
 */
class RecordatorioEnvioWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = runCatching {
        val context = applicationContext
        val db = AppDatabase.getInstance(context)
        val clienteDao = db.clienteDao()
        val movimientoDao = db.movimientoDao()

        val clientes = clienteDao.observarTodos().first()
        if (clientes.isEmpty()) return@runCatching Result.success()

        val cal = java.util.Calendar.getInstance()
        val diaHoy = cal.get(java.util.Calendar.DAY_OF_MONTH)
        val horaAhora = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val minAhora = cal.get(java.util.Calendar.MINUTE)
        val ahoraMin = horaAhora * 60 + minAhora

        // Total facturado del año, cacheado para no consultar por cliente.
        val movRepo = MovimientoRepository(movimientoDao)
        val totalAnual = movRepo.obtenerTotalIngresosAnuales(DateUtils.anioActual()).first()
        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "AR"))

        clientes
            .filter { it.envioAutomatico }
            .filter { it.envioDiaMes == diaHoy }
            .filter {
                val configMin = it.envioHora * 60 + it.envioMinuto
                // Disparamos si la hora configurada ya pasó (hasta 1 hora de margen).
                ahoraMin in configMin..(configMin + 60)
            }
            .forEach { cliente ->
                val override = CategoriaOverride.fromLetra(cliente.categoria)
                val categoria = override ?: CategoriaCalculator.calcularCategoria(totalAnual)
                val cuota = categoria.cuotaSegunRubro(cliente.esVentaMuebles)
                RecordatorioNotificacionHelper.mostrar(
                    context = context,
                    clienteId = cliente.id,
                    nombreCliente = cliente.nombre,
                    cuotaFormateada = formatter.format(cuota)
                )
            }

        val periodo = HonorariosHelper.periodoActual()
        clientes
            .filter { it.honorarioActivo && it.honorarioMonto > 0.0 }
            .filter { HonorariosHelper.esMesCobro(it) }
            .filter { !HonorariosHelper.estaPagadoPeriodo(it, periodo) }
            .forEach { cliente ->
                val configMin = cliente.envioHora * 60 + cliente.envioMinuto
                if (ahoraMin !in configMin..(configMin + 60)) return@forEach
                val dias = HonorariosHelper.diasHastaVencimiento(cliente)
                if (dias in setOf(7, 3, 1, 0) || dias < 0) {
                    val estado = when {
                        dias < 0 -> "vencido hace ${-dias} días"
                        dias == 0 -> "vence hoy"
                        else -> "vence en $dias días"
                    }
                    val mensaje = buildString {
                        append("Honorario ")
                        append(formatter.format(cliente.honorarioMonto))
                        append(" • ")
                        append(estado)
                        if (cliente.whatsappEnviar && cliente.whatsappNumero.isNotBlank()) {
                            append(" • WhatsApp automático listo")
                        }
                    }
                    RecordatorioNotificacionHelper.mostrarHonorario(
                        context = context,
                        clienteId = cliente.id,
                        nombreCliente = cliente.nombre,
                        mensaje = mensaje
                    )
                }
            }

        Result.success()
    }.getOrElse { Result.retry() }
}

