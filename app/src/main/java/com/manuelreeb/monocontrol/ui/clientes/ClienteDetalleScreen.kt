package com.manuelreeb.monocontrol.ui.clientes

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manuelreeb.monocontrol.domain.model.CategoriaMonotributo
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// ── Paleta ──────────────────────────────────────────────────────────────────
private val Celeste     = Color(0xFF75AADB)
private val CelesteOsc  = Color(0xFF4A86C8)
private val Amarillo    = Color(0xFFFBB81C)
private val Blanco      = Color(0xFFFFFFFF)
private val BlancoSuave = Color(0xFFF0F6FF)
private val GrisCeleste = Color(0xFFCCDFF4)
private val TextoOscuro = Color(0xFF0D2A4A)
private val TextoSuave  = Color(0xFF6B7B8C)
private val Rojo        = Color(0xFFE53935)
private val Verde       = Color(0xFF2E7D32)
private val VerdeClaro  = Color(0xFFE8F5E9)
private val RojoClaro   = Color(0xFFFFEBEE)
private val AmarilloClaro = Color(0xFFFFF8E1)
private val NaranjaVenc = Color(0xFFFF6D00)

// ── Helpers ──────────────────────────────────────────────────────────────────
private fun diasHastaVencimiento(epoch: Long): Long? {
    if (epoch == 0L) return null
    val hoy = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    return (epoch - hoy) / (1000 * 60 * 60 * 24)
}

private fun formatFecha(epoch: Long): String {
    if (epoch == 0L) return "Sin fecha"
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(epoch))
}

private fun estadoColorBg(estado: String, diasVenc: Long?): Color = when {
    diasVenc != null && diasVenc < 0 -> RojoClaro
    diasVenc != null && diasVenc <= 5 -> AmarilloClaro
    estado == "inactivo" -> Color(0xFFF5F5F5)
    else -> VerdeClaro
}

private fun estadoColorText(estado: String, diasVenc: Long?): Color = when {
    diasVenc != null && diasVenc < 0 -> Rojo
    diasVenc != null && diasVenc <= 5 -> NaranjaVenc
    estado == "inactivo" -> TextoSuave
    else -> Verde
}

private fun estadoLabel(estado: String, diasVenc: Long?): String = when {
    diasVenc != null && diasVenc < 0  -> "⛔ Vencido hace ${-diasVenc} días"
    diasVenc != null && diasVenc == 0L -> "⚠️ Vence HOY"
    diasVenc != null && diasVenc <= 5  -> "⚠️ Vence en $diasVenc días"
    diasVenc != null                   -> "✅ Al día — vence en $diasVenc días"
    estado == "inactivo"               -> "⚫ Inactivo"
    else                               -> "✅ Activo"
}

private fun inicialesCliente(nombre: String): String {
    val partes = nombre.trim().split(" ")
    return when {
        partes.size >= 2 -> "${partes[0].firstOrNull() ?: ""}${partes[1].firstOrNull() ?: ""}".uppercase()
        partes.isNotEmpty() -> partes[0].take(2).uppercase()
        else -> "??"
    }
}

