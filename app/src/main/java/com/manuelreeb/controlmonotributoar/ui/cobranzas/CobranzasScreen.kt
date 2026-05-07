package com.reeb.controlmonotributoar.ui.cobranzas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reeb.controlmonotributoar.data.local.entity.ClienteEntity
import com.reeb.controlmonotributoar.ui.clientes.ClientesViewModel
import com.reeb.controlmonotributoar.utils.CurrencyFormatter
import com.reeb.controlmonotributoar.utils.HonorariosHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val Blanco = Color(0xFFFFFFFF)
private val BlancoSuave = Color(0xFFF0F6FF)
private val CelesteOsc = Color(0xFF4A86C8)
private val Verde = Color(0xFF2E7D32)
private val Naranja = Color(0xFFE65100)
private val Rojo = Color(0xFFE53935)
private val Gris = Color(0xFF6B7B8C)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CobranzasScreen(
    viewModel: ClientesViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clientes by viewModel.clientes.collectAsStateWithLifecycle()
    val filtro by viewModel.filtro.collectAsStateWithLifecycle()
    val periodoActual = remember { HonorariosHelper.periodoActual() }

    val pendientes = remember(clientes) {
        clientes.filter {
            it.honorarioActivo &&
                it.honorarioMonto > 0 &&
                HonorariosHelper.esMesCobro(it) &&
                !HonorariosHelper.estaPagadoPeriodo(it, periodoActual)
        }
    }

    val hoy = remember(pendientes) { pendientes.filter { HonorariosHelper.diasHastaVencimiento(it) == 0 } }
    val sieteDias = remember(pendientes) { pendientes.filter { HonorariosHelper.diasHastaVencimiento(it) in 1..7 } }
    val vencidos = remember(pendientes) { pendientes.filter { HonorariosHelper.diasHastaVencimiento(it) < 0 } }

    val lista = when (filtro) {
        "cobranza_hoy" -> hoy
        "cobranza_7d" -> sieteDias
        "cobranza_vencidos" -> vencidos
        else -> pendientes
    }

    val totalPendiente = pendientes.sumOf { it.honorarioMonto }
    val totalHoy = hoy.sumOf { it.honorarioMonto }
    val totalVencido = vencidos.sumOf { it.honorarioMonto }

    Scaffold(
        containerColor = BlancoSuave,
        topBar = {
            TopAppBar(
                title = { Text("Cobranzas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blanco)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Blanco)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Resumen de honorarios", color = CelesteOsc, fontWeight = FontWeight.Bold)
                        Text("Pendiente total: ${CurrencyFormatter.formatear(totalPendiente)}", color = Gris)
                        Text("Para hoy: ${CurrencyFormatter.formatear(totalHoy)}", color = Naranja)
                        Text("Vencido: ${CurrencyFormatter.formatear(totalVencido)}", color = Rojo)
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FiltroChipCobranzas("Todos", filtro == "honorarios_pendientes") {
                        viewModel.onFiltroChange("honorarios_pendientes")
                    }
                    FiltroChipCobranzas("Hoy", filtro == "cobranza_hoy") {
                        viewModel.onFiltroChange("cobranza_hoy")
                    }
                    FiltroChipCobranzas("7 días", filtro == "cobranza_7d") {
                        viewModel.onFiltroChange("cobranza_7d")
                    }
                    FiltroChipCobranzas("Vencidos", filtro == "cobranza_vencidos") {
                        viewModel.onFiltroChange("cobranza_vencidos")
                    }
                }
            }

            if (lista.isEmpty()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Blanco)) {
                        Text(
                            "No hay cobranzas pendientes en este filtro.",
                            modifier = Modifier.padding(14.dp),
                            color = Gris
                        )
                    }
                }
            } else {
                items(lista, key = { it.id }) { cliente ->
                    CobranzaCard(
                        cliente = cliente,
                        onWhatsApp = {
                            CoroutineScope(Dispatchers.Main).launch {
                                val intent = viewModel.enviarRecordatorioWhatsApp(context, cliente)
                                intent?.let { context.startActivity(it) }
                            }
                        },
                        onMarcarCobrado = { viewModel.marcarHonorarioPagado(cliente) },
                        onCobradoYWhatsApp = {
                            viewModel.marcarHonorarioPagado(cliente)
                            CoroutineScope(Dispatchers.Main).launch {
                                val intent = viewModel.enviarRecordatorioWhatsApp(context, cliente)
                                intent?.let { context.startActivity(it) }
                            }
                        }
                    )
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun FiltroChipCobranzas(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CelesteOsc,
            selectedLabelColor = Blanco
        )
    )
}

@Composable
private fun CobranzaCard(
    cliente: ClienteEntity,
    onWhatsApp: () -> Unit,
    onMarcarCobrado: () -> Unit,
    onCobradoYWhatsApp: () -> Unit
) {
    val dias = HonorariosHelper.diasHastaVencimiento(cliente)
    val (estado, color) = when {
        dias < 0 -> "Vencido hace ${-dias} días" to Rojo
        dias == 0 -> "Vence hoy" to Naranja
        else -> "Vence en $dias días" to Verde
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Blanco),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(8.dp)
                        .background(color, RoundedCornerShape(99.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text(cliente.nombre, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(CurrencyFormatter.formatear(cliente.honorarioMonto), color = CelesteOsc, fontWeight = FontWeight.SemiBold)
            }
            Text(estado, color = color, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onMarcarCobrado,
                    colors = ButtonDefaults.buttonColors(containerColor = Verde),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Blanco)
                    Spacer(Modifier.width(4.dp))
                    Text("Marcar cobrado", color = Blanco)
                }
                OutlinedButton(
                    onClick = onWhatsApp,
                    modifier = Modifier.weight(1f),
                    enabled = cliente.whatsappNumero.isNotBlank()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = Color(0xFF25D366))
                    Spacer(Modifier.width(4.dp))
                    Text("WhatsApp")
                }
            }
            Button(
                onClick = onCobradoYWhatsApp,
                modifier = Modifier.fillMaxWidth(),
                enabled = cliente.whatsappNumero.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CelesteOsc)
            ) {
                Text("Marcar cobrado + Enviar WhatsApp", color = Blanco, fontWeight = FontWeight.SemiBold)
            }
            if (cliente.whatsappNumero.isBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Gris, modifier = Modifier.size(14.dp))
                    Text("Sin número de WhatsApp configurado", color = Gris, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
