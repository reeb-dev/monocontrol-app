package com.manuelreeb.monocontrol.ui.movimientos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manuelreeb.monocontrol.domain.model.CategoriaMonotributo
import com.manuelreeb.monocontrol.domain.model.Movimiento
import com.manuelreeb.monocontrol.domain.model.TipoMovimiento
import com.manuelreeb.monocontrol.utils.CategoriaCalculator
import com.manuelreeb.monocontrol.utils.CurrencyFormatter
import com.manuelreeb.monocontrol.utils.DateUtils

private val Celeste     = Color(0xFF75AADB)
private val CelesteOsc  = Color(0xFF4A86C8)
private val Amarillo    = Color(0xFFFBB81C)
private val Blanco      = Color(0xFFFFFFFF)
private val BlancoSuave = Color(0xFFF0F6FF)
private val GrisCeleste = Color(0xFFCCDFF4)
private val TextoOscuro = Color(0xFF0D2A4A)
private val Verde       = Color(0xFF2E7D32)
private val Rojo        = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovimientoScreen(viewModel: MovimientoViewModel) {
    val movimientos by viewModel.movimientos.collectAsStateWithLifecycle()
    val guardadoExitoso by viewModel.guardadoExitoso.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val tope by viewModel.tope.collectAsStateWithLifecycle()

    var monto by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(TipoMovimiento.INGRESO) }

    // Regex: hasta 10 dígitos enteros y opcional , o . con 0–2 decimales.
    // El tope ABSOLUTO de la app es el límite anual de la categoría más alta (K).
    // Una vez superado, ya no hay categoría que cubra al usuario y queda fuera del régimen.
    val montoRegex = remember { Regex("^\\d{0,10}([.,]\\d{0,2})?$") }
    val MONTO_MAX = CategoriaMonotributo.K.limiteAnual

    val montoDouble = monto.replace(",", ".").toDoubleOrNull()
    val montoFormateado = montoDouble?.let { CurrencyFormatter.formatear(it) } ?: ""

    // Cálculo dinámico: si este es un INGRESO, ¿qué pasaría con la facturación anual?
    val totalSiAplicado = if (tipo == TipoMovimiento.INGRESO && montoDouble != null)
        tope.totalAnual + montoDouble else tope.totalAnual
    val categoriaSiAplicado = CategoriaCalculator.calcularCategoria(totalSiAplicado)
    val categoriaAnterior = tope.categoriaActual
    val cambiaDeCategoria =
        tipo == TipoMovimiento.INGRESO && montoDouble != null &&
            categoriaSiAplicado.ordinal > categoriaAnterior.ordinal
    val excedeTope = montoDouble != null && totalSiAplicado > MONTO_MAX

    LaunchedEffect(guardadoExitoso) {
        if (guardadoExitoso) {
            monto = ""
            descripcion = ""
            viewModel.resetGuardado()
        }
    }

    Scaffold(containerColor = BlancoSuave) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // ── FORMULARIO RÁPIDO ──────────────────────────────────────
            item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(18.dp),
                colors   = CardDefaults.cardColors(containerColor = Blanco),
                border   = androidx.compose.foundation.BorderStroke(1.5.dp, GrisCeleste)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(4.dp, 24.dp).background(Amarillo, RoundedCornerShape(2.dp)))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Agregar movimiento",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = TextoOscuro
                        )
                    }

                    // Selector Ingreso / Gasto
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier              = Modifier.fillMaxWidth()
                    ) {
                        TipoMovimiento.entries.forEach { t ->
                            FilterChip(
                                selected = tipo == t,
                                onClick  = { tipo = t },
                                label    = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment     = Alignment.CenterVertically
                                    ) {
                                        Text(if (t == TipoMovimiento.INGRESO) "💰" else "💸")
                                        Text(if (t == TipoMovimiento.INGRESO) "Ingreso" else "Gasto")
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (t == TipoMovimiento.INGRESO) Verde.copy(alpha = 0.2f) else Rojo.copy(alpha = 0.2f),
                                    selectedLabelColor     = if (t == TipoMovimiento.INGRESO) Verde else Rojo
                                )
                            )
                        }
                    }

                    // Campo Monto
                    OutlinedTextField(
                        value           = monto,
                        onValueChange   = { nuevo ->
                            // Solo aceptamos lo que matchea el regex (números + 1 separador decimal).
                            // El tope real depende de si es ingreso o gasto:
                            //  - INGRESO: el monto + lo facturado en el año no puede superar el tope de K.
                            //  - GASTO: solo limitamos a $9.999.999.999,99 para no romper el doble.
                            val limpio = nuevo.trimStart('0').let { if (it.startsWith(".") || it.startsWith(",")) "0$it" else it }
                            val candidato = if (nuevo.isEmpty()) "" else limpio.ifEmpty { nuevo }
                            if (candidato.isEmpty() || montoRegex.matches(candidato)) {
                                val v = candidato.replace(",", ".").toDoubleOrNull()
                                val limite = if (tipo == TipoMovimiento.INGRESO)
                                    (MONTO_MAX - tope.totalAnual).coerceAtLeast(0.0) + 0.01
                                else 9_999_999_999.99
                                if (v == null || v <= limite) monto = candidato
                            }
                        },
                        label           = { Text("Monto ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier        = Modifier.fillMaxWidth(),
                        singleLine      = true,
                        shape           = RoundedCornerShape(12.dp),
                        textStyle       = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextoOscuro
                        ),
                        isError         = excedeTope,
                        colors          = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CelesteOsc,
                            focusedLabelColor  = CelesteOsc,
                            cursorColor        = CelesteOsc
                        ),
                        leadingIcon     = { Text("$", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CelesteOsc) },
                        supportingText  = {
                            when {
                                excedeTope -> Text(
                                    "Supera el tope máximo del Monotributo (cat. K)",
                                    color = Rojo,
                                    fontWeight = FontWeight.SemiBold
                                )
                                montoFormateado.isNotBlank() -> Text(
                                    "= $montoFormateado",
                                    color = CelesteOsc,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                else -> Text(
                                    "Categoría actual: ${categoriaAnterior.letra}  •  Tope cat.: " +
                                        CurrencyFormatter.formatearCompacto(categoriaAnterior.limiteAnual),
                                    color = TextoOscuro.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    )

                    // ── Aviso dinámico de cambio de categoría / exceso de tope ──
                    when {
                        excedeTope -> AvisoCategoria(
                            color = Rojo,
                            titulo = "⛔ Quedarías fuera del régimen",
                            detalle = "Con este ingreso pasarías de la cat. ${categoriaAnterior.letra} y superarías el tope máximo " +
                                "(cat. K, ${CurrencyFormatter.formatearCompacto(MONTO_MAX)}). " +
                                "No podés cargarlo dentro del Monotributo."
                        )
                        cambiaDeCategoria -> AvisoCategoria(
                            color = Amarillo,
                            titulo = "⚠️ Te recategorizás",
                            detalle = "Pasás de la cat. ${categoriaAnterior.letra} → cat. ${categoriaSiAplicado.letra}. " +
                                "Total anual quedaría en ${CurrencyFormatter.formatear(totalSiAplicado)} " +
                                "(tope nueva categoría: ${CurrencyFormatter.formatearCompacto(categoriaSiAplicado.limiteAnual)})."
                        )
                    }

                    // Campo Descripción
                    OutlinedTextField(
                        value         = descripcion,
                        onValueChange = { descripcion = it },
                        label         = { Text("Descripción (opcional)") },
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true,
                        shape         = RoundedCornerShape(12.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CelesteOsc,
                            focusedLabelColor  = CelesteOsc,
                            cursorColor        = CelesteOsc
                        )
                    )

                    // Error
                    error?.let {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Rojo.copy(alpha = 0.1f))
                                .padding(10.dp)
                        ) {
                            Text(it, color = Rojo, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        LaunchedEffect(it) { viewModel.resetError() }
                    }

                    // Success
                    if (guardadoExitoso) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Verde.copy(alpha = 0.1f))
                                .padding(10.dp)
                        ) {
                            Text("✅ Movimiento guardado", color = Verde, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }

                    // Botón Guardar
                    Button(
                        onClick = {
                            if (montoDouble != null && !excedeTope && montoDouble > 0) {
                                viewModel.agregar(montoDouble, tipo, descripcion)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = CelesteOsc,
                            disabledContainerColor = GrisCeleste
                        ),
                        enabled  = montoDouble != null && montoDouble > 0 && !excedeTope
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Blanco, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Guardar movimiento", fontWeight = FontWeight.Bold, color = Blanco)
                    }
                }
            }
            }

            // ── RESUMEN RÁPIDO ─────────────────────────────────────────
            if (movimientos.isNotEmpty()) {
                val totalIngresos = movimientos.filter { it.tipo == TipoMovimiento.INGRESO }.sumOf { it.monto }
                val totalGastos   = movimientos.filter { it.tipo == TipoMovimiento.GASTO }.sumOf { it.monto }
                val neto          = totalIngresos - totalGastos

                item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = CardDefaults.cardColors(containerColor = Celeste)
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Resumen de hoy", style = MaterialTheme.typography.labelMedium,
                                color = Blanco.copy(alpha = 0.85f))
                            Text(
                                "Neto: ${CurrencyFormatter.formatear(neto)}",
                                fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Amarillo
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("💰 Ingresos", style = MaterialTheme.typography.labelSmall, color = Blanco.copy(alpha = 0.8f))
                                Text(CurrencyFormatter.formatearCompacto(totalIngresos), fontWeight = FontWeight.Bold, color = Blanco)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("💸 Gastos", style = MaterialTheme.typography.labelSmall, color = Blanco.copy(alpha = 0.8f))
                                Text(CurrencyFormatter.formatearCompacto(totalGastos), fontWeight = FontWeight.Bold, color = Blanco)
                            }
                        }
                    }
                }
                }
            }

            // ── HISTORIAL ──────────────────────────────────────────────
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(4.dp, 20.dp).background(Celeste, RoundedCornerShape(2.dp)))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Historial (${movimientos.size})",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = TextoOscuro
                    )
                }
            }

            if (movimientos.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("📋", fontSize = 40.sp)
                            Text("Sin movimientos", fontWeight = FontWeight.SemiBold, color = TextoOscuro)
                            Text("Agrega tu primer ingreso o gasto", style = MaterialTheme.typography.bodySmall,
                                color = TextoOscuro.copy(alpha = 0.6f))
                        }
                    }
                }
            } else {
                items(movimientos.sortedByDescending { it.fecha }, key = { it.id }) { mov ->
                    MovimientoItem(mov, onEliminar = { viewModel.eliminar(mov) })
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun MovimientoItem(movimiento: Movimiento, onEliminar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(
            containerColor = if (movimiento.tipo == TipoMovimiento.INGRESO) Verde.copy(alpha = 0.05f) else Rojo.copy(alpha = 0.05f)
        ),
        border   = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (movimiento.tipo == TipoMovimiento.INGRESO) Verde.copy(alpha = 0.3f) else Rojo.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // Emoji + Descripción
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier.weight(1f)
            ) {
                Text(
                    if (movimiento.tipo == TipoMovimiento.INGRESO) "💰" else "💸",
                    fontSize = 20.sp
                )
                Column {
                    Text(
                        movimiento.descripcion.ifBlank { if (movimiento.tipo == TipoMovimiento.INGRESO) "Ingreso" else "Gasto" },
                        fontWeight = FontWeight.SemiBold, color = TextoOscuro
                    )
                    Text(
                        DateUtils.formatear(movimiento.fecha),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoOscuro.copy(alpha = 0.6f)
                    )
                }
            }

            // Monto
            Text(
                (if (movimiento.tipo == TipoMovimiento.INGRESO) "+" else "−") + CurrencyFormatter.formatear(movimiento.monto),
                fontWeight = FontWeight.ExtraBold,
                color      = if (movimiento.tipo == TipoMovimiento.INGRESO) Verde else Rojo,
                fontSize   = 14.sp
            )

            // Botón eliminar
            IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Rojo, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun AvisoCategoria(color: Color, titulo: String, detalle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(titulo, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(detalle, color = TextoOscuro, fontSize = 12.sp)
        }
    }
}

