package com.manuelreeb.monocontrol.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import com.manuelreeb.monocontrol.ui.components.FabAction
import com.manuelreeb.monocontrol.ui.components.SpeedDialFab
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manuelreeb.monocontrol.domain.model.Alerta
import com.manuelreeb.monocontrol.domain.model.Movimiento
import com.manuelreeb.monocontrol.domain.model.ResumenMensual
import com.manuelreeb.monocontrol.domain.model.TipoMovimiento
import com.manuelreeb.monocontrol.utils.CategoriaCalculator
import com.manuelreeb.monocontrol.utils.CurrencyFormatter
import com.manuelreeb.monocontrol.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.Locale

// 🇦🇷 Paleta
private val Celeste    = Color(0xFF75AADB)
private val CelesteOsc = Color(0xFF4A86C8)
private val Amarillo   = Color(0xFFFBB81C)
private val Blanco     = Color(0xFFFFFFFF)
private val BlancoSuave = Color(0xFFF0F6FF)
private val TextoOscuro = Color(0xFF0D2A4A)
private val TextoSuave  = Color(0xFF6B7B8C)
private val GrisCeleste = Color(0xFFCCDFF4)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAgregarIngreso: () -> Unit,
    onVerAlertas: () -> Unit,
    onAgregarGasto: () -> Unit = onAgregarIngreso,
    onAgregarCliente: () -> Unit = {},
    onIrCategoria: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.actualizarAlertas() }

    Scaffold(
        containerColor = BlancoSuave,
        floatingActionButton = {
            // Si el usuario es PERSONAL, ocultamos la acción "Cliente" del FAB
            // (no necesita administrar clientes, solo su propio Monotributo).
            val acciones = buildList {
                add(FabAction("Ingreso", Icons.Default.TrendingUp, onAgregarIngreso))
                add(FabAction("Gasto", Icons.Default.TrendingDown, onAgregarGasto))
                if (state.rol == com.manuelreeb.monocontrol.domain.model.UserRole.CONTADOR) {
                    add(FabAction("Cliente", Icons.Default.PersonAdd, onAgregarCliente))
                }
                add(FabAction("Categoría", Icons.Default.Star, onIrCategoria))
            }
            SpeedDialFab(actions = acciones)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Chip de rubro (la TopAppBar ya muestra el logo y la campanita global)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Celeste.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        if (state.esVentaMuebles) "Rubro: venta de muebles" else "Rubro: servicios",
                        style = MaterialTheme.typography.labelMedium,
                        color = CelesteOsc,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            val resumen = state.resumen
            if (resumen == null) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Celeste)
                }
            } else {
                HomeSummaryContent(
                    resumen = resumen,
                    esVentaMuebles = state.esVentaMuebles,
                    ultimosMovimientos = state.ultimosMovimientos,
                    alertasNoLeidas = state.alertasNoLeidas,
                    onVerAlertas = onVerAlertas
                )
            }
        }
    }
}