// ── Pantalla principal ───────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteDetalleScreen(
    clienteId: Long,
    viewModel: ClienteDetalleViewModel,
    viewModelClientes: ClientesViewModel,
    onBack: () -> Unit,
    onEnviarEmail: () -> Unit,
    onConfigurarEnvio: (Long) -> Unit = {}
) {
    val cliente by viewModel.cliente.collectAsStateWithLifecycle()
    val guardadoExitoso by viewModel.guardadoExitoso.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var editando by remember { mutableStateOf(false) }

    // Campos editables
    var nombre by remember { mutableStateOf("") }
    var cuit by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf<String?>(null) }
    var esVentaMuebles by remember { mutableStateOf(false) }
    var estadoCliente by remember { mutableStateOf("activo") }
    var notas by remember { mutableStateOf("") }
    var proximoVencimientoEpoch by remember { mutableStateOf(0L) }
    var expandedCat by remember { mutableStateOf(false) }
    var expandedEstado by remember { mutableStateOf(false) }
    var mostrarDatePicker by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    // ── Estado de ingresos ───────────────────────────────────────────────
    var ingresoAnual by remember { mutableStateOf("") }
    var anioIngreso by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var mesIngreso by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1) }
    var ingresosMensualesJson by remember { mutableStateOf("") }
    // Ingresos mensuales: lista de 12 montos (índice 0 = enero)
    var ingresosMensuales by remember { mutableStateOf(List(12) { 0.0 }) }
    var editandoIngresos by remember { mutableStateOf(false) }
    var expandedAnio by remember { mutableStateOf(false) }

    // ── Estado campos AFIP Argentina ─────────────────────────────────────
    var dni by remember { mutableStateOf("") }
    var actividadAfip by remember { mutableStateOf("") }
    var puntoVenta by remember { mutableStateOf("") }
    var fechaAltaMonotributoEpoch by remember { mutableStateOf(0L) }
    var obraSocial by remember { mutableStateOf("") }
    var domicilioFiscal by remember { mutableStateOf("") }
    var tieneEmpleados by remember { mutableStateOf(false) }
    var cantidadEmpleados by remember { mutableStateOf("") }
    var mostrarDatePickerAlta by remember { mutableStateOf(false) }

    LaunchedEffect(clienteId) { viewModel.cargarCliente(clienteId) }

    LaunchedEffect(cliente) {
        cliente?.let { c ->
            nombre = c.nombre
            cuit = c.cuit
            email = c.email
            telefono = c.telefono
            categoria = c.categoria
            esVentaMuebles = c.esVentaMuebles
            estadoCliente = c.estadoCliente.ifBlank { "activo" }
            notas = c.notas
            proximoVencimientoEpoch = c.proximoVencimientoEpoch
            // Ingresos
            ingresoAnual = if (c.ingresoAnualCliente > 0) formatMontoInput(c.ingresoAnualCliente) else ""
            if (c.anioIngresoCliente > 0) anioIngreso = c.anioIngresoCliente
            if (c.mesIngresoCliente > 0) mesIngreso = c.mesIngresoCliente
            ingresosMensualesJson = c.ingresosMensualesJson
            ingresosMensuales = parseMensuales(c.ingresosMensualesJson)
            // AFIP
            dni = c.dni
            actividadAfip = c.actividadAfip
            puntoVenta = if (c.puntoVenta > 0) c.puntoVenta.toString() else ""
            fechaAltaMonotributoEpoch = c.fechaAltaMonotributoEpoch
            obraSocial = c.obraSocial
            domicilioFiscal = c.domicilioFiscal
            tieneEmpleados = c.tieneEmpleados
            cantidadEmpleados = if (c.cantidadEmpleados > 0) c.cantidadEmpleados.toString() else ""
        }
    }

    LaunchedEffect(guardadoExitoso) {
        if (guardadoExitoso) { editando = false; viewModel.resetMensajes() }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it); viewModel.resetMensajes() }
    }

    val diasVenc = remember(proximoVencimientoEpoch) { diasHastaVencimiento(proximoVencimientoEpoch) }
    val bgEstado  by animateColorAsState(estadoColorBg(estadoCliente, diasVenc), tween(400), label = "bg")
    val txtEstado by animateColorAsState(estadoColorText(estadoCliente, diasVenc), tween(400), label = "txt")

    Scaffold(
        containerColor = BlancoSuave,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            // ── TopBar ───────────────────────────────────────────────────
            TopAppBar(
                title = { Text(if (editando) "Editar Cliente" else "Perfil del Cliente",
                    fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    if (!editando) {
                        IconButton(onClick = { editando = true }) {
                            Icon(Icons.Default.Edit, "Editar", tint = CelesteOsc)
                        }
                        IconButton(onClick = {
                            scope.launch {
                                cliente?.let { c ->
                                    val intent = viewModelClientes.enviarRecordatorio(context, c)
                                    intent?.let { runCatching { context.startActivity(it) }; onEnviarEmail() }
                                }
                            }
                        }) {
                            Icon(Icons.Default.Email, "Enviar email", tint = Amarillo)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blanco, titleContentColor = TextoOscuro
                )
            )

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ── HEADER ───────────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CelesteOsc)
                ) {
                    Column(
                        Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Avatar con iniciales
                        Box(
                            Modifier.size(80.dp).clip(CircleShape).background(Blanco),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                inicialesCliente(cliente?.nombre ?: nombre),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CelesteOsc
                            )
                        }
                        Text(
                            cliente?.nombre ?: nombre.ifBlank { "Nuevo cliente" },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Blanco
                        )
                        if (cuit.isNotBlank()) {
                            Text("CUIT: $cuit",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Blanco.copy(alpha = 0.85f))
                        }
                        // Badge de estado dinámico
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(bgEstado)
                                .padding(horizontal = 14.dp, vertical = 5.dp)
                        ) {
                            Text(estadoLabel(estadoCliente, diasVenc),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = txtEstado)
                        }
                        // Acciones rápidas en el header
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (telefono.isNotBlank()) {
                                FilledTonalIconButton(onClick = {
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telefono")))
                                }) {
                                    Icon(Icons.Default.Phone, "Llamar", tint = CelesteOsc)
                                }
                                FilledTonalIconButton(onClick = {
                                    val num = telefono.filter { it.isDigit() }
                                    context.startActivity(Intent(Intent.ACTION_VIEW,
                                        Uri.parse("https://wa.me/$num")))
                                }) {
                                    Icon(Icons.Default.Chat, "WhatsApp", tint = Verde)
                                }
                            }
                            if (email.isNotBlank()) {
                                FilledTonalIconButton(onClick = {
                                    scope.launch {
                                        cliente?.let { c ->
                                            val intent = viewModelClientes.enviarRecordatorio(context, c)
                                            intent?.let { runCatching { context.startActivity(it) }; onEnviarEmail() }
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Email, "Email", tint = Amarillo)
                                }
                            }
                            FilledTonalIconButton(onClick = {
                                viewModelClientes.aplicarComoActivo(
                                    cliente ?: return@FilledTonalIconButton
                                )
                            }) {
                                Icon(Icons.Default.Check, "Aplicar como activo", tint = Verde)
                            }
                        }
                    }
                }

                // ── ESTADO Y VENCIMIENTO ─────────────────────────────────
                SeccionCard(titulo = "Estado y Próximo Vencimiento", icono = Icons.Default.DateRange) {
                    // Selector de estado
                    ExposedDropdownMenuBox(
                        expanded = expandedEstado && editando,
                        onExpandedChange = { if (editando) expandedEstado = it }
                    ) {
                        OutlinedTextField(
                            value = when (estadoCliente) {
                                "activo"    -> "✅ Activo"
                                "inactivo"  -> "⚫ Inactivo"
                                "por_vencer"-> "⚠️ Por vencer"
                                "vencido"   -> "⛔ Vencido"
                                else        -> estadoCliente
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Estado") },
                            trailingIcon = { if (editando) Icon(Icons.Default.ExpandMore, null) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            enabled = editando,
                            shape = RoundedCornerShape(12.dp),
                            colors = campoColores()
                        )
                        ExposedDropdownMenu(expanded = expandedEstado, onDismissRequest = { expandedEstado = false }) {
                            listOf("activo" to "✅ Activo", "inactivo" to "⚫ Inactivo",
                                "por_vencer" to "⚠️ Por vencer", "vencido" to "⛔ Vencido").forEach { (val_, label) ->
                                DropdownMenuItem(text = { Text(label) }, onClick = {
                                    estadoCliente = val_; expandedEstado = false
                                })
                            }
                        }
                    }
                    // Fecha de próximo vencimiento
                    OutlinedTextField(
                        value = formatFecha(proximoVencimientoEpoch),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Próximo Vencimiento") },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = CelesteOsc) },
                        trailingIcon = {
                            if (editando) {
                                IconButton(onClick = { mostrarDatePicker = true }) {
                                    Icon(Icons.Default.Edit, "Elegir fecha", tint = CelesteOsc)
                                }
                            } else if (proximoVencimientoEpoch != 0L) {
                                IconButton(onClick = { if (editando) proximoVencimientoEpoch = 0L }) {
                                    Icon(Icons.Default.EventAvailable, null, tint = Verde)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = campoColores()
                    )
                    // Si hay vencimiento definido, mostrar chip informativo
                    diasVenc?.let { dias ->
                        Row(
                            Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(bgEstado)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, null, tint = txtEstado, modifier = Modifier.size(16.dp))
                            Text(
                                when {
                                    dias < 0  -> "Vencido hace ${-dias} días — contactar al cliente"
                                    dias == 0L -> "Vence HOY"
                                    dias <= 5  -> "Quedan $dias días para el vencimiento"
                                    else       -> "Vence el ${formatFecha(proximoVencimientoEpoch)}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = txtEstado
                            )
                        }
                    }
                }

                // ── DATOS FISCALES ───────────────────────────────────────
                SeccionCard(titulo = "Datos Fiscales", icono = Icons.Default.AccountBalance) {
                    CampoEditable("Nombre / Razón Social", nombre, Icons.Default.Person, editando,
                        onValueChange = { nombre = it })
                    CampoEditable("CUIT", cuit, Icons.Default.Badge, editando,
                        keyboardType = KeyboardType.Number,
                        onValueChange = { cuit = it.filter { c -> c.isDigit() || c == '-' }.take(13) })

                    // Categoría dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedCat && editando,
                        onExpandedChange = { if (editando) expandedCat = it }
                    ) {
                        OutlinedTextField(
                            value = categoria?.let { "Categoría $it" } ?: "Automática (según facturación)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría Monotributo") },
                            leadingIcon = { Icon(Icons.Default.Star, null, tint = if (categoria != null) Amarillo else CelesteOsc) },
                            trailingIcon = { if (editando) Icon(Icons.Default.ExpandMore, null) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            enabled = editando,
                            shape = RoundedCornerShape(12.dp),
                            colors = campoColores()
                        )
                        if (editando) {
                            ExposedDropdownMenu(expanded = expandedCat, onDismissRequest = { expandedCat = false }) {
                                DropdownMenuItem(text = { Text("Automática (según facturación)") },
                                    onClick = { categoria = null; expandedCat = false })
                                HorizontalDivider()
                                CategoriaMonotributo.entries.forEach { c ->
                                    DropdownMenuItem(text = { Text("Categoría ${c.letra}") },
                                        onClick = { categoria = c.letra; expandedCat = false })
                                }
                            }
                        }
                    }
                    // Chips de categoría y rubro (modo solo vista)
                    if (!editando && categoria != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ChipInfo("Cat. ${categoria}", Amarillo, TextoOscuro)
                            if (esVentaMuebles) ChipInfo("Muebles", CelesteOsc.copy(0.15f), CelesteOsc)
                        }
                    }
                    // Checkbox muebles
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = esVentaMuebles,
                            onCheckedChange = { if (editando) esVentaMuebles = it },
                            enabled = editando,
                            colors = CheckboxDefaults.colors(checkedColor = CelesteOsc)
                        )
                        Column {
                            Text("Venta de cosas muebles", color = TextoOscuro, fontWeight = FontWeight.SemiBold)
                            Text("Modifica cuotas desde categoría C",
                                style = MaterialTheme.typography.bodySmall, color = TextoSuave)
                        }
                    }
                }

                // ── CONTACTO ─────────────────────────────────────────────
                SeccionCard(titulo = "Contacto y Domicilio", icono = Icons.Default.ContactPhone) {
                    CampoEditable("Email para recordatorios", email, Icons.Default.Email, editando,
                        keyboardType = KeyboardType.Email, onValueChange = { email = it })
                    CampoEditable("Teléfono", telefono, Icons.Default.Phone, editando,
                        keyboardType = KeyboardType.Phone, onValueChange = { telefono = it })
                    CampoEditable("Domicilio Fiscal", domicilioFiscal, Icons.Default.Home, editando,
                        onValueChange = { domicilioFiscal = it })

                    // Acciones de contacto rápido (modo vista)
                    if (!editando && (email.isNotBlank() || telefono.isNotBlank())) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            if (telefono.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telefono"))) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, CelesteOsc)
                                ) {
                                    Icon(Icons.Default.Phone, null, tint = CelesteOsc, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Llamar", color = CelesteOsc, fontSize = 13.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        val num = telefono.filter { it.isDigit() }
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$num")))
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Verde)
                                ) {
                                    Icon(Icons.Default.Chat, null, tint = Verde, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("WhatsApp", color = Verde, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // Botón de configuración avanzada de envío
                    if (!editando) {
                        OutlinedButton(
                            onClick = { onConfigurarEnvio(clienteId) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CelesteOsc)
                        ) {
                            Icon(Icons.Default.Settings, null, tint = CelesteOsc, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Configurar mensaje, horario y WhatsApp",
                                color = CelesteOsc, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }

                // ── DATOS AFIP / ARGENTINA ───────────────────────────────
                SeccionCard(titulo = "Datos AFIP / Monotributo", icono = Icons.Default.AccountBox) {
                    CampoEditable("DNI", dni, Icons.Default.Badge, editando,
                        keyboardType = KeyboardType.Number,
                        onValueChange = { dni = it.filter { c -> c.isDigit() }.take(8) })
                    CampoEditable("Actividad AFIP", actividadAfip, Icons.Default.Work, editando,
                        onValueChange = { actividadAfip = it })
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = puntoVenta,
                            onValueChange = { if (editando) puntoVenta = it.filter { c -> c.isDigit() }.take(5) },
                            label = { Text("Punto de venta") },
                            leadingIcon = { Icon(Icons.Default.Receipt, null, tint = CelesteOsc) },
                            modifier = Modifier.weight(1f),
                            enabled = editando,
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = campoColores()
                        )
                        OutlinedTextField(
                            value = formatFecha(fechaAltaMonotributoEpoch),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Alta Monotributo") },
                            leadingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = CelesteOsc) },
                            trailingIcon = {
                                if (editando) IconButton(onClick = { mostrarDatePickerAlta = true }) {
                                    Icon(Icons.Default.Edit, null, tint = CelesteOsc)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = campoColores()
                        )
                    }
                    CampoEditable("Obra Social", obraSocial, Icons.Default.LocalHospital, editando,
                        onValueChange = { obraSocial = it })
                    // Empleados
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = tieneEmpleados,
                            onCheckedChange = { if (editando) tieneEmpleados = it },
                            enabled = editando,
                            colors = CheckboxDefaults.colors(checkedColor = CelesteOsc)
                        )
                        Column(Modifier.weight(1f)) {
                            Text("Tiene empleados", color = TextoOscuro, fontWeight = FontWeight.SemiBold)
                            Text("Afecta los aportes mensuales", style = MaterialTheme.typography.bodySmall, color = TextoSuave)
                        }
                        if (tieneEmpleados) {
                            OutlinedTextField(
                                value = cantidadEmpleados,
                                onValueChange = { if (editando) cantidadEmpleados = it.filter { c -> c.isDigit() }.take(2) },
                                label = { Text("Cant.") },
                                modifier = Modifier.width(80.dp),
                                enabled = editando,
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = campoColores()
                            )
                        }
                    }
                    // Chips resumen modo vista
                    if (!editando) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            if (puntoVenta.isNotBlank()) ChipInfo("PV $puntoVenta", GrisCeleste, TextoOscuro)
                            if (obraSocial.isNotBlank()) ChipInfo("🏥 $obraSocial", VerdeClaro, Verde)
                            if (tieneEmpleados) ChipInfo("👷 ${cantidadEmpleados.ifBlank{"?"}} empl.", AmarilloClaro, NaranjaVenc)
                        }
                    }
                }

                // ── RECATEGORIZACIÓN SEMESTRAL ───────────────────────────
                CardRecategorizacion(
                    ingresoAnual = ingresoAnual.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0,
                    mesIngreso = mesIngreso,
                    categoriaActual = categoria,
                    esVentaMuebles = esVentaMuebles
                )

                // ── NOTAS DEL CONTADOR ───────────────────────────────────
                SeccionCard(titulo = "Notas del Contador", icono = Icons.AutoMirrored.Filled.Notes) {
                    OutlinedTextField(
                        value = notas,
                        onValueChange = { if (editando) notas = it },
                        label = { Text("Notas privadas") },
                        placeholder = { Text("Ej: cliente puntual, prefiere WhatsApp, vence el 20...") },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                        enabled = editando,
                        maxLines = 6,
                        shape = RoundedCornerShape(12.dp),
                        colors = campoColores()
                    )
                    if (notas.isNotBlank() && !editando) {
                        Row(
                            Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(GrisCeleste.copy(alpha = 0.5f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Notes, null, tint = TextoSuave, modifier = Modifier.size(16.dp))
                            Text(notas, style = MaterialTheme.typography.bodySmall, color = TextoOscuro)
                        }
                    }
                }

                // ── INGRESOS Y CATEGORÍA ─────────────────────────────────
                CardIngresosCategoria(
                    ingresoAnual = ingresoAnual,
                    anioIngreso = anioIngreso,
                    mesIngreso = mesIngreso,
                    ingresosMensuales = ingresosMensuales,
                    esVentaMuebles = esVentaMuebles,
                    editando = editandoIngresos,
                    expandedAnio = expandedAnio,
                    onExpandedAnio = { expandedAnio = it },
                    onIngresoChange = { ingresoAnual = it },
                    onAnioChange = { anioIngreso = it },
                    onMesChange = { mesIngreso = it },
                    onMensualChange = { idx, val_ ->
                        ingresosMensuales = ingresosMensuales.toMutableList().also { it[idx] = val_ }
                        ingresoAnual = ingresosMensuales.sum().let { if (it > 0) formatMontoInput(it) else "" }
                    },
                    onEditar = { editandoIngresos = true },
                    onGuardar = {
                        val monto = ingresoAnual.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
                        val json = buildMensualesJson(ingresosMensuales)
                        viewModel.actualizarIngresos(monto, anioIngreso, mesIngreso, json)
                        ingresosMensualesJson = json
                        editandoIngresos = false
                    },
                    onCancelar = {
                        ingresoAnual = if ((cliente?.ingresoAnualCliente ?: 0.0) > 0)
                            formatMontoInput(cliente!!.ingresoAnualCliente) else ""
                        ingresosMensuales = parseMensuales(ingresosMensualesJson)
                        editandoIngresos = false
                    }
                )

                // ── BOTONES DE ACCIÓN ────────────────────────────────────
                AnimatedVisibility(visible = editando) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = {
                                cliente?.let { c ->
                                    nombre = c.nombre; cuit = c.cuit; email = c.email
                                    telefono = c.telefono; categoria = c.categoria
                                    esVentaMuebles = c.esVentaMuebles
                                    estadoCliente = c.estadoCliente.ifBlank { "activo" }
                                    notas = c.notas
                                    proximoVencimientoEpoch = c.proximoVencimientoEpoch
                                    // AFIP
                                    dni = c.dni; actividadAfip = c.actividadAfip
                                    puntoVenta = if (c.puntoVenta > 0) c.puntoVenta.toString() else ""
                                    fechaAltaMonotributoEpoch = c.fechaAltaMonotributoEpoch
                                    obraSocial = c.obraSocial; domicilioFiscal = c.domicilioFiscal
                                    tieneEmpleados = c.tieneEmpleados
                                    cantidadEmpleados = if (c.cantidadEmpleados > 0) c.cantidadEmpleados.toString() else ""
                                }
                                editando = false
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, GrisCeleste)
                        ) { Text("Cancelar", color = TextoSuave) }

                        Button(
                            onClick = {
                                viewModel.actualizar(
                                    nombre, cuit, email, telefono, categoria, esVentaMuebles,
                                    estadoCliente, notas, proximoVencimientoEpoch,
                                    dni = dni,
                                    actividadAfip = actividadAfip,
                                    puntoVenta = puntoVenta.toIntOrNull() ?: 0,
                                    fechaAltaMonotributoEpoch = fechaAltaMonotributoEpoch,
                                    obraSocial = obraSocial,
                                    domicilioFiscal = domicilioFiscal,
                                    tieneEmpleados = tieneEmpleados,
                                    cantidadEmpleados = cantidadEmpleados.toIntOrNull() ?: 0
                                )
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CelesteOsc)
                        ) {
                            Icon(Icons.Default.Check, null, tint = Blanco, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Guardar Cambios", color = Blanco, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                AnimatedVisibility(visible = !editando) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { viewModelClientes.aplicarComoActivo(cliente ?: return@Button) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CelesteOsc)
                        ) {
                            Icon(Icons.Default.Check, null, tint = Blanco, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Aplicar como cliente activo", color = Blanco, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { mostrarDialogoEliminar = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Rojo)
                        ) {
                            Icon(Icons.Default.Delete, null, tint = Rojo, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Eliminar Cliente", color = Rojo, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // ── DatePicker para fecha de alta Monotributo ────────────────────────────
    if (mostrarDatePickerAlta) {
        val dpState = rememberDatePickerState(
            initialSelectedDateMillis = if (fechaAltaMonotributoEpoch != 0L) fechaAltaMonotributoEpoch else null
        )
        DatePickerDialog(
            onDismissRequest = { mostrarDatePickerAlta = false },
            confirmButton = {
                TextButton(onClick = {
                    fechaAltaMonotributoEpoch = dpState.selectedDateMillis ?: 0L
                    mostrarDatePickerAlta = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { mostrarDatePickerAlta = false }) { Text("Cancelar") } }
        ) { DatePicker(state = dpState) }
    }

    // ── DatePicker ───────────────────────────────────────────────────────────
    if (mostrarDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (proximoVencimientoEpoch != 0L) proximoVencimientoEpoch else null
        )
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    proximoVencimientoEpoch = datePickerState.selectedDateMillis ?: 0L
                    mostrarDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = datePickerState) }
    }

    // ── Diálogo eliminar ─────────────────────────────────────────────────────
    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("¿Eliminar cliente?", fontWeight = FontWeight.Bold) },
            text = { Text("Esta acción no se puede deshacer. Se eliminará \"${cliente?.nombre}\" de tu lista.") },
            confirmButton = {
                Button(onClick = { viewModel.eliminar { onBack() } },
                    colors = ButtonDefaults.buttonColors(containerColor = Rojo)) {
                    Text("Eliminar", color = Blanco)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) { Text("Cancelar") }
            }
        )
    }
}

