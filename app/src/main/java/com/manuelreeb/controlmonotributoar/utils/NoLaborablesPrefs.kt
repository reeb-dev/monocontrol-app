package com.reeb.controlmonotributoar.utils

import android.content.Context
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object NoLaborablesPrefs {
    private const val PREFS = "no_laborables_prefs"
    private const val KEY_DATES = "custom_no_laborables_dates_v1"
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun getAll(context: Context): Set<LocalDate> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_DATES, emptySet())
            .orEmpty()

        return raw.mapNotNull {
            runCatching { LocalDate.parse(it, formatter) }.getOrNull()
        }.toSet()
    }

    fun isCustomNoLaborable(context: Context, date: LocalDate): Boolean {
        return getAll(context).contains(date)
    }

    fun toggle(context: Context, date: LocalDate) {
        val set = getAll(context).toMutableSet()
        if (!set.add(date)) set.remove(date)
        save(context, set)
    }

    private fun save(context: Context, dates: Set<LocalDate>) {
        val raw = dates.map { it.format(formatter) }.toSet()
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_DATES, raw)
            .apply()
    }
}
