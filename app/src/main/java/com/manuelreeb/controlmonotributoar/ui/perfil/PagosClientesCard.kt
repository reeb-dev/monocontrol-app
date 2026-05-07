package com.reeb.controlmonotributoar.ui.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reeb.controlmonotributoar.data.local.entity.ClienteEntity
import com.reeb.controlmonotributoar.ui.theme.*
import com.reeb.controlmonotributoar.utils.PagosMonotributoHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagosClientesCard(viewModel: PerfilViewModel) {
    val clientes by viewModel.clientes.collectAsStateWithLifecycle()
    val periodoActual = remember { PagosMonotributoHelper.periodoActual() }
    val nombrePeriodo = remember { PagosMonotributoHelper.nombrePeriodo(periodoActual) }

    // Periodos disponibles (últimos 6 meses)
    val periodos = remember { PagosMonotributoHelper.ultimosPeriodos(6) }
    var periodoSeleccionado by remember { mutableStateOf(periodoActual) }
    var expandidoPeriodo by remember { mutableStateOf(false) }

    // Cliente seleccionado para el sheet de historial
    var clienteSheet by remember { mutableStateOf<ClienteEntity?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Mostrar historial de pagos de un cliente
    clienteSheet?.let { cli ->
        ModalBottomSheet(
            onDismissRequest = { clienteSheet = null },
            sheetState = sheetState,
            containerColor = BlancoArgentino
        ) {
            HistorialPagosSheet(
                cliente = cli,
                periodos = periodos,
                onTogglePago = { periodo, estadoActual ->
                    val nuevo = if (estadoActual == PagosMonotributoHelper.EstadoPago.PAGADO)
                        PagosMonotributoHelper.EstadoPago.PENDIENTE
                    else PagosMonotributoHelper.EstadoPago.PAGADO
                    viewModel.actualizarPago(cli, periodo, nuevo)
                },
                onCerrar = { clienteSheet = null }
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, CelesteBorde)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                        .background(CelesteOscuro.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Payments, null, tint = CelesteOscuro, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text("Pagos Monotributo", fontWeight = FontWeight.Bold, color = TextoOscuro, fontSize = 15.sp)
                    Text("Clientes · $nombrePeriodo", style = MaterialTheme.typography.labelSmall, color = GrisMedio)
                }
            }

            // Selector de período
            ExposedDropdownMenuBox(
                expanded = expandidoPeriodo,
                onExpandedChange = { expandidoPeriodo = it }
            ) {
                OutlinedTextField(
                    value = PagosMonotributoHelper.nombrePeriodo(periodoSeleccionado),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Período") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoPeriodo) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CelesteOscuro, cursorColor = CelesteOscuro)
                )
                ExposedDropdownMenu(expanded = expandidoPeriodo, onDismissRequest = { expandidoPeriodo = false }) {
                    periodos.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(PagosMonotributoHelper.nombrePeriodo(p)) },
                            onClick = { periodoSeleccionado = p; expandidoPeriodo = false }
                        )
                    }
                }
            }

            if (clientes.isEmpty()) {
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .background(GrisClaro).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay clientes cargados.", style = MaterialTheme.typography.bodySmall, color = GrisMedio)
                }
            } else {
                // Progreso del período seleccionado
                val activos = clientes.filter { !it.eliminado }
                val pagados = activos.count {
                    PagosMonotributoHelper.estadoDe(it.pagosMonotributoJson, periodoSeleccionado) ==
                        PagosMonotributoHelper.EstadoPago.PAGADO
                }
                val totalActivos = activos.size
                val progreso = if (totalActivos > 0) pagados.toFloat() / totalActivos else 0f

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Progreso del período", style = MaterialTheme.typography.labelMedium, color = GrisMedio)
                        Text(
                            "$pagados / $totalActivos pagaron",
                            fontWeight = FontWeight.Bold,
                            color = if (progreso >= 1f) VerdeExito else CelesteOscuro,
                            fontSize = 13.sp
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progreso },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = if (progreso >= 1f) VerdeExito else CelesteOscuro,
                        trackColor = GrisClaro
                    )
                }

                HorizontalDivider(color = GrisClaro)

                // Lista de clientes
                activos.sortedBy { it.nombre }.forEach { cliente ->
                    val estado = PagosMonotributoHelper.estadoDe(
                        cliente.pagosMonotributoJson, periodoSeleccionado
                    )
                    ClientePagoRow(
                        cliente = cliente,
                        estado = estado,
                        onToggle = {
                            val nuevo = if (estado == PagosMonotributoHelper.EstadoPago.PAGADO)
                                PagosMonotributoHelper.EstadoPago.PENDIENTE
                            else PagosMonotributoHelper.EstadoPago.PAGADO
                            viewModel.actualizarPago(cliente, periodoSeleccionado, nuevo)
                        },
                        onVerHistorial = { clienteSheet = cliente }
                    )
                }
            }
        }
    }
}

