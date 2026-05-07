package com.reeb.controlmonotributoar.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reeb.controlmonotributoar.data.remote.RemoteConfigHolder
import com.reeb.controlmonotributoar.data.remote.RemoteConfigRepository
import com.reeb.controlmonotributoar.data.work.UpdateCheckWorker
import com.reeb.controlmonotributoar.utils.AppEventLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UpdateViewModel(private val context: Context) : ViewModel() {
    private val repo = RemoteConfigHolder.get(context.applicationContext)

    // Estados observables
    val config = repo.config
    val ultimaActualizacion = repo.ultimaActualizacion
    val versionDisponible = repo.versionDisponible

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _refreshMessage = MutableStateFlow<String?>(null)
    val refreshMessage: StateFlow<String?> = _refreshMessage.asStateFlow()

    fun forzarActualizacion() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _refreshMessage.value = null
            try {
                val resultado = repo.refrescar()
                _refreshMessage.value = when (resultado) {
                    is RemoteConfigRepository.RefreshResult.Updated -> {
                        AppEventLogger.log("update_config_updated", "version=${resultado.nuevaVersion}")
                        "✅ Tarifas actualizadas a v${resultado.nuevaVersion}"
                    }
                    is RemoteConfigRepository.RefreshResult.NoChanges -> {
                        AppEventLogger.log("update_config_no_changes")
                        "ℹ️ Ya tienes la versión más reciente"
                    }
                    is RemoteConfigRepository.RefreshResult.Error -> {
                        AppEventLogger.log("update_config_error", resultado.mensaje)
                        "❌ Error: ${resultado.mensaje}"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en forzarActualizacion", e)
                AppEventLogger.log("update_config_exception", e.message.orEmpty())
                _refreshMessage.value = "❌ Error: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun programarChequeoPeriodicoDeActualizaciones() {
        UpdateCheckWorker.schedulePeriodicCheck(context)
    }

    fun cancelarChequeoPeriodicoDeActualizaciones() {
        UpdateCheckWorker.cancelPeriodicCheck(context)
    }

    companion object {
        private const val TAG = "UpdateViewModel"
    }
}