// ── Componentes reutilizables ─────────────────────────────────────────────────
@Composable
private fun SeccionCard(
    titulo: String,
    icono: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        border = BorderStroke(1.dp, GrisCeleste)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icono, null, tint = CelesteOsc, modifier = Modifier.size(20.dp))
                Text(titulo, fontWeight = FontWeight.Bold, color = TextoOscuro,
                    style = MaterialTheme.typography.titleSmall)
            }
            HorizontalDivider(color = GrisCeleste)
            content()
        }
    }
}

@Composable
private fun CampoEditable(
    label: String,
    value: String,
    icono: ImageVector,
    editando: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icono, null, tint = CelesteOsc) },
        modifier = Modifier.fillMaxWidth(),
        enabled = editando,
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = campoColores()
    )
}

@Composable
private fun campoColores() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CelesteOsc,
    focusedLabelColor = CelesteOsc,
    cursorColor = CelesteOsc,
    disabledBorderColor = GrisCeleste,
    disabledTextColor = TextoOscuro,
    disabledLabelColor = TextoSuave,
    disabledLeadingIconColor = CelesteOsc.copy(alpha = 0.6f)
)

@Composable
private fun ChipInfo(texto: String, bg: Color, textColor: Color) {
    Box(
        Modifier.clip(RoundedCornerShape(8.dp)).background(bg).padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(texto, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

// ── Helpers de ingresos ───────────────────────────────────────────────────────

private fun formatMontoInput(d: Double): String =
    NumberFormat.getNumberInstance(Locale("es", "AR")).apply {
        maximumFractionDigits = 2; minimumFractionDigits = 0
    }.format(d)

private fun formatMonto(d: Double): String =
    "$ " + NumberFormat.getNumberInstance(Locale("es", "AR")).apply {
        maximumFractionDigits = 2; minimumFractionDigits = 0
    }.format(d)

private fun parseMensuales(json: String): List<Double> {
    if (json.isBlank()) return List(12) { 0.0 }
    val map = json.split(",").mapNotNull { entry ->
        val parts = entry.split(":")
        if (parts.size == 2) parts[0].toIntOrNull()?.let { it to (parts[1].toDoubleOrNull() ?: 0.0) }
        else null
    }.toMap()
    return (1..12).map { map[it] ?: 0.0 }
}

private fun buildMensualesJson(mensuales: List<Double>): String =
    mensuales.mapIndexed { idx, v -> "${idx + 1}:$v" }
        .filter { !it.endsWith(":0.0") }
        .joinToString(",")

private val MESES = listOf("Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic")

// ── Card de Ingresos y Categoría ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardIngresosCategoria(
    ingresoAnual: String,
    anioIngreso: Int,
    mesIngreso: Int,
    ingresosMensuales: List<Double>,
    esVentaMuebles: Boolean,
    editando: Boolean,
    expandedAnio: Boolean,
    onExpandedAnio: (Boolean) -> Unit,
    onIngresoChange: (String) -> Unit,
    onAnioChange: (Int) -> Unit,
    onMesChange: (Int) -> Unit,
    onMensualChange: (Int, Double) -> Unit,
    onEditar: () -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    val montoAnual = ingresoAnual.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
    val categoriaActual = if (montoAnual > 0) CategoriaMonotributo.porLimiteAnual(montoAnual) else null
    val limiteActual = categoriaActual?.limiteAnual ?: 0.0
    val progreso = if (limiteActual > 0) (montoAnual / limiteActual).coerceIn(0.0, 1.0).toFloat() else 0f
    val categoriaProxima = if (categoriaActual != null) {
        CategoriaMonotributo.entries.getOrNull(CategoriaMonotributo.entries.indexOf(categoriaActual) + 1)
    } else null
    val margenHastaSiguiente = if (categoriaActual != null) limiteActual - montoAnual else 0.0

    val anioActual = Calendar.getInstance().get(Calendar.YEAR)
    val anios = (anioActual downTo anioActual - 3).toList()

    // Colores según % del límite
    val (colorBarra, colorBg) = when {
        progreso >= 0.95f -> Rojo to RojoClaro
        progreso >= 0.80f -> NaranjaVenc to AmarilloClaro
        progreso >= 0.60f -> Amarillo to AmarilloClaro
        else -> Verde to VerdeClaro
    }
    val barraAnim by animateFloatAsState(progreso, tween(800), label = "barra")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        border = BorderStroke(1.dp, if (progreso >= 0.80f) colorBarra.copy(alpha = 0.5f) else GrisCeleste)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // Encabezado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = CelesteOsc,
                        modifier = Modifier.size(20.dp))
                    Text("Ingresos y Categoría", fontWeight = FontWeight.Bold,
                        color = TextoOscuro, style = MaterialTheme.typography.titleSmall)
                }
                if (!editando) {
                    TextButton(onClick = onEditar) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Editar", fontSize = 12.sp)
                    }
                }
            }
            HorizontalDivider(color = GrisCeleste)

            if (montoAnual > 0 && !editando) {
                // ── Vista resumen ────────────────────────────────────────
                // Categoría calculada
                categoriaActual?.let { cat ->
                    Row(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colorBg)
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Categoría calculada", style = MaterialTheme.typography.labelSmall, color = TextoSuave)
                            Text("Categoría ${cat.letra}", fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp, color = colorBarra)
                            Text("Límite anual: ${formatMonto(limiteActual)}",
                                style = MaterialTheme.typography.bodySmall, color = TextoSuave)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Ingresado", style = MaterialTheme.typography.labelSmall, color = TextoSuave)
                            Text(formatMonto(montoAnual), fontWeight = FontWeight.Bold,
                                color = colorBarra, fontSize = 16.sp)
                            Text("Año $anioIngreso ${if (mesIngreso > 0) "· hasta ${MESES[mesIngreso-1]}" else ""}",
                                style = MaterialTheme.typography.labelSmall, color = TextoSuave)
                        }
                    }
                }

                // Barra de progreso
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Uso del límite", style = MaterialTheme.typography.labelSmall, color = TextoSuave)
                        Text("${(progreso * 100).toInt()}%", style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold, color = colorBarra)
                    }
                    LinearProgressIndicator(
                        progress = { barraAnim },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                        color = colorBarra,
                        trackColor = colorBg,
                        strokeCap = StrokeCap.Round
                    )
                }

                // Alerta de proximidad al límite
                when {
                    progreso >= 0.95f -> AlertaIngreso(
                        "⛔ SUPERÓ el límite de categoría ${categoriaActual?.letra}",
                        "Debe recategorizarse a ${categoriaProxima?.letra ?: "K"}. Cuota nueva: ${categoriaProxima?.let { formatMonto(it.cuotaSegunRubro(esVentaMuebles)) } ?: "-"}",
                        Rojo, RojoClaro
                    )
                    progreso >= 0.90f -> AlertaIngreso(
                        "⚠️ Muy cerca del límite — faltan ${formatMonto(margenHastaSiguiente)}",
                        "Aviso al cliente para evitar sorpresas. Próxima cat: ${categoriaProxima?.letra ?: "K"}",
                        NaranjaVenc, AmarilloClaro
                    )
                    progreso >= 0.75f -> AlertaIngreso(
                        "💡 Llevan el ${(progreso*100).toInt()}% del límite — faltan ${formatMonto(margenHastaSiguiente)}",
                        "Buen momento para proyectar si llegan al fin de año sin recategorizarse.",
                        Amarillo, AmarilloClaro
                    )
                    else -> {}
                }

                // Cuotas actuales
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    categoriaActual?.let { cat ->
                        CuotaChip("Cuota actual", formatMonto(cat.cuotaSegunRubro(esVentaMuebles)), CelesteOsc)
                    }
                    categoriaProxima?.let { prox ->
                        CuotaChip("Cuota siguiente (${prox.letra})", formatMonto(prox.cuotaSegunRubro(esVentaMuebles)), NaranjaVenc)
                    }
                }

                // Desglose mensual si hay datos
                if (ingresosMensuales.any { it > 0 }) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Desglose mensual", style = MaterialTheme.typography.labelSmall,
                            color = TextoSuave, fontWeight = FontWeight.Bold)
                        ingresosMensuales.chunked(3).forEachIndexed { rowIdx, grupo ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                grupo.forEachIndexed { colIdx, monto ->
                                    val mesIdx = rowIdx * 3 + colIdx
                                    Box(
                                        Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                            .background(if (monto > 0) CelesteOsc.copy(0.08f) else GrisCeleste.copy(0.3f))
                                            .padding(6.dp)
                                    ) {
                                        Column {
                                            Text(MESES[mesIdx], fontSize = 9.sp, color = TextoSuave)
                                            Text(if (monto > 0) formatMonto(monto) else "-",
                                                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                                color = if (monto > 0) TextoOscuro else TextoSuave)
                                        }
                                    }
                                }
                                // Rellenar si el grupo tiene menos de 3 elementos
                                repeat(3 - grupo.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }

            } else if (!editando) {
                // Estado vacío
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .background(GrisCeleste.copy(0.3f)).padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📊", fontSize = 24.sp)
                    Column {
                        Text("Sin datos de ingresos", fontWeight = FontWeight.Bold, color = TextoOscuro)
                        Text("Agregá los ingresos anuales del cliente para ver en qué categoría está y si está cerca de subir.",
                            style = MaterialTheme.typography.bodySmall, color = TextoSuave)
                    }
                }
            }

            // ── Modo edición ─────────────────────────────────────────────
            AnimatedVisibility(visible = editando) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // Año e ingreso total
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Selector año
                        ExposedDropdownMenuBox(
                            expanded = expandedAnio,
                            onExpandedChange = onExpandedAnio,
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = anioIngreso.toString(),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Año") },
                                trailingIcon = { Icon(Icons.Default.ExpandMore, null) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = campoColores()
                            )
                            ExposedDropdownMenu(expanded = expandedAnio, onDismissRequest = { onExpandedAnio(false) }) {
                                anios.forEach { a ->
                                    DropdownMenuItem(text = { Text(a.toString()) }, onClick = {
                                        onAnioChange(a); onExpandedAnio(false)
                                    })
                                }
                            }
                        }
                        // Ingreso total
                        OutlinedTextField(
                            value = ingresoAnual,
                            onValueChange = onIngresoChange,
                            label = { Text("Ingreso anual $") },
                            modifier = Modifier.weight(2f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = campoColores()
                        )
                    }

                    // Selector de mes hasta el que acumuló
                    Text("Mes hasta el que se acumuló:", style = MaterialTheme.typography.labelSmall, color = TextoSuave)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        MESES.forEachIndexed { idx, mes ->
                            val seleccionado = idx + 1 == mesIngreso
                            Box(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (seleccionado) CelesteOsc else GrisCeleste.copy(0.4f))
                                    .padding(vertical = 4.dp)
                                    .clickable { onMesChange(idx + 1) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(mes, fontSize = 8.sp, fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                                    color = if (seleccionado) Blanco else TextoSuave)
                            }
                        }
                    }

                    // Desglose mensual opcional
                    Text("Ingreso por mes (opcional):", style = MaterialTheme.typography.labelSmall, color = TextoSuave)
                    ingresosMensuales.chunked(3).forEachIndexed { rowIdx, grupo ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            grupo.forEachIndexed { colIdx, monto ->
                                val mesIdx = rowIdx * 3 + colIdx
                                OutlinedTextField(
                                    value = if (monto > 0) monto.toLong().toString() else "",
                                    onValueChange = { v -> onMensualChange(mesIdx, v.toLongOrNull()?.toDouble() ?: 0.0) },
                                    label = { Text(MESES[mesIdx], fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = campoColores(),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                                )
                            }
                            repeat(3 - grupo.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }

                    // Botones guardar/cancelar ingresos
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onCancelar, modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, GrisCeleste)) {
                            Text("Cancelar", color = TextoSuave)
                        }
                        Button(onClick = onGuardar, modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CelesteOsc)) {
                            Icon(Icons.Default.Check, null, tint = Blanco, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Guardar", color = Blanco, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertaIngreso(titulo: String, subtitulo: String, color: Color, bg: Color) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(bg).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(titulo, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
            Text(subtitulo, style = MaterialTheme.typography.labelSmall, color = TextoOscuro)
        }
    }
}

