package com.reeb.controlmonotributoar.data.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Programa el [RecordatorioEnvioWorker] para correr cada hora.
 *
 * El worker es muy liviano y sólo dispara la notificación cuando coincide
 * el día/hora configurado de algún cliente.
 *
 * Se llama una sola vez en `Application.onCreate()` con `KEEP` para que
 * sobreviva entre reinicios.
 */
object RecordatorioScheduler {

    private const val WORK_NAME = "recordatorio_envio_periodico"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<RecordatorioEnvioWorker>(
            1, TimeUnit.HOURS
        )
            .addTag(WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancelar(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}

