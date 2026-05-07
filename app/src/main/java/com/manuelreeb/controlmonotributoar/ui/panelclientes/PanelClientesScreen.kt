package com.reeb.controlmonotributoar.ui.panelclientes

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reeb.controlmonotributoar.ui.theme.*
import com.reeb.controlmonotributoar.utils.PanelClientesExporter
import com.reeb.controlmonotributoar.utils.CurrencyFormatter
import androidx.compose.ui.platform.LocalContext
import com.reeb.controlmonotributoar.utils.ScreenTourPrefs
import com.reeb.controlmonotributoar.ui.theme.AppRadius
import androidx.compose.animation.animateContentSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelClientesScreen(
    viewModel: PanelClientesViewModel,
    onBack: () -> Unit,
    onAbrirCobranzas: () -> Unit = {}
) {
    val context = LocalContext.current
    val resumen by viewModel.resumen.collectAsStateWithLifecycle()
    val clientes by viewModel.clientesConStats.collectAsStateWithLifecycle()
    val criticosSemana by viewModel.criticosSemana.collectAsStateWithLifecycle()
    val filtro by viewModel.filtro.collectAsStateWithLifecycle()
    val weights by viewModel.weights.collectAsStateWithLifecycle()
    var mesesGrafico by rememberSaveable { mutableStateOf(12) }
    var mostrarTourPanel by rememberSaveable { mutableStateOf(false) }
    var modoPro by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        mostrarTourPanel = !ScreenTourPrefs.wasSeenPanel(context)
    }

    Scaffold(
        containerColor = BlancoSuave,
        topBar = {
            TopAppBar(
                title = { Text("Panel de clientes", fontWeight = FontWeight.Bold, color = TextoOscuro) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = TextoOscuro)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BlancoSuave)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Text("Panel de gestión de clientes", style = MaterialTheme.typography.titleMedium, color = TextoOscuro)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppRadius.xl,
                    colors = CardDefaults.cardColors(containerColor = BlancoArgentino)
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Group, null, tint = CelesteOscuro, modifier = Modifier.size(20.dp))
                            Text("Cartera de clientes", fontWeight = FontWeight.Bold, color = TextoOscuro)
                        }

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatCard("Total", "${resumen.total}", CelesteOscuro, Modifier.weight(1f))
                            StatCard("Críticos", "${resumen.criticos}", RojoMedio, Modifier.weight(1f))
                            StatCard("Vencen 7d", "${resumen.vencenEn7Dias}", Color(0xFFE65100), Modifier.weight(1f))
                            StatCard("Sin datos", "${resumen.sinDatos}", GrisMedio, Modifier.weight(1f))
                        }

                        Text(
                            "Score de riesgo: combina % de límite anual, vencimiento y calidad de datos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrisMedio
                        )
                        Text(
                            "Leyenda: 0-44 bajo · 45-74 medio · 75-100 crítico",
                            style = MaterialTheme.typography.labelSmall,
                            color = GrisMedio
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FiltroChip("Todos", filtro == "todos") { viewModel.setFiltro("todos") }
                    FiltroChip("Críticos", filtro == "criticos") { viewModel.setFiltro("criticos") }
                    FiltroChip("Vencen 7d", filtro == "vencen_7") { viewModel.setFiltro("vencen_7") }
                    FiltroChip("Sin datos", filtro == "sin_datos") { viewModel.setFiltro("sin_datos") }
                }
            }
            item {
                FilterChip(
                    selected = modoPro,
                    onClick = { modoPro = !modoPro },
                    label = { Text(if (modoPro) "Modo Pro activado" else "Modo Pro") }
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
                    shape = AppRadius.md
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Ponderación del score", color = TextoOscuro, fontWeight = FontWeight.Bold)
                        Text("Límite ${(weights.limite * 100).toInt()}% · Vencimiento ${(weights.vencimiento * 100).toInt()}% · Datos ${(weights.calidadDatos * 100).toInt()}%", color = GrisMedio, style = MaterialTheme.typography.bodySmall)
                        Text("Peso límite", style = MaterialTheme.typography.labelSmall, color = GrisMedio)
                        Slider(
                            value = weights.limite,
                            onValueChange = { viewModel.setWeights(weights.copy(limite = it)) },
                            valueRange = 0.1f..0.8f
                        )
                        Text("Peso vencimiento", style = MaterialTheme.typography.labelSmall, color = GrisMedio)
                        Slider(
                            value = weights.vencimiento,
                            onValueChange = { viewModel.setWeights(weights.copy(vencimiento = it)) },
                            valueRange = 0.1f..0.8f
                        )
                        Text("Peso calidad de datos", style = MaterialTheme.typography.labelSmall, color = GrisMedio)
                        Slider(
                            value = weights.calidadDatos,
                            onValueChange = { viewModel.setWeights(weights.copy(calidadDatos = it)) },
                            valueRange = 0.1f..0.6f
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            val i = PanelClientesExporter.exportarRankingExcel(context, clientes)
                            context.startActivity(Intent.createChooser(i, "Exportar ranking (Excel)"))
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Exportar Excel") }
                    OutlinedButton(
                        onClick = {
                            val i = PanelClientesExporter.exportarRankingPdf(context, clientes)
                            context.startActivity(Intent.createChooser(i, "Exportar ranking (PDF)"))
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Exportar PDF") }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    AssistChip(
                        onClick = { mesesGrafico = 6 },
                        label = { Text("Gráfico 6m") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (mesesGrafico == 6) CelesteOscuro.copy(alpha = 0.18f) else BlancoArgentino
                        )
                    )
                    AssistChip(
                        onClick = { mesesGrafico = 12 },
                        label = { Text("Gráfico 12m") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (mesesGrafico == 12) CelesteOscuro.copy(alpha = 0.18f) else BlancoArgentino
                        )
                    )
                    OutlinedButton(
                        onClick = {
                            val i = PanelClientesExporter.exportarCriticosSemanaPdf(context, criticosSemana)
                            context.startActivity(Intent.createChooser(i, "Exportar críticos de la semana"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("PDF críticos semana")
                    }
                }
            }
            item {
                OutlinedButton(
                    onClick = onAbrirCobranzas,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AttachMoney, contentDescription = null, tint = CelesteOscuro)
                    Spacer(Modifier.width(6.dp))
                    Text("Abrir vista de cobranzas")
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
                    shape = AppRadius.md,
                    border = BorderStroke(1.dp, GrisClaro)
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Clientes críticos de la semana", color = TextoOscuro, fontWeight = FontWeight.SemiBold)
                        if (criticosSemana.isEmpty()) {
                            Text("Sin clientes críticos por ahora. Buen trabajo 👌", color = GrisMedio, style = MaterialTheme.typography.bodySmall)
                        } else {
                            criticosSemana.take(5).forEachIndexed { index, item ->
                                Text(
                                    "${index + 1}. ${item.cliente.nombre} · score ${item.scoreRiesgo} · vto ${item.diasAVencimiento ?: "N/D"}d",
                                    color = GrisMedio,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Ranking de riesgo por cliente",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextoOscuro,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (clientes.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "No hay clientes para el filtro seleccionado.",
                            modifier = Modifier.padding(16.dp),
                            color = GrisMedio
                        )
                    }
                }
            } else {
                items(clientes.take(if (modoPro) 25 else 15), key = { it.cliente.id }) { item ->
                    ClienteRiesgoCard(item, mesesGrafico)
                }
            }
        }
    }

    if (mostrarTourPanel) {
        AlertDialog(
            onDismissRequest = {
                ScreenTourPrefs.markSeenPanel(context)
                mostrarTourPanel = false
            },
            title = { Text("Tour rápido: Panel", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1) Ajustá ponderaciones para tu criterio de riesgo.", style = MaterialTheme.typography.bodySmall, color = GrisMedio)
                    Text("2) Cambiá entre 6m y 12m para leer tendencias.", style = MaterialTheme.typography.bodySmall, color = GrisMedio)
                    Text("3) Exportá Excel/PDF para seguimiento semanal.", style = MaterialTheme.typography.bodySmall, color = GrisMedio)
                }
            },
            confirmButton = {
                Button(onClick = {
                    ScreenTourPrefs.markSeenPanel(context)
                    mostrarTourPanel = false
                }) { Text("Entendido") }
            }
        )
    }
}

@Composable
private fun StatCard(title: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.09f))
    ) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = GrisMedio)
            Text(value, fontWeight = FontWeight.Bold, color = color, fontSize = 18.sp)
        }
    }
}