@Composable
private fun HomeSummaryContent(
    resumen: ResumenMensual,
    esVentaMuebles: Boolean,
    ultimosMovimientos: List<Movimiento> = emptyList(),
    alertasNoLeidas: List<Alerta> = emptyList(),
    onVerAlertas: () -> Unit = {}
) {
    val cuota = resumen.categoriaActual.cuotaSegunRubro(esVentaMuebles)
    val facturadoAnual   = resumen.totalIngresosAnual          // ⬅ Suma total del año
    val proxima          = CategoriaCalculator.proximaCategoria(facturadoAnual)
    val promedioPermitido = CategoriaCalculator.promedioMensualPermitido(facturadoAnual)

    // ── Categoría actual ──
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = Celeste)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Categoría actual", style = MaterialTheme.typography.labelMedium, color = Blanco.copy(alpha = 0.85f))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Monotributo ${resumen.categoriaActual.letra}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Blanco
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Amarillo)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text("Cat. ${resumen.categoriaActual.letra}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextoOscuro)
                }
            }
            Text(
                "Cuota mensual: ${CurrencyFormatter.formatear(cuota)}",
                style = MaterialTheme.typography.bodySmall,
                color = Blanco.copy(alpha = 0.9f)
            )
        }
    }

    // ── Progreso al límite ──
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = Blanco),
        border   = androidx.compose.foundation.BorderStroke(1.5.dp, GrisCeleste)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "${DateUtils.nombreMes(resumen.mes)} ${resumen.anio}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextoOscuro
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Facturado este año", color = TextoOscuro)
                Text(CurrencyFormatter.formatear(facturadoAnual), fontWeight = FontWeight.Bold, color = CelesteOsc)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Límite cat. ${resumen.categoriaActual.letra}", color = TextoOscuro)
                Text(CurrencyFormatter.formatearCompacto(resumen.categoriaActual.limiteAnual), color = TextoOscuro)
            }

            val porcentaje = resumen.porcentajeDelLimite
            val colorBarra = when {
                porcentaje >= 0.9f -> Color(0xFFE53935)
                porcentaje >= 0.7f -> Amarillo
                else               -> Celeste
            }
            LinearProgressIndicator(
                progress    = { porcentaje },
                modifier    = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(7.dp)),
                color       = colorBarra,
                trackColor  = GrisCeleste
            )
            Text(
                "${(porcentaje * 100).toInt()}% del límite utilizado",
                style = MaterialTheme.typography.bodySmall,
                color = colorBarra,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    // ── 📰 Resumen / Novedades ──
    NovedadesCard(
        resumen = resumen,
        alertasNoLeidas = alertasNoLeidas,
        ultimosMovimientos = ultimosMovimientos,
        onVerAlertas = onVerAlertas
    )

    // ── Planificación: promedio mensual permitido + próxima categoría ──
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = Blanco),
        border   = androidx.compose.foundation.BorderStroke(1.5.dp, GrisCeleste)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("📋 Planificación anual", fontWeight = FontWeight.Bold, color = TextoOscuro)

            // Caso 1: ya superó el límite anual → no hay margen
            if (promedioPermitido <= 0.0) {
                InfoLine(
                    label = "Margen mensual disponible",
                    valor = "$0",
                    hint  = "Ya alcanzaste el límite anual de tu categoría."
                )
            } else {
                // Caso 2: margen normal — usamos formato completo para evitar “999K” ambiguos
                InfoLine(
                    label = "Podés facturar en promedio",
                    valor = "${CurrencyFormatter.formatear(promedioPermitido)}/mes",
                    hint  = "hasta fin de año sin subir de categoría"
                )
            }

            if (proxima != null) {
                val diferenciaCuota = proxima.cuotaSegunRubro(esVentaMuebles) - cuota
                InfoLine(
                    label = "Próxima categoría",
                    valor = "${proxima.letra} · ${CurrencyFormatter.formatear(proxima.cuotaSegunRubro(esVentaMuebles))}/mes",
                    hint  = "pagarías ${CurrencyFormatter.formatear(diferenciaCuota)} más de cuota"
                )
            } else {
                Text(
                    "⚠️ Estás en la categoría más alta (K). Si superás ${CurrencyFormatter.formatear(resumen.categoriaActual.limiteAnual)} quedás fuera del régimen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE53935)
                )
            }
        }
    }

    // ── Ingresos vs Gastos del mes ──
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = Blanco),
        border   = androidx.compose.foundation.BorderStroke(1.5.dp, GrisCeleste)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(Celeste))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Ingresos del mes", style = MaterialTheme.typography.labelSmall, color = TextoOscuro)
                Text(
                    CurrencyFormatter.formatear(resumen.totalIngresos),
                    fontWeight = FontWeight.ExtraBold,
                    color = CelesteOsc
                )
            }
            Box(modifier = Modifier.width(2.dp).height(50.dp).clip(RoundedCornerShape(1.dp)).background(Amarillo))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFE53935)))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Gastos del mes", style = MaterialTheme.typography.labelSmall, color = TextoOscuro)
                Text(
                    CurrencyFormatter.formatear(resumen.totalGastos),
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFE53935)
                )
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, valor: String, hint: String) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = TextoOscuro, fontWeight = FontWeight.SemiBold)
            Text(valor, color = CelesteOsc, fontWeight = FontWeight.Bold)
        }
        Text(hint, style = MaterialTheme.typography.bodySmall, color = TextoSuave)
    }
}

