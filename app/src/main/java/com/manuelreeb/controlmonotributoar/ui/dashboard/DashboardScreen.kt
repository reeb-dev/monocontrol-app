package com.reeb.controlmonotributoar.ui.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reeb.controlmonotributoar.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Calendar

// ────────────────────────────────────────────────────────────────────────────
//  DASHBOARD MEJORADO
// ────────────────────────────────────────────────────────────────────────────
@Composable
fun DashboardScreen(
    nombreUsuario: String = "",
    onNuevoIngreso: () -> Unit = {},
    onVerReporte: () -> Unit = {},
    onVerClientes: () -> Unit = {},
    onVerAlertas: () -> Unit = {},
    onVerCategoria: () -> Unit = {}
) {
    // Saludo según hora del día
    val hora = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val (saludo, emojiSaludo) = when (hora) {
        in 5..11  -> "Buen día"      to "☀️"
        in 12..18 -> "Buenas tardes" to "🌤️"
        else      -> "Buenas noches" to "🌙"
    }

    // Consejo rotatorio cada 6s
    val consejos = remember {
        listOf(
            "💡 Registrá tus ingresos cada semana para evitar sorpresas a fin de mes." to "Tip semanal",
            "📊 Usá la simulación para anticipar si vas a recategorizar." to "Planificá",
            "📁 Exportá tus movimientos a Excel para tu contador en 1 click." to "Productividad",
            "🔔 Activá las notificaciones para no perder ningún vencimiento." to "Importante",
            "👥 Si sos contador, gestioná todos tus clientes desde el panel." to "Contadores"
        )
    }
    var indiceConsejo by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(6_000)
            indiceConsejo = (indiceConsejo + 1) % consejos.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlancoSuave)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HeaderSaludo(saludo, emojiSaludo, nombreUsuario)

        // Estado fiscal con barra de progreso
        EstadoFiscalCard(
            porcentajeUso = 0.62f,
            categoria = "Categoría B",
            facturado = "$3.420.000",
            limite = "$5.500.000",
            onClick = onVerCategoria
        )

        // KPIs en una fila scrollable visual
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            KPIMini("Estado", "Óptimo", "✅", VerdeExito, Modifier.weight(1f))
            KPIMini("Días", "45", "⏰", SolAmarillo, Modifier.weight(1f))
            KPIMini("Alertas", "2", "🔔", RojoMedio, Modifier.weight(1f))
        }

        // Próximo vencimiento destacado
        ProximoVencimientoCard()

        // Acciones rápidas en grid 2x2
        Text(
            "Acciones rápidas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextoOscuro,
            modifier = Modifier.padding(top = 4.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AccionRapida("Nuevo ingreso", Icons.Default.Add, CelesteOscuro, Modifier.weight(1f), onNuevoIngreso)
                AccionRapida("Ver reporte", Icons.Default.Assessment, VerdeExito, Modifier.weight(1f), onVerReporte)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AccionRapida("Mis clientes", Icons.Default.Group, AzulInfo, Modifier.weight(1f), onVerClientes)
                AccionRapida("Alertas", Icons.Default.Notifications, SolAmarillo, Modifier.weight(1f), onVerAlertas)
            }
        }

        // Mini gráfico tendencia 6 meses
        TendenciaMiniChart()

        // Últimos movimientos
        UltimosMovimientosCard()

        // Tip rotativo con animación
        AnimatedContent(
            targetState = indiceConsejo,
            transitionSpec = {
                (fadeIn(tween(500)) togetherWith fadeOut(tween(300)))
            },
            label = "consejo"
        ) { idx ->
            ConsejoCard(consejos[idx].first, consejos[idx].second)
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ────────────────────────────────────────────────────────────────────────────
//  HEADER CON GRADIENTE
// ────────────────────────────────────────────────────────────────────────────
@Composable
private fun HeaderSaludo(saludo: String, emoji: String, nombre: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(CelesteArgentino, CelesteOscuro)
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "$emoji $saludo${if (nombre.isNotBlank()) ", $nombre" else ""}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = BlancoArgentino
                )
                Text(
                    "Acá tenés el resumen de tu situación fiscal",
                    style = MaterialTheme.typography.bodySmall,
                    color = BlancoArgentino.copy(alpha = 0.85f)
                )
            }
            // Decoración: círculo translúcido
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(80.dp)
                    .offset(x = 30.dp, y = (-20).dp)
                    .clip(CircleShape)
                    .background(BlancoArgentino.copy(alpha = 0.12f))
            )
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
//  ESTADO FISCAL CON PROGRESO
// ────────────────────────────────────────────────────────────────────────────
@Composable
private fun EstadoFiscalCard(
    porcentajeUso: Float,
    categoria: String,
    facturado: String,
    limite: String,
    onClick: () -> Unit
) {
    val color = when {
        porcentajeUso >= 0.9f -> RojoMedio
        porcentajeUso >= 0.75f -> SolAmarillo
        else -> VerdeExito
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, CelesteBorde),
        onClick = onClick
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Insights, null, tint = CelesteOscuro, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Tu situación hoy", fontWeight = FontWeight.Bold, color = TextoOscuro,
                    modifier = Modifier.weight(1f))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(color.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        categoria,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }

            // Barra de progreso animada
            LinearProgressIndicator(
                progress = { porcentajeUso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = color,
                trackColor = GrisClaro
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Facturado", style = MaterialTheme.typography.labelSmall, color = GrisMedio)
                    Text(facturado, fontWeight = FontWeight.Bold, color = TextoOscuro)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Límite categoría", style = MaterialTheme.typography.labelSmall, color = GrisMedio)
                    Text(limite, fontWeight = FontWeight.Bold, color = color)
                }
            }
            Text(
                "Usado: ${(porcentajeUso * 100).toInt()}% del límite anual",
                style = MaterialTheme.typography.labelSmall,
                color = GrisMedio
            )
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
//  KPI MINI
// ────────────────────────────────────────────────────────────────────────────
@Composable
private fun KPIMini(label: String, valor: String, emoji: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(emoji, fontSize = 22.sp)
            Text(label, style = MaterialTheme.typography.labelSmall, color = GrisMedio, fontSize = 10.sp)
            Text(valor, fontWeight = FontWeight.ExtraBold, color = color, fontSize = 14.sp)
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
//  PRÓXIMO VENCIMIENTO
// ────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProximoVencimientoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SolAmarillo.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, SolAmarillo.copy(alpha = 0.4f))
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SolAmarillo.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Schedule, null, tint = SolMarron, modifier = Modifier.size(24.dp))
            }
            Column(Modifier.weight(1f)) {
                Text("Próximo vencimiento", style = MaterialTheme.typography.labelSmall, color = GrisMedio)
                Text("Cuota mensual", fontWeight = FontWeight.Bold, color = TextoOscuro)
                Text("20 de mayo · faltan 5 días", style = MaterialTheme.typography.bodySmall, color = SolMarron)
            }
            Text("$58.350", fontWeight = FontWeight.ExtraBold, color = SolMarron, fontSize = 16.sp)
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
//  ACCIÓN RÁPIDA
// ────────────────────────────────────────────────────────────────────────────
@Composable
private fun AccionRapida(
    label: String,
    icono: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        onClick = onClick
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Text(
                label,
                fontWeight = FontWeight.SemiBold,
                color = TextoOscuro,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
//  TENDENCIA - GRÁFICO MINI DE BARRAS
// ────────────────────────────────────────────────────────────────────────────
@Composable
private fun TendenciaMiniChart() {
    val datos = remember {
        listOf(
            "Dic" to 0.45f, "Ene" to 0.55f, "Feb" to 0.40f,
            "Mar" to 0.70f, "Abr" to 0.62f, "May" to 0.85f
        )
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, CelesteBorde)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = VerdeExito, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Tendencia (6 meses)", fontWeight = FontWeight.Bold, color = TextoOscuro,
                    modifier = Modifier.weight(1f))
                Text("+18%", fontWeight = FontWeight.Bold, color = VerdeExito, fontSize = 13.sp)
            }
            Row(
                Modifier.fillMaxWidth().height(80.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                datos.forEach { (mes, alturaRel) ->
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height((alturaRel * 60).dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(CelesteArgentino, CelesteOscuro)
                                    )
                                )
                        )
                        Text(mes, fontSize = 10.sp, color = GrisMedio)
                    }
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
//  ÚLTIMOS MOVIMIENTOS
// ────────────────────────────────────────────────────────────────────────────
@Composable
private fun UltimosMovimientosCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, CelesteBorde)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null, tint = CelesteOscuro, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Últimos movimientos", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = TextoOscuro, modifier = Modifier.weight(1f))
                TextButton(onClick = {}) {
                    Text("Ver todos", color = CelesteOscuro, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            repeat(3) { index ->
                MovimientoItem(
                    emoji = if (index % 2 == 0) "💰" else "💸",
                    desc  = if (index % 2 == 0) "Factura #${1001 + index}" else "Gastos operativos",
                    fecha = "Hace ${(index + 1) * 2} días",
                    monto = if (index % 2 == 0) "+\$50.000" else "−\$8.500",
                    color = if (index % 2 == 0) VerdeExito else RojoMedio
                )
                if (index < 2) HorizontalDivider(color = GrisClaro)
            }
        }
    }
}

@Composable
private fun MovimientoItem(emoji: String, desc: String, fecha: String, monto: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 18.sp)
            }
            Column {
                Text(desc, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoOscuro)
                Text(fecha, style = MaterialTheme.typography.labelSmall, color = GrisMedio)
            }
        }
        Text(monto, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = color)
    }
}

// ────────────────────────────────────────────────────────────────────────────
//  CONSEJO ROTATIVO
// ────────────────────────────────────────────────────────────────────────────
@Composable
private fun ConsejoCard(texto: String, etiqueta: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SolSuave),
        border = BorderStroke(1.dp, SolAmarillo.copy(alpha = 0.35f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SolAmarillo.copy(alpha = 0.25f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(etiqueta, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SolMarron)
            }
            Text(texto, style = MaterialTheme.typography.bodyMedium, color = TextoOscuro)
        }
    }
}
