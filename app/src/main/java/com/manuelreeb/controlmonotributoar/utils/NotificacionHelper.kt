package com.reeb.controlmonotributoar.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.reeb.controlmonotributoar.R
import com.reeb.controlmonotributoar.domain.model.CategoriaMonotributo

object NotificacionHelper {

    private const val CANAL_ID = "canal_monotributo"
    private const val CANAL_NOMBRE = "Alertas Monotributo"
    private const val NOTIF_ID_LIMITE = 1001

    fun crearCanal(context: Context) {
        val canal = NotificationChannel(
            CANAL_ID,
            CANAL_NOMBRE,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alertas sobre el límite de facturación"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(canal)
    }

    /**
     * Muestra una notificación que aclara explícitamente:
     *  - en qué categoría está hoy el usuario
     *  - a qué categoría podría pasar (recategorización)
     */
    @SuppressLint("MissingPermission")
    fun mostrarAlertaLimite(
        context: Context,
        porcentaje: Int,
        categoriaActual: CategoriaMonotributo? = null,
        proxima: CategoriaMonotributo? = null
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val catActualTxt = categoriaActual?.let { " (cat. ${it.letra})" } ?: ""
        val titulo = when {
            porcentaje >= 100 -> "🚨 ¡Superaste el límite anual$catActualTxt!"
            porcentaje >= 90  -> "🔴 Estás al $porcentaje% del límite$catActualTxt"
            else              -> "⚠️ Alcanzaste el $porcentaje% del límite$catActualTxt"
        }

        val mensaje = buildString {
            if (categoriaActual != null) {
                append("Categoría actual: ${categoriaActual.letra}.")
            } else {
                append("Revisá tu facturación.")
            }
            if (proxima != null) {
                append(" Si seguís facturando pasarías a la categoría ${proxima.letra}.")
            } else if (categoriaActual == CategoriaMonotributo.K) {
                append(" Estás en la categoría más alta (K): si la superás quedás fuera del régimen.")
            }
        }

        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIF_ID_LIMITE, notificacion)
    }
}