@Composable
private fun FiltroChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CelesteOscuro,
            selectedLabelColor = BlancoArgentino
        )
    )
}

@Composable
private fun ClienteRiesgoCard(item: ClienteStats, mesesGrafico: Int) {
    val colorRiesgo = when {
        item.scoreRiesgo >= 75 -> RojoMedio
        item.scoreRiesgo >= 45 -> Color(0xFFE65100)
        else -> VerdeExito
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, colorRiesgo.copy(alpha = 0.25f))
    ) {
        Column(
            Modifier
                .padding(14.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(colorRiesgo.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${item.scoreRiesgo}",
                        color = colorRiesgo,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.cliente.nombre, color = TextoOscuro, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        buildString {
                            append(item.cliente.categoria?.let { "Cat. $it" } ?: "Cat. automática")
                            item.diasAVencimiento?.let { append(" • Vto: $it días") }
                            append(" • ${item.tendencia}")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = GrisMedio
                    )
                }
                Text(
                    "${(item.porcentajeLimite * 100).toInt()}%",
                    color = colorRiesgo,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            LinearProgressIndicator(
                progress = { item.porcentajeLimite.coerceIn(0f, 1f) },
                color = colorRiesgo,
                trackColor = GrisClaro,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Facturación anual", color = GrisMedio, style = MaterialTheme.typography.labelSmall)
                Text(CurrencyFormatter.formatear(item.facturacionAnio), color = TextoOscuro, fontWeight = FontWeight.SemiBold)
            }
            if (item.serieMensual.isNotEmpty()) {
                MiniBarsChart(values = item.serieMensual, barColor = colorRiesgo, meses = mesesGrafico)
            }
            if (item.facturacionAnio <= 0.0) {
                Text(
                    "Sin datos cargados de ingresos anuales para este cliente.",
                    color = Color(0xFFE65100),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun MiniBarsChart(values: List<Double>, barColor: Color, meses: Int) {
    val clean = values.takeLast(meses.coerceIn(3, 12))
    val max = clean.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(GrisClaro.copy(alpha = 0.5f))
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        if (clean.isEmpty()) return@Canvas
        val gap = 4f
        val barW = ((size.width - gap * (clean.size - 1)) / clean.size).coerceAtLeast(2f)
        clean.forEachIndexed { i, v ->
            val h = ((v / max).toFloat() * size.height).coerceAtLeast(2f)
            val x = i * (barW + gap)
            drawRect(
                color = barColor.copy(alpha = 0.85f),
                topLeft = androidx.compose.ui.geometry.Offset(x, size.height - h),
                size = androidx.compose.ui.geometry.Size(barW, h)
            )
        }
    }
}

