package com.manuelreeb.monocontrol.data.remote

import android.content.Context

/**
 * Singleton perezoso para acceder al [RemoteConfigRepository] sin necesidad
 * de un contenedor de DI. Útil para Composables y ViewModels que no reciben
 * la instancia por parámetro.
 */
object RemoteConfigHolder {
    @Volatile private var instance: RemoteConfigRepository? = null

    fun get(context: Context): RemoteConfigRepository =
        instance ?: synchronized(this) {
            instance ?: RemoteConfigRepository(context.applicationContext).also { instance = it }
        }
}

