package com.reeb.controlmonotributoar.utils

import android.content.Context
import com.reeb.controlmonotributoar.domain.model.UserRole

object HomeUxPrefs {
    private const val PREFS = "home_ux_prefs"
    private const val KEY_ONBOARDING_CONTADOR = "onboarding_contador_v1"
    private const val KEY_ONBOARDING_PERSONAL = "onboarding_personal_v1"

    fun wasOnboardingSeen(context: Context, role: UserRole): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getBoolean(key(role), false)
    }

    fun markOnboardingSeen(context: Context, role: UserRole) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(key(role), true).apply()
    }

    private fun key(role: UserRole): String {
        return if (role == UserRole.CONTADOR) KEY_ONBOARDING_CONTADOR else KEY_ONBOARDING_PERSONAL
    }
}
