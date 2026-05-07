package com.reeb.controlmonotributoar.utils

import android.content.Context

object ScreenTourPrefs {
    private const val PREFS = "screen_tour_prefs"

    private const val KEY_HOME = "tour_home_v1"
    private const val KEY_CLIENTES = "tour_clientes_v1"
    private const val KEY_PANEL = "tour_panel_v1"

    fun wasSeenHome(context: Context): Boolean = wasSeen(context, KEY_HOME)
    fun wasSeenClientes(context: Context): Boolean = wasSeen(context, KEY_CLIENTES)
    fun wasSeenPanel(context: Context): Boolean = wasSeen(context, KEY_PANEL)

    fun markSeenHome(context: Context) = markSeen(context, KEY_HOME)
    fun markSeenClientes(context: Context) = markSeen(context, KEY_CLIENTES)
    fun markSeenPanel(context: Context) = markSeen(context, KEY_PANEL)

    private fun wasSeen(context: Context, key: String): Boolean {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(key, false)
    }

    private fun markSeen(context: Context, key: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(key, true)
            .apply()
    }
}
