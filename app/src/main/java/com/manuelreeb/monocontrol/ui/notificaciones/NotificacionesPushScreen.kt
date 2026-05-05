package com.manuelreeb.monocontrol.ui.notificaciones

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manuelreeb.monocontrol.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesPushScreen(onBack: () -> Unit) {
    var pushActivo by remember { mutableStateOf(true) }
    var emailActivo by remember { mutableStateOf(true) }
    var smsActivo by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BlancoSuave,
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blanco)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── CANALES ──────────────────────────────────────────────
            CardCanal("📱 Push (App)", pushActivo) { pushActivo = it }
            CardCanal("📧 Email", emailActivo) { emailActivo = it }
            CardCanal("💬 SMS", smsActivo) { smsActivo = it }

            // ── TIPOS DE NOTIFICACIÓN ────────────────────────────────
            Text("Tipos de notificación", style = MaterialTheme.typography.labelLarge, color = GrisMedio,
                modifier = Modifier.padding(top = 8.dp))

            NotificacionTipo(
                "⏰ Recordatorio de vencimiento",
                "5 días antes de que venza tu cuota",
                pushActivo
            )
            NotificacionTipo(
                "↕️ Cambio de categoría",
                "Cuando tu facturación te acerca al límite",
                pushActivo
            )
            NotificacionTipo(
                "📊 Actualización de tarifas",
                "Nuevas cuotas y cambios de AFIP",
                pushActivo
            )
            NotificacionTipo(
                "⚠️ Anomalías detectadas",
                "Cambios inusuales en tu actividad",
                pushActivo
            )

            // ── HORARIOS DE NOTIFICACIÓN ────────────────────────────
            HorariosCard()

            // ── CENTRO DE NOTIFICACIONES ────────────────────────────
            CentroNotificacionesCard()

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun CardCanal(titulo: String, activo: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, if (activo) CelesteOscuro else GrisCeleste)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.Bold, color = TextoOscuro)
                Text(
                    if (activo) "Habilitado" else "Deshabilitado",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (activo) VerdeExito else GrisMedio
                )
            }
            Switch(
                checked = activo,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = VerdeExito,
                    checkedTrackColor = VerdeExito.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun NotificacionTipo(titulo: String, descripcion: String, habilitado: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (habilitado) CelesteOscuro.copy(alpha = 0.08f) else GrisClaro)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = habilitado,
            onCheckedChange = { },
            colors = CheckboxDefaults.colors(checkedColor = CelesteOscuro),
            enabled = habilitado
        )
        Column(Modifier.weight(1f)) {
            Text(titulo, fontWeight = FontWeight.SemiBold, color = TextoOscuro, fontSize = 13.sp)
            Text(descripcion, style = MaterialTheme.typography.labelSmall, color = GrisMedio)
        }
        if (habilitado) {
            Icon(Icons.Default.Check, null, tint = VerdeExito, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun HorariosCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, CelesteBorde)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Schedule, null, tint = CelesteOscuro, modifier = Modifier.size(20.dp))
                Text("Horarios de notificación", fontWeight = FontWeight.Bold, color = TextoOscuro)
            }

            Text("Recibirás notificaciones entre estas horas:",
                style = MaterialTheme.typography.bodySmall, color = GrisMedio)

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = "09:00 AM",
                    onValueChange = { },
                    label = { Text("Desde") },
                    modifier = Modifier.weight(1f),
                    readOnly = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CelesteOscuro
                    ),
                    trailingIcon = { Icon(Icons.Default.Schedule, null, tint = CelesteOscuro) }
                )
                OutlinedTextField(
                    value = "09:00 PM",
                    onValueChange = { },
                    label = { Text("Hasta") },
                    modifier = Modifier.weight(1f),
                    readOnly = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CelesteOscuro
                    ),
                    trailingIcon = { Icon(Icons.Default.Schedule, null, tint = CelesteOscuro) }
                )
            }
        }
    }
}

@Composable
private fun CentroNotificacionesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, CelesteBorde)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.NotificationsNone, null, tint = CelesteOscuro, modifier = Modifier.size(20.dp))
                Text("Centro de notificaciones", fontWeight = FontWeight.Bold, color = TextoOscuro)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { i ->
                    NotificacionItem(
                        icono = when (i) {
                            0 -> "⏰"
                            1 -> "✅"
                            else -> "📊"
                        },
                        titulo = when (i) {
                            0 -> "Vencimiento en 5 días"
                            1 -> "Tu pedido fue procesado"
                            else -> "Nueva tarifa disponible"
                        },
                        fecha = when (i) {
                            0 -> "Hoy a las 14:32"
                            1 -> "Ayer a las 10:15"
                            else -> "Hace 2 días"
                        },
                        leida = i > 0
                    )
                }
            }

            TextButton(onClick = { }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Ver todas las notificaciones", color = CelesteOscuro, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun NotificacionItem(icono: String, titulo: String, fecha: String, leida: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (leida) GrisClaro else AzulInfo.copy(alpha = 0.1f))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icono, fontSize = 20.sp)
        Column(Modifier.weight(1f)) {
            Text(titulo, fontWeight = if (!leida) FontWeight.Bold else FontWeight.Normal,
                color = TextoOscuro, fontSize = 12.sp)
            Text(fecha, style = MaterialTheme.typography.labelSmall, color = GrisMedio)
        }
        if (!leida) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(AzulInfo)
            )
        }
    }
}

private val Blanco = BlancoArgentino

