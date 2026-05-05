package com.manuelreeb.monocontrol.ui.alertas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manuelreeb.monocontrol.data.repository.AlertaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlertasViewModel(private val repository: AlertaRepository) : ViewModel() {

    val alertas = repository.obtenerTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun marcarLeida(id: Long) {
        viewModelScope.launch { repository.marcarLeida(id) }
    }
}

