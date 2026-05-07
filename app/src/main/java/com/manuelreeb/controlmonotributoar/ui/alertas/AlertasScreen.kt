package com.reeb.controlmonotributoar.ui.alertas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reeb.controlmonotributoar.domain.model.Alerta

private val Celeste     = Color(0xFF75AADB)
private val Amarillo    = Color(0xFFFBB81C)
private val Blanco      = Color(0xFFFFFFFF)
private val BlancoSuave = Color(0xFFF0F6FF)
private val GrisCeleste = Color(0xFFCCDFF4)
private val TextoOscuro = Color(0xFF0D2A4A)
private val Verde       = Color(0xFF2E7D32)
private val Rojo        = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertasScreen(viewModel: AlertasViewModel, onBack: () -> Unit) {
    val alertas by viewModel.alertas.collectAsStateWithLifecycle()
    val pendientes = alertas.count { !it.leida }

    Scaffold(containerColor = BlancoSuave) { padding ->
        if (alertas.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("✅", fontSize = 64.sp)
                    Text("¡Todo en orden!", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, color = Verde)
                    Text("Tu facturación está bien encaminada.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoOscuro.copy(alpha = 0.6f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Verde.copy(alpha = 0.08f))
                            .padding(12.dp)
                    ) {
                        Text("Sin alertas activas 🟢",
                            color = Verde, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier       = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Resumen
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (pendientes > 0) Rojo.copy(alpha = 0.08f) else Verde.copy(alpha = 0.08f))
                            .padding(14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                            modifier              = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    if (pendientes > 0) "⚠️  $pendientes alerta${if (pendientes > 1) "s" else ""} pendiente${if (pendientes > 1) "s" else ""}"
                                    else "✅  Todas las alertas revisadas",
                                    fontWeight = FontWeight.Bold,
                                    color      = if (pendientes > 0) Rojo else Verde
                                )
                                Text("Total: ${alertas.size} alertas",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextoOscuro.copy(alpha = 0.55f))
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (pendientes > 0) Rojo else Verde)
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$pendientes",
                                    color = Blanco, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            }
                        }
                    }
                }

                items(alertas, key = { it.id }) { alerta ->
                    AlertaItemMejorado(alerta, onMarcarLeida = { viewModel.marcarLeida(alerta.id) })
                }
            }
        }
    }
}

@Composable
private fun AlertaItemMejorado(alerta: Alerta, onMarcarLeida: () -> Unit) {
    val (colorBorde, colorFondo, emoji) = when {
        alerta.leida             -> Triple(GrisCeleste, Color(0xFFF8F8F8), "✅")
        alerta.porcentaje >= 0.9f -> Triple(Rojo, Rojo.copy(alpha = 0.06f), "🔴")
        alerta.porcentaje >= 0.7f -> Triple(Amarillo, Amarillo.copy(alpha = 0.07f), "🟡")
        else                     -> Triple(Celeste, Celeste.copy(alpha = 0.06f), "🔵")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = colorFondo),
        border   = androidx.compose.foundation.BorderStroke(
            if (alerta.leida) 1.dp else 2.dp, colorBorde
        )
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 26.sp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    alerta.mensaje,
                    fontWeight     = if (alerta.leida) FontWeight.Normal else FontWeight.SemiBold,
                    color          = if (alerta.leida) TextoOscuro.copy(alpha = 0.5f) else TextoOscuro,
                    textDecoration = if (alerta.leida) TextDecoration.LineThrough else null
                )
                // Barra de progreso mini
                if (!alerta.leida) {
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress   = { alerta.porcentaje },
                        modifier   = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color      = colorBorde,
                        trackColor = GrisCeleste
                    )
                    Text(
                        "${(alerta.porcentaje * 100).toInt()}% del límite",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorBorde
                    )
                }
            }
            if (!alerta.leida) {
                IconButton(onClick = onMarcarLeida, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Marcar leída",
                        tint = Verde, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}
