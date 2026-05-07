package com.reeb.controlmonotributoar.data.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.reeb.controlmonotributoar.MainActivity
import com.reeb.controlmonotributoar.R
import com.reeb.controlmonotributoar.data.remote.RemoteConfigHolder
import com.reeb.controlmonotributoar.data.remote.RemoteConfigRepository
import java.util.concurrent.TimeUnit

/**
 * Chequea periódicamente si hay nuevas versiones disponibles.
 * Se ejecuta cada 24 horas en background usando WorkManager.
 *
 * Cuando encuentra una versión más nueva, muestra una notificación al usuario.
 */
class UpdateCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        Log.d(TAG, "Iniciando chequeo de actualizaciones")

        val repo = RemoteConfigHolder.get(applicationContext)
        val configActual = repo.config.value
        val resultado = repo.refrescar()

        when (resultado) {
            is RemoteConfigRepository.RefreshResult.Updated -> {
                Log.d(TAG, "Nueva versión disponible: ${resultado.nuevaVersion}")
                mostrarNotificacion(
                    titulo = "Tarifas actualizadas",
                    mensaje = "Nueva versión ${resultado.nuevaVersion} disponible",
                    urgente = true
                )
                Result.success()
            }
            is RemoteConfigRepository.RefreshResult.NoChanges -> {
                Log.d(TAG, "No hay cambios")
                Result.success()
            }
            is RemoteConfigRepository.RefreshResult.Error -> {
                Log.w(TAG, "Error en chequeo: ${resultado.mensaje}")
                // No mostrar notificación de error, solo log
                Result.retry()
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error en UpdateCheckWorker: ${e.message}", e)
        Result.retry()
    }

    private fun mostrarNotificacion(
        titulo: String,
        mensaje: String,
        urgente: Boolean = false
    ) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal para Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = if (urgente) {
                NotificationManager.IMPORTANCE_HIGH
            } else {
                NotificationManager.IMPORTANCE_DEFAULT
            }
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Actualizaciones de Tarifas",
                importance
            ).apply {
                description = "Notificaciones sobre nuevas versiones de tarifas del monotributo"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent para abrir la app
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("scroll_to", "categorias") // Para scrollear a la sección de categorías
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val TAG = "UpdateCheckWorker"
        private const val CHANNEL_ID = "update_check_channel"
        private const val NOTIFICATION_ID = 9001
        private const val WORK_NAME = "check_updates_work"

        /**
         * Programa un chequeo periódico de actualizaciones.
         * Se ejecuta cada 24 horas con flexibilidad de 4 horas.
         */
        fun schedulePeriodicCheck(context: Context) {
            val updateCheckRequest = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
                24, TimeUnit.HOURS,
                4, TimeUnit.HOURS // Ventana de flexibilidad
            ).addTag(WORK_NAME).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                updateCheckRequest
            )

            Log.d(TAG, "Chequeo periódico programado (cada 24 horas)")
        }

        /**
         * Cancela los chequeos periódicos.
         */
        fun cancelPeriodicCheck(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Chequeo periódico cancelado")
        }

        /**
         * Fuerza un chequeo inmediato (para debug/testing).
         */
        fun forceCheckNow(context: Context) {
            val oneTimeRequest = androidx.work.OneTimeWorkRequestBuilder<UpdateCheckWorker>()
                .addTag(WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "${WORK_NAME}_now",
                androidx.work.ExistingWorkPolicy.REPLACE,
                oneTimeRequest
            )

            Log.d(TAG, "Chequeo inmediato solicitado")
        }
    }
}
