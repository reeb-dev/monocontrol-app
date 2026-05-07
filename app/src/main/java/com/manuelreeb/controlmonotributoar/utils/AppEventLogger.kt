package com.reeb.controlmonotributoar.utils

import android.util.Log

object AppEventLogger {
    private const val TAG = "MonoControlEvent"

    fun log(event: String, details: String = "") {
        Log.d(TAG, if (details.isBlank()) event else "$event | $details")
    }
}