@Composable
private fun CuotaChip(label: String, monto: String, color: Color) {
    Column(
        Modifier.clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.1f)).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextoSuave)
        Text(monto, fontWeight = FontWeight.Bold, color = color, fontSize = 13.sp)
    }
}

// ── Card Recategorización Semestral Argentina ─────────────────────────────────

@Composable
private fun CardRecategorizacion(
    ingresoAnual: Double,
    mesIngreso: Int,
    categoriaActual: String?,
    esVentaMuebles: Boolean
) {
    val hoy = remember { Calendar.getInstance() }
    val mesActual = hoy.get(Calendar.MONTH) + 1   // 1-12
    val anioActual = hoy.get(Calendar.YEAR)

    // Períodos de recategorización en Argentina: enero y julio
    // Período ene-jun → recategoriza en JULIO (usando ingresos jul-jun del año anterior)
    // Período jul-dic → recategoriza en ENERO (usando ingresos jul-jun)
    val (periodoNombre, mesRecateg, labelRecateg) = when {
        mesActual in 1..6  -> Triple("Enero–Junio",  7, "Julio $anioActual")
        else               -> Triple("Julio–Diciembre", 1, "Enero ${anioActual + 1}")
    }

    // Días hasta la próxima recategorización
    val fechaRecateg = Calendar.getInstance().apply {
        set(Calendar.MONTH, mesRecateg - 1)
        set(Calendar.DAY_OF_MONTH, 1)
        if (mesRecateg <= mesActual) add(Calendar.YEAR, 1)
    }
    val diasHasta = ((fechaRecateg.timeInMillis - hoy.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

    // Calcular si debe subir, bajar o mantener
    val catActualEnum = categoriaActual?.let { letra ->
        CategoriaMonotributo.entries.firstOrNull { it.letra == letra }
    }
    val catCalculada = if (ingresoAnual > 0) CategoriaMonotributo.porLimiteAnual(ingresoAnual) else null

    val (accion, accionColor, accionBg, accionEmoji) = when {
        catCalculada == null       -> Quadruple("Sin datos de ingresos para calcular", TextoSuave, GrisCeleste.copy(0.3f), "📊")
        catActualEnum == null      -> Quadruple("Categoría actual no definida", TextoSuave, GrisCeleste.copy(0.3f), "❓")
        catCalculada > catActualEnum -> Quadruple(
            "⬆️ DEBE SUBIR a categoría ${catCalculada.letra}",
            Rojo, RojoClaro, "⬆️"
        )
        catCalculada < catActualEnum -> Quadruple(
            "⬇️ Puede BAJAR a categoría ${catCalculada.letra}",
            Verde, VerdeClaro, "⬇️"
        )
        else -> Quadruple("✅ Mantiene categoría ${catActualEnum.letra}", Verde, VerdeClaro, "✅")
    }

    val urgenciaColor = when {
        diasHasta <= 15 -> Rojo
        diasHasta <= 45 -> NaranjaVenc
        else -> CelesteOsc
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        border = BorderStroke(1.dp, if (diasHasta <= 15) Rojo.copy(0.4f) else GrisCeleste)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Autorenew, null, tint = CelesteOsc, modifier = Modifier.size(20.dp))
                Text("Recategorización Semestral", fontWeight = FontWeight.Bold,
                    color = TextoOscuro, style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f))
                // Badge días restantes
                Box(
                    Modifier.clip(RoundedCornerShape(12.dp))
                        .background(urgenciaColor.copy(0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("$diasHasta días", fontSize = 12.sp,
                        fontWeight = FontWeight.Bold, color = urgenciaColor)
                }
            }
            HorizontalDivider(color = GrisCeleste)

            // Info del período
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoChipRecateg("Período actual", periodoNombre, CelesteOsc)
                InfoChipRecateg("Próxima recateg.", labelRecateg, urgenciaColor)
            }

            // Resultado del cálculo
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(accionBg).padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(accionEmoji, fontSize = 28.sp)
                Column(Modifier.weight(1f)) {
                    Text(accion, fontWeight = FontWeight.Bold, color = accionColor,
                        style = MaterialTheme.typography.bodyMedium)
                    if (catCalculada != null && catActualEnum != null) {
                        val cuotaNueva = catCalculada.cuotaSegunRubro(esVentaMuebles)
                        val cuotaActual = catActualEnum.cuotaSegunRubro(esVentaMuebles)
                        val diff = cuotaNueva - cuotaActual
                        if (diff != 0.0) {
                            Text(
                                "${if (diff > 0) "+" else ""}${formatMonto(diff)} en cuota mensual",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (diff > 0) Rojo else Verde,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Cuotas comparativas
            if (catActualEnum != null && catCalculada != null && catCalculada != catActualEnum) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CuotaChip("Cuota actual (${catActualEnum.letra})",
                        formatMonto(catActualEnum.cuotaSegunRubro(esVentaMuebles)), CelesteOsc)
                    CuotaChip("Cuota nueva (${catCalculada.letra})",
                        formatMonto(catCalculada.cuotaSegunRubro(esVentaMuebles)),
                        if (catCalculada > catActualEnum) Rojo else Verde)
                }
            }

            // Alerta de urgencia
            if (diasHasta <= 15) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .background(RojoClaro).padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, null, tint = Rojo, modifier = Modifier.size(18.dp))
                    Text("¡Quedan solo $diasHasta días para recategorizar en ARCA/AFIP!",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold, color = Rojo)
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun InfoChipRecateg(label: String, valor: String, color: Color) {
    Column(
        Modifier.clip(RoundedCornerShape(10.dp))
            .background(color.copy(0.08f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextoSuave)
        Text(valor, fontWeight = FontWeight.Bold, color = color,
            style = MaterialTheme.typography.bodySmall)
    }
}

