package com.reeb.controlmonotributoar.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.reeb.controlmonotributoar.MainActivity
import com.reeb.controlmonotributoar.R

/**
 * Helper para crear el canal de notificaciones y disparar la notificación
 * "es hora de enviar el recordatorio al cliente X".
 *
 * La notificación al hacer tap abre la app en la pantalla de configuración
 * del cliente para que el contador revise y envíe.
 */
object RecordatorioNotificacionHelper {

    private const val CHANNEL_ID = "recordatorio_envios"
    private const val CHANNEL_NAME = "Recordatorios de envío"
    private const val CHANNEL_DESC = "Avisos para enviar la factura/recordatorio del Monotributo a tus clientes."

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
                val canal = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = CHANNEL_DESC }
                mgr.createNotificationChannel(canal)
            }
        }
    }

    fun mostrar(
        context: Context,
        clienteId: Long,
        nombreCliente: String,
        cuotaFormateada: String
    ) {
        crearCanal(context)

        // Tap → abre la app y va a la pantalla de configuración del cliente.
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("cliente_id", clienteId)
            putExtra("abrir", "configurar_envio")
        }
        val pending = PendingIntent.getActivity(
            context,
            clienteId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("📧 Recordatorio para $nombreCliente")
            .setContentText("Es hora de enviar el aviso de pago ($cuotaFormateada)")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Tocá para abrir la pantalla de envío y mandar el email/WhatsApp con la factura adjunta."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.notify(clienteId.toInt(), notif)
    }

    fun mostrarHonorario(
        context: Context,
        clienteId: Long,
        nombreCliente: String,
        mensaje: String
    ) {
        crearCanal(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("cliente_id", clienteId)
            putExtra("abrir", "cliente_detalle")
        }
        val pending = PendingIntent.getActivity(
            context,
            (clienteId + 10_000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("💼 Cobranza de honorarios: $nombreCliente")
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.notify((clienteId + 10_000).toInt(), notif)
    }
}