@Composable
private fun ClientePagoRow(
    cliente: ClienteEntity,
    estado: PagosMonotributoHelper.EstadoPago,
    onToggle: () -> Unit,
    onVerHistorial: () -> Unit
) {
    val (colorFondo, colorTexto, colorBorde) = when (estado) {
        PagosMonotributoHelper.EstadoPago.PAGADO ->
            Triple(VerdeExito.copy(alpha = 0.08f), VerdeExito, VerdeExito.copy(alpha = 0.3f))
        PagosMonotributoHelper.EstadoPago.VENCIDO ->
            Triple(RojoMedio.copy(alpha = 0.08f), RojoMedio, RojoMedio.copy(alpha = 0.3f))
        else ->
            Triple(GrisClaro.copy(alpha = 0.5f), GrisMedio, GrisClaro)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorFondo)
            .border(1.dp, colorBorde, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Avatar con inicial
        Box(
            Modifier.size(38.dp).clip(CircleShape)
                .background(CelesteOscuro.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                cliente.nombre.firstOrNull()?.uppercase() ?: "?",
                fontWeight = FontWeight.ExtraBold,
                color = CelesteOscuro,
                fontSize = 16.sp
            )
        }

        // Datos del cliente
        Column(Modifier.weight(1f)) {
            Text(cliente.nombre, fontWeight = FontWeight.SemiBold, color = TextoOscuro, fontSize = 14.sp, maxLines = 1)
            Text(
                buildString {
                    append(cliente.categoria?.let { "Cat. $it" } ?: "Sin cat.")
                    if (cliente.cuit.isNotBlank()) append(" · CUIT ${cliente.cuit}")
                },
                style = MaterialTheme.typography.labelSmall,
                color = GrisMedio
            )
        }

        // Chip de estado
        Box(
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(colorTexto.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                "${estado.emoji} ${estado.label}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colorTexto
            )
        }

        // Botón toggle pago
        IconButton(
            onClick = onToggle,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                if (estado == PagosMonotributoHelper.EstadoPago.PAGADO)
                    Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = "Toggle pago",
                tint = if (estado == PagosMonotributoHelper.EstadoPago.PAGADO) VerdeExito else GrisMedio,
                modifier = Modifier.size(24.dp)
            )
        }

        // Ver historial
        IconButton(onClick = onVerHistorial, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.History, contentDescription = "Historial", tint = CelesteOscuro, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun HistorialPagosSheet(
    cliente: ClienteEntity,
    periodos: List<String>,
    onTogglePago: (periodo: String, estadoActual: PagosMonotributoHelper.EstadoPago) -> Unit,
    onCerrar: () -> Unit
) {
    val pagos = PagosMonotributoHelper.parsear(cliente.pagosMonotributoJson)

    Column(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp).padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(52.dp).clip(CircleShape).background(CelesteOscuro),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    cliente.nombre.firstOrNull()?.uppercase() ?: "?",
                    fontWeight = FontWeight.ExtraBold,
                    color = BlancoArgentino,
                    fontSize = 24.sp
                )
            }
            Column(Modifier.weight(1f)) {
                Text(cliente.nombre, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextoOscuro)
                Text(
                    buildString {
                        append(cliente.categoria?.let { "Cat. $it" } ?: "Sin categoría")
                        if (cliente.cuit.isNotBlank()) append(" · CUIT ${cliente.cuit}")
                    },
                    style = MaterialTheme.typography.bodySmall, color = GrisMedio
                )
            }
            IconButton(onClick = onCerrar) {
                Icon(Icons.Default.Close, null, tint = GrisMedio)
            }
        }

        // Resumen de pagados
        val totalPagados = pagos.values.count { it == PagosMonotributoHelper.EstadoPago.PAGADO }
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(VerdeExito.copy(alpha = 0.10f))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.BarChart, null, tint = VerdeExito, modifier = Modifier.size(20.dp))
                Text(
                    "$totalPagados meses registrados como pagados en total",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoOscuro,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        HorizontalDivider(color = GrisClaro)

        Text("Historial de pagos (últimos 6 meses)", fontWeight = FontWeight.Bold, color = TextoOscuro)

        // Lista de períodos
        periodos.forEach { periodo ->
            val estado = pagos[periodo] ?: PagosMonotributoHelper.EstadoPago.PENDIENTE
            val esPagado = estado == PagosMonotributoHelper.EstadoPago.PAGADO
            val esActual = periodo == PagosMonotributoHelper.periodoActual()

            Row(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (esPagado) VerdeExito.copy(0.08f) else GrisClaro.copy(0.4f))
                    .border(
                        1.dp,
                        if (esPagado) VerdeExito.copy(0.3f) else if (esActual) CelesteOscuro.copy(0.4f) else GrisClaro,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    if (esPagado) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    null,
                    tint = if (esPagado) VerdeExito else GrisMedio,
                    modifier = Modifier.size(22.dp)
                )
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            PagosMonotributoHelper.nombrePeriodo(periodo),
                            fontWeight = FontWeight.SemiBold,
                            color = TextoOscuro,
                            fontSize = 14.sp
                        )
                        if (esActual) {
                            Box(
                                Modifier.clip(RoundedCornerShape(4.dp))
                                    .background(CelesteOscuro.copy(0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Mes actual", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CelesteOscuro)
                            }
                        }
                    }
                    Text(
                        "${estado.emoji} ${estado.label}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (esPagado) VerdeExito else GrisMedio
                    )
                }
                FilledTonalButton(
                    onClick = { onTogglePago(periodo, estado) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (esPagado) RojoMedio.copy(0.12f) else VerdeExito.copy(0.12f),
                        contentColor = if (esPagado) RojoMedio else VerdeExito
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        if (esPagado) "Desmarcar" else "Marcar pagado",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