// ─────────────────────────────────────────────────────────────
// Card "Resumen / Novedades"
// ─────────────────────────────────────────────────────────────
@Composable
private fun NovedadesCard(
    resumen: ResumenMensual,
    alertasNoLeidas: List<Alerta>,
    ultimosMovimientos: List<Movimiento>,
    onVerAlertas: () -> Unit
) {
    val porcentaje = resumen.porcentajeDelLimite
    val tieneAlertas = alertasNoLeidas.isNotEmpty()
    val tieneMovs = ultimosMovimientos.isNotEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, GrisCeleste)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📰 Novedades", fontWeight = FontWeight.Bold, color = TextoOscuro)
                if (tieneAlertas) {
                    TextButton(onClick = onVerAlertas) {
                        Text("Ver todas", color = CelesteOsc, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // 1) Estado general según % de límite
            EstadoBanner(porcentaje = porcentaje, categoria = resumen.categoriaActual.letra)

            // 2) Alertas no leídas (top 3)
            if (tieneAlertas) {
                Divider(color = GrisCeleste)
                Text(
                    "🔔 Alertas pendientes",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextoOscuro,
                    fontWeight = FontWeight.SemiBold
                )
                alertasNoLeidas.forEach { alerta ->
                    AlertaItem(alerta)
                }
            }

            // 3) Últimos movimientos
            if (tieneMovs) {
                Divider(color = GrisCeleste)
                Text(
                    "🧾 Últimos movimientos",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextoOscuro,
                    fontWeight = FontWeight.SemiBold
                )
                ultimosMovimientos.forEach { mov ->
                    MovimientoItem(mov)
                }
            }

            if (!tieneAlertas && !tieneMovs) {
                Text(
                    "Todavía no registraste movimientos. Tocá el botón ➕ para empezar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSuave
                )
            }
        }
    }
}

@Composable
private fun EstadoBanner(porcentaje: Float, categoria: String) {
    val (icono, mensaje, color) = when {
        porcentaje >= 0.9f -> Triple(
            "🚨",
            "Estás muy cerca del tope de la categoría $categoria. Revisá si te conviene recategorizar.",
            Color(0xFFE53935)
        )
        porcentaje >= 0.7f -> Triple(
            "⚠️",
            "Llevás más del 70% del límite anual. Cuidá tus próximas facturaciones.",
            Amarillo
        )
        porcentaje >= 0.4f -> Triple(
            "📈",
            "Vas a buen ritmo en la categoría $categoria. Seguí registrando para no perderte nada.",
            CelesteOsc
        )
        else -> Triple(
            "✅",
            "Todo bajo control. Buen momento para planificar el resto del año.",
            Celeste
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(icono, fontSize = 20.sp)
        Text(
            mensaje,
            style = MaterialTheme.typography.bodySmall,
            color = TextoOscuro,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AlertaItem(alerta: Alerta) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Amarillo.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Amarillo)
        )
        Text(
            alerta.mensaje,
            style = MaterialTheme.typography.bodySmall,
            color = TextoOscuro,
            modifier = Modifier.weight(1f)
        )
        Text(
            "${(alerta.porcentaje * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = TextoOscuro,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MovimientoItem(mov: Movimiento) {
    val esIngreso = mov.tipo == TipoMovimiento.INGRESO
    val color = if (esIngreso) CelesteOsc else Color(0xFFE53935)
    val signo = if (esIngreso) "+" else "−"
    val fechaFmt = remember(mov.fecha) {
        SimpleDateFormat("dd MMM", Locale("es", "AR")).format(mov.fecha)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Column(Modifier.weight(1f)) {
            Text(
                mov.descripcion.ifBlank { if (esIngreso) "Ingreso" else "Gasto" },
                style = MaterialTheme.typography.bodyMedium,
                color = TextoOscuro,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                fechaFmt,
                style = MaterialTheme.typography.labelSmall,
                color = TextoSuave
            )
        }
        Text(
            "$signo ${CurrencyFormatter.formatear(mov.monto)}",
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}


