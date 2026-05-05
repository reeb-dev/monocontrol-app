package com.manuelreeb.monocontrol.data.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.manuelreeb.monocontrol.data.local.AppDatabase
import com.manuelreeb.monocontrol.data.repository.ClienteRepository
import com.manuelreeb.monocontrol.data.sync.ClienteSyncService

/**
 * Worker que sincroniza la tabla `cliente` con Firestore.
 *
 * Se ejecuta:
 * - Al arranque de la app (encolado desde MainActivity).
 * - Cada vez que el dispositivo recupera red (constraint NetworkType.CONNECTED).
 * - Al hacer cambios offline (encolado desde ClientesViewModel/ConfigurarEnvio).
 *
 * Es seguro encolarlo varias veces — usa `ExistingWorkPolicy.REPLACE` para
 * cancelar trabajos previos y correr uno nuevo si hay cambios pendientes.
 */
class ClienteSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val repo = ClienteRepository(db.clienteDao())
        val service = ClienteSyncService(repo)

        return if (service.sync()) Result.success() else Result.retry()
    }

    companion object {
        private const val WORK_NAME = "cliente_sync"

        /** Encola un sync inmediato; si no hay red, WorkManager espera. */
        fun encolar(context: Context) {
            val request = OneTimeWorkRequestBuilder<ClienteSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}

