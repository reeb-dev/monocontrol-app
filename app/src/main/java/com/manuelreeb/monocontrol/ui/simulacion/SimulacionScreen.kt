package com.manuelreeb.monocontrol.ui.simulacion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manuelreeb.monocontrol.domain.model.CategoriaMonotributo
import com.manuelreeb.monocontrol.utils.CurrencyFormatter

private val Celeste      = Color(0xFF75AADB)
private val CelesteOsc   = Color(0xFF4A86C8)
private val Amarillo     = Color(0xFFFBB81C)
private val Blanco       = Color(0xFFFFFFFF)
private val BlancoSuave  = Color(0xFFF0F6FF)
private val GrisCeleste  = Color(0xFFCCDFF4)
private val TextoOscuro  = Color(0xFF0D2A4A)
private val Verde        = Color(0xFF2E7D32)
private val Rojo         = Color(0xFFE53935)

@Composable
fun SimulacionScreen() {
    var facturacionActual by remember { mutableStateOf("") }
    var facturacionExtra  by remember { mutableStateOf("") }

    val actualDouble  = facturacionActual.toDoubleOrNull() ?: 0.0
    val extraDouble   = facturacionExtra.toDoubleOrNull()  ?: 0.0
    val totalSimulado = actualDouble + extraDouble

    val categoriaActual  = CategoriaMonotributo.porLimiteAnual(actualDouble)
    val categoriaSimulada = CategoriaMonotributo.porLimiteAnual(totalSimulado)
    val sube             = categoriaSimulada.ordinal > categoriaActual.ordinal
    val diferenciaCuota  = categoriaSimulada.cuotaMensual - categoriaActual.cuotaMensual

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlancoSuave)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── HEADER ─────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Celeste)
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("🔮  Simulador de recategorización",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = Blanco)
                Text("Calculá cómo afecta facturar más en tu categoría",
                    style = MaterialTheme.typography.bodySmall,
                    color = Blanco.copy(alpha = 0.85f))
            }
        }

        // ── INPUTS ─────────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(16.dp),
            colors   = CardDefaults.cardColors(containerColor = Blanco),
            border   = androidx.compose.foundation.BorderStroke(1.5.dp, GrisCeleste)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(4.dp, 22.dp).background(Amarillo, RoundedCornerShape(2.dp)))
                    Spacer(Modifier.width(8.dp))
                    Text("Ingresá los montos", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, color = TextoOscuro)
                }

                OutlinedTextField(
                    value         = facturacionActual,
                    onValueChange = { facturacionActual = it },
                    label         = { Text("Facturación acumulada actual ($)") },
                    modifier      = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CelesteOsc,
                        focusedLabelColor  = CelesteOsc,
                        cursorColor        = CelesteOsc
                    )
                )

                OutlinedTextField(
                    value         = facturacionExtra,
                    onValueChange = { facturacionExtra = it },
                    label         = { Text("¿Cuánto más pensás facturar? ($)") },
                    modifier      = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine    = true,
                    shape         = RoundedCornerShape(12.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CelesteOsc,
                        focusedLabelColor  = CelesteOsc,
                        cursorColor        = CelesteOsc
                    )
                )
            }
        }

        // ── RESULTADO ──────────────────────────────────────────────────────
        if (actualDouble > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = if (sube) Rojo.copy(alpha = 0.08f) else Verde.copy(alpha = 0.08f)
                ),
                border   = androidx.compose.foundation.BorderStroke(
                    2.dp, if (sube) Rojo else Verde
                )
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (sube) "⚠️  Te recategorizás" else "✅  Quedás en la misma categoría",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 16.sp,
                        color      = if (sube) Rojo else Verde
                    )

                    HorizontalDivider(color = if (sube) Rojo.copy(0.2f) else Verde.copy(0.2f))

                    // Antes / Después
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CatBox("Categoría actual", categoriaActual, Celeste)
                        if (sube) {
                            Text("→", fontSize = 28.sp, fontWeight = FontWeight.Bold,
                                color = Rojo, modifier = Modifier.align(Alignment.CenterVertically))
                            CatBox("Con simulación", categoriaSimulada, Rojo)
                        } else {
                            Text("=", fontSize = 28.sp, fontWeight = FontWeight.Bold,
                                color = Verde, modifier = Modifier.align(Alignment.CenterVertically))
                            CatBox("Con simulación", categoriaSimulada, Verde)
                        }
                    }

                    HorizontalDivider(color = GrisCeleste)

                    // Datos del impacto
                    SimResultRow("💰", "Total simulado", CurrencyFormatter.formatear(totalSimulado))
                    SimResultRow("📋", "Nueva cuota mensual",
                        CurrencyFormatter.formatear(categoriaSimulada.cuotaMensual))
                    if (sube) {
                        SimResultRow("📈", "Aumento de cuota",
                            "+ ${CurrencyFormatter.formatear(diferenciaCuota)}",
                            colorValor = Rojo)
                        SimResultRow("📅", "Próxima recategorización", "Enero / Julio")
                    }
                    SimResultRow("🏁", "Límite nueva categoría",
                        CurrencyFormatter.formatear(categoriaSimulada.limiteAnual))
                }
            }
        }

        // ── TABLA REFERENCIA RÁPIDA ────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(16.dp),
            colors   = CardDefaults.cardColors(containerColor = Blanco),
            border   = androidx.compose.foundation.BorderStroke(1.5.dp, GrisCeleste)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(4.dp, 22.dp).background(Celeste, RoundedCornerShape(2.dp)))
                    Spacer(Modifier.width(8.dp))
                    Text("Referencia rápida 2025",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, color = TextoOscuro)
                }
                // Encabezado tabla
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GrisCeleste)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Cat.", fontWeight = FontWeight.Bold, color = CelesteOsc, modifier = Modifier.width(36.dp))
                    Text("Límite anual", fontWeight = FontWeight.Bold, color = CelesteOsc, modifier = Modifier.weight(1f))
                    Text("Cuota/mes", fontWeight = FontWeight.Bold, color = CelesteOsc)
                }
                CategoriaMonotributo.entries.forEach { cat ->
                    val esResaltada = cat == categoriaActual || cat == categoriaSimulada
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    cat == categoriaSimulada && sube -> Rojo.copy(alpha = 0.08f)
                                    cat == categoriaActual           -> Celeste.copy(alpha = 0.15f)
                                    else                             -> Color.Transparent
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            cat.letra,
                            fontWeight = if (esResaltada) FontWeight.ExtraBold else FontWeight.Normal,
                            color      = if (esResaltada) CelesteOsc else TextoOscuro,
                            modifier   = Modifier.width(36.dp)
                        )
                        Text(
                            CurrencyFormatter.formatearCompacto(cat.limiteAnual),
                            style  = MaterialTheme.typography.bodySmall,
                            color  = TextoOscuro,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            CurrencyFormatter.formatearCompacto(cat.cuotaMensual),
                            style     = MaterialTheme.typography.bodySmall,
                            color     = if (cat == categoriaSimulada && sube) Rojo else TextoOscuro,
                            fontWeight = if (esResaltada) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CatBox(label: String, cat: CategoriaMonotributo, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextoOscuro.copy(alpha = 0.6f))
        Box(
            modifier         = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(cat.letra, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = Blanco)
        }
        Text(CurrencyFormatter.formatearCompacto(cat.cuotaMensual) + "/mes",
            style = MaterialTheme.typography.labelSmall, color = TextoOscuro.copy(alpha = 0.7f))
    }
}

@Composable
private fun SimResultRow(emoji: String, label: String, valor: String, colorValor: Color = TextoOscuro) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 15.sp)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TextoOscuro.copy(alpha = 0.7f))
        }
        Text(valor, fontWeight = FontWeight.Bold, color = colorValor)
    }
}

