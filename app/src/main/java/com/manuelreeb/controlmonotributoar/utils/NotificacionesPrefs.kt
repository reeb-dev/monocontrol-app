package com.reeb.controlmonotributoar.utils

import android.content.Context

data class NotificacionesConfig(
    val pushActivo: Boolean = true,
    val emailActivo: Boolean = true,
    val smsActivo: Boolean = false
)

object NotificacionesPrefs {
    private const val PREFS = "notificaciones_config_prefs"
    private const val KEY_PUSH = "push_activo_v1"
    private const val KEY_EMAIL = "email_activo_v1"
    private const val KEY_SMS = "sms_activo_v1"

    fun load(context: Context): NotificacionesConfig {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return NotificacionesConfig(
            pushActivo = prefs.getBoolean(KEY_PUSH, true),
            emailActivo = prefs.getBoolean(KEY_EMAIL, true),
            smsActivo = prefs.getBoolean(KEY_SMS, false)
        )
    }

    fun save(context: Context, config: NotificacionesConfig) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_PUSH, config.pushActivo)
            .putBoolean(KEY_EMAIL, config.emailActivo)
            .putBoolean(KEY_SMS, config.smsActivo)
            .apply()
    }
}
