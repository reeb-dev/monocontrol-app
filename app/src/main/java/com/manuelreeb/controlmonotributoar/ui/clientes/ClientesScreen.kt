package com.reeb.controlmonotributoar.ui.clientes

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reeb.controlmonotributoar.data.local.entity.ClienteEntity
import com.reeb.controlmonotributoar.domain.model.CategoriaMonotributo
import com.reeb.controlmonotributoar.utils.ScreenTourPrefs
import com.reeb.controlmonotributoar.ui.theme.AppRadius
import com.reeb.controlmonotributoar.ui.components.shimmerLoading
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.Calendar

// ── Paleta local ─────────────────────────────────────────────────────────────
private val P       = Color(0xFF1A5FA8)  // primary
private val PAcc    = Color(0xFF4A90D9)  // primary light
private val Amb     = Color(0xFFF9A825)  // amber/sol
private val Blanco  = Color(0xFFFFFFFF)
private val Fondo   = Color(0xFFF5F8FE)
private val Surface = Color(0xFFEFF6FF)
private val Borde   = Color(0xFFBDD7F5)
private val TxOsc   = Color(0xFF0D2A4A)
private val TxSuave = Color(0xFF6B8099)
private val Verde   = Color(0xFF2E7D32)
private val VClaro  = Color(0xFFE8F5E9)
private val Rojo    = Color(0xFFE53935)
private val RClaro  = Color(0xFFFFEBEE)
private val Naranja = Color(0xFFE65100)
private val NClaro  = Color(0xFFFFF3E0)
private val GrisC   = Color(0xFFECF0F5)

private fun diasVenc(epoch: Long): Long? {
    if (epoch == 0L) return null
    val hoy = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    return (epoch - hoy) / (1000L * 60 * 60 * 24)
}

private fun iniciales(nombre: String): String {
    val p = nombre.trim().split(" ")
    return when {
        p.size >= 2 -> "${p[0].firstOrNull() ?: ""}${p[1].firstOrNull() ?: ""}".uppercase()
        p.isNotEmpty() -> p[0].take(2).uppercase()
        else -> "??"
    }
}

// ── Pantalla principal ────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientesScreen(
    viewModel: ClientesViewModel,
    @Suppress("UNUSED_PARAMETER") onBack: () -> Unit,
    onVerDetalle: (Long) -> Unit,
    onAbrirPanel: () -> Unit,
    initialFiltro: String = "todos"
) {
    val query    by viewModel.query.collectAsStateWithLifecycle()
    val filtro   by viewModel.filtro.collectAsStateWithLifecycle()
    val clientes by viewModel.clientes.collectAsStateWithLifecycle()
    val stats    by viewModel.stats.collectAsStateWithLifecycle()

    var mostrarDialogoNuevo by remember { mutableStateOf(false) }
    var mostrarMasivo by remember { mutableStateOf(false) }
    var mostrarTipBusqueda by rememberSaveable { mutableStateOf(true) }
    var mostrarTourClientes by rememberSaveable { mutableStateOf(false) }
    var mostrarSkeleton by rememberSaveable { mutableStateOf(true) }
    var modoPro by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(initialFiltro) {
        if (initialFiltro.isNotBlank() && initialFiltro != "todos") {
            viewModel.onFiltroChange(initialFiltro)
        }
    }
    LaunchedEffect(Unit) {
        mostrarTourClientes = !ScreenTourPrefs.wasSeenClientes(context)
        delay(900)
        mostrarSkeleton = false
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var snack by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(snack) {
        snack?.let { snackbarHostState.showSnackbar(it); snack = null }
    }

    Scaffold(
        containerColor = Fondo,
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // FAB secundario: recordatorio masivo
                if (stats.total > 0) {
                    SmallFloatingActionButton(
                        onClick = { mostrarMasivo = true },
                        containerColor = Amb,
                        contentColor = TxOsc
                    ) { Icon(Icons.Default.Send, "Masivo") }
                }
                ExtendedFloatingActionButton(
                    onClick = { mostrarDialogoNuevo = true },
                    containerColor = P,
                    contentColor = Blanco,
                    icon = { Icon(Icons.Default.PersonAdd, null) },
                    text = { Text("Nuevo cliente", fontWeight = FontWeight.Bold) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Panel de control ─────────────────────────────────────────
            item {
                PanelControl(
                    stats = stats,
                    filtroActual = filtro,
                    onFiltro = viewModel::onFiltroChange,
                    onAbrirPanel = onAbrirPanel
                )
            }
            if (stats.total > 0) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = modoPro,
                            onClick = { modoPro = !modoPro },
                            label = { Text(if (modoPro) "Modo Pro activado" else "Modo Pro") }
                        )
                    }
                }
            }

            // ── Buscador ─────────────────────────────────────────────────
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = { Text("Buscar por nombre o CUIT…") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = P) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onQueryChange("") }) {
                                Icon(Icons.Default.Close, null, tint = TxSuave)
                            }
                        }
                    },
                    singleLine = true,
                    shape = AppRadius.lg,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = P,
                        focusedLabelColor = P,
                        cursorColor = P,
                        unfocusedBorderColor = Borde
                    )
                )
            }
            if (query.isBlank() && mostrarTipBusqueda) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Blanco),
                        shape = AppRadius.md,
                        border = BorderStroke(1.dp, Borde)
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Tip: buscá por CUIT o nombre para encontrar clientes rápido.", color = TxSuave, style = MaterialTheme.typography.bodySmall)
                            TextButton(onClick = { mostrarTipBusqueda = false }) { Text("Ocultar") }
                        }
                    }
                }
            }

            // ── Lista ─────────────────────────────────────────────────────
            if (clientes.isEmpty() && mostrarSkeleton) {
                items(3) {
                    SkeletonClienteCard()
                }
            } else if (clientes.isEmpty()) {
                item {
                    EstadoVacio(
                        filtroActual = filtro,
                        hayBusqueda = query.isNotBlank(),
                        onCrear = { mostrarDialogoNuevo = true },
                        onLimpiarFiltro = { viewModel.onFiltroChange("todos") },
                        onLimpiarBusqueda = { viewModel.onQueryChange("") }
                    )
                }
            } else {
                items(clientes, key = { it.id }) { cli ->
                    ClienteCard(
                        cliente = cli,
                        viewModel = viewModel,
                        compactMode = modoPro,
                        onVerDetalle = { onVerDetalle(cli.id) },
                        onAplicar = { viewModel.aplicarComoActivo(cli); snack = "✓ ${cli.nombre} cargado como activo" },
                        onEliminar = { viewModel.eliminar(cli); snack = "Cliente eliminado" },
                        onEmailEnviado = { snack = "✉️ Email abierto" }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (mostrarDialogoNuevo) {
        DialogoNuevoCliente(
            onDismiss = { mostrarDialogoNuevo = false },
            onGuardar = { nombre, cuit, email, telefono, cat, muebles ->
                viewModel.guardarNuevo(nombre, cuit, email, telefono, cat, muebles) {
                    snack = "✅ Cliente guardado"
                }
                mostrarDialogoNuevo = false
            }
        )
    }

    if (mostrarMasivo) {
        AlertDialog(
            onDismissRequest = { mostrarMasivo = false },
            title = { Text("Recordatorio masivo", fontWeight = FontWeight.Bold) },
            text = {
                Text("Se abrirán los emails de todos los clientes que tienen dirección configurada (${stats.total - stats.sinEmail} clientes).")
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarMasivo = false
                        snack = "📨 Enviando recordatorios…"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = P)
                ) { Text("Enviar a todos", color = Blanco) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarMasivo = false }) { Text("Cancelar") }
            }
        )
    }

    if (mostrarTourClientes) {
        AlertDialog(
            onDismissRequest = {
                ScreenTourPrefs.markSeenClientes(context)
                mostrarTourClientes = false
            },
            title = { Text("Tour rápido: Clientes", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1) Usá filtros para encontrar riesgos en segundos.", style = MaterialTheme.typography.bodySmall, color = TxSuave)
                    Text("2) Mirá el semáforo en cada card para priorizar.", style = MaterialTheme.typography.bodySmall, color = TxSuave)
                    Text("3) Abrí el panel para ranking y exportación semanal.", style = MaterialTheme.typography.bodySmall, color = TxSuave)
                }
            },
            confirmButton = {
                Button(onClick = {
                    ScreenTourPrefs.markSeenClientes(context)
                    mostrarTourClientes = false
                }) { Text("Entendido") }
            }
        )
    }
}

// ── Panel de control ──────────────────────────────────────────────────────────
@Composable
private fun PanelControl(
    stats: PanelStats,
    filtroActual: String,
    onFiltro: (String) -> Unit,
    onAbrirPanel: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Header gradiente
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF0D3B6E), Color(0xFF1A5FA8))))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Panel del Contador", color = Blanco.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.labelMedium)
                        Text("${stats.total} clientes", color = Blanco,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold)
                    }
                    Box(
                        Modifier.size(52.dp).clip(CircleShape).background(Blanco.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Group, null, tint = Blanco, modifier = Modifier.size(28.dp))
                    }
                }
                OutlinedButton(
                    onClick = onAbrirPanel,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Blanco),
                    border = BorderStroke(1.dp, Blanco.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Analytics, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Abrir panel de métricas", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatChipPanel("${stats.activos}", "activos", VerdeChip, Modifier.weight(1f))
                    StatChipPanel("${stats.porVencer}", "por vencer", NaranjaChip, Modifier.weight(1f))
                    StatChipPanel("${stats.vencidos}", "vencidos", RojoChip, Modifier.weight(1f))
                    StatChipPanel("${stats.conRecateg}", "recateg.", AmbChip, Modifier.weight(1f))
                }
            }
        }

        // Chips de filtro
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val filtros = listOf(
                Triple("todos",      "Todos",         Icons.Default.Group),
                Triple("activos",    "Activos",        Icons.Default.CheckCircle),
                Triple("por_vencer", "Por vencer",     Icons.Default.AccessTime),
                Triple("vencidos",   "Vencidos",       Icons.Default.Warning),
                Triple("recateg",    "Recategorizar",  Icons.AutoMirrored.Filled.TrendingUp),
                Triple("sin_email",  "Sin email",      Icons.Default.MailOutline),
                Triple("honorarios_pendientes", "Honorarios", Icons.Default.AttachMoney)
            )
            items(filtros) { (id, label, icon) ->
                val sel = filtroActual == id
                FilterChip(
                    selected = sel,
                    onClick = { onFiltro(id) },
                    label = { Text(label, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal) },
                    leadingIcon = { Icon(icon, null, modifier = Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = P,
                        selectedLabelColor = Blanco,
                        selectedLeadingIconColor = Blanco
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true, selected = sel,
                        borderColor = Borde, selectedBorderColor = P
                    )
                )
            }
        }
    }
}

private val VerdeChip   = Color(0xFF1B6B32)
private val NaranjaChip = Color(0xFFBF4800)
private val RojoChip    = Color(0xFF9B1B1B)
private val AmbChip     = Color(0xFF7B5200)

@Composable
private fun StatChipPanel(valor: String, label: String, textColor: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Blanco.copy(alpha = 0.15f))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(valor, fontWeight = FontWeight.ExtraBold, color = Blanco, fontSize = 20.sp)
        Text(label, fontSize = 10.sp, color = Blanco.copy(alpha = 0.8f))
    }
}

// ── Card de cliente ───────────────────────────────────────────────────────────
@Composable
private fun ClienteCard(
    cliente: ClienteEntity,
    viewModel: ClientesViewModel,
    compactMode: Boolean = false,
    onVerDetalle: () -> Unit,
    onAplicar: () -> Unit,
    onEliminar: () -> Unit,
    onEmailEnviado: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dias = remember(cliente.proximoVencimientoEpoch) { diasVenc(cliente.proximoVencimientoEpoch) }

    val (cardBg, leftBar) = when {
        dias != null && dias < 0  -> RClaro to Rojo
        dias != null && dias <= 7 -> NClaro to Naranja
        cliente.estadoCliente == "inactivo" -> GrisC to TxSuave
        else -> Blanco to Verde
    }

    // Categoría calculada
    val catCalculada = remember(cliente.ingresoAnualCliente, cliente.categoria) {
        if (cliente.ingresoAnualCliente > 0)
            CategoriaMonotributo.porLimiteAnual(cliente.ingresoAnualCliente)
        else null
    }
    val debeRecategorizar = catCalculada != null && cliente.categoria != null &&
        catCalculada.letra != cliente.categoria
    val categoriaRef = catCalculada ?: cliente.categoria?.let { letra ->
        CategoriaMonotributo.entries.firstOrNull { it.letra == letra }
    }
    val porcentajeUso = if (categoriaRef != null && categoriaRef.limiteAnual > 0.0 && cliente.ingresoAnualCliente > 0.0) {
        (cliente.ingresoAnualCliente / categoriaRef.limiteAnual).toFloat()
    } else {
        0f
    }
    val (semaforoColor, semaforoTexto) = when {
        porcentajeUso >= 0.90f -> Rojo to "Riesgo alto"
        porcentajeUso >= 0.75f -> Naranja to "Riesgo medio"
        porcentajeUso > 0f -> Verde to "Riesgo bajo"
        else -> TxSuave to "Sin datos"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, AppRadius.lg)
            .animateContentSize()
            .clickable { onVerDetalle() },
        shape = AppRadius.lg,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, if (dias != null && dias < 0) Rojo.copy(0.3f) else Borde)
    ) {
        Row(Modifier.fillMaxWidth()) {
            // Barra lateral de color
            Box(Modifier.width(4.dp).fillMaxHeight().background(leftBar, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)))
            Column(Modifier.padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(if (compactMode) 6.dp else 8.dp)) {
                // Fila superior
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Avatar
                    Box(
                        Modifier.size(44.dp).clip(CircleShape)
                            .background(Brush.linearGradient(listOf(P, PAcc))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(iniciales(cliente.nombre), fontWeight = FontWeight.ExtraBold,
                            color = Blanco, fontSize = 15.sp)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(cliente.nombre, fontWeight = FontWeight.Bold, color = TxOsc,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (cliente.cuit.isNotBlank()) {
                            Text("CUIT ${cliente.cuit}", style = MaterialTheme.typography.bodySmall,
                                color = TxSuave)
                        }
                    }
                    // Botón rápido eliminar
                    IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, null, tint = Rojo.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp))
                    }
                }

                // Chips de estado
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()) {
                    MiniChip("● $semaforoTexto", semaforoColor.copy(alpha = 0.15f), semaforoColor)
                    // Categoría
                    if (cliente.categoria != null) {
                        MiniChip("Cat. ${cliente.categoria}", Amb.copy(0.2f), TxOsc)
                    }
                    // Rubro
                    if (cliente.esVentaMuebles) MiniChip("Muebles", Surface, P)
                    // Vencimiento / Estado
                    when {
                        dias != null && dias < 0 ->
                            MiniChip("⛔ Venció hace ${-dias}d", RClaro, Rojo)
                        dias != null && dias == 0L ->
                            MiniChip("⚠️ Vence HOY", NClaro, Naranja)
                        dias != null && dias <= 7 ->
                            MiniChip("⚠️ ${dias}d", NClaro, Naranja)
                        dias != null ->
                            MiniChip("✅ ${dias}d", VClaro, Verde)
                        cliente.estadoCliente == "inactivo" ->
                            MiniChip("⚫ Inactivo", GrisC, TxSuave)
                        else -> MiniChip("✅ Activo", VClaro, Verde)
                    }
                    // Recategorización
                    if (debeRecategorizar) {
                        MiniChip("↕️ Recateg.", Color(0xFFFFF3E0), Color(0xFFE65100))
                    }
                }

                // Contacto rápido
                if (cliente.email.isNotBlank() || cliente.telefono.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (cliente.email.isNotBlank()) {
                            Text("📧 ${cliente.email}", style = MaterialTheme.typography.labelSmall,
                                color = P, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false))
                        }
                        if (cliente.telefono.isNotBlank()) {
                            Text("📱 ${cliente.telefono}", style = MaterialTheme.typography.labelSmall,
                                color = TxSuave)
                        }
                    }
                }

                if (compactMode) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        MiniChip("${(porcentajeUso * 100).toInt()}% límite", Surface, P)
                        MiniChip(
                            if (cliente.ingresoAnualCliente > 0.0) "Anual ${"%.0f".format(cliente.ingresoAnualCliente / 1000)}K" else "Sin anual",
                            GrisC,
                            TxSuave
                        )
                    }
                }

                HorizontalDivider(color = Borde.copy(alpha = 0.5f))

                // Botones de acción
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AccionBtn("Cargar", Icons.Default.SwapHoriz, P, onAplicar)
                    if (cliente.email.isNotBlank()) {
                        AccionBtn("Email", Icons.Default.Email, Amb) {
                            scope.launch {
                                val intent = viewModel.enviarRecordatorio(context, cliente)
                                intent?.let { runCatching { context.startActivity(it) }; onEmailEnviado() }
                            }
                        }
                    }
                    if (cliente.telefono.isNotBlank()) {
                        AccionBtn("WA", Icons.AutoMirrored.Filled.Chat, Verde) {
                            val num = cliente.telefono.filter { it.isDigit() }
                            context.startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://wa.me/$num")))
                        }
                        AccionBtn("Llamar", Icons.Default.Phone, PAcc) {
                            context.startActivity(Intent(Intent.ACTION_DIAL,
                                Uri.parse("tel:${cliente.telefono}")))
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    // Botón de detalle
                    OutlinedButton(
                        onClick = onVerDetalle,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, P)
                    ) {
                        Text("Ver perfil", color = P, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ChevronRight, null, tint = P, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SkeletonClienteCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = AppRadius.lg,
        colors = CardDefaults.cardColors(containerColor = Blanco),
        border = BorderStroke(1.dp, Borde)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier
                    .fillMaxWidth(0.5f)
                    .height(14.dp)
                    .clip(AppRadius.sm)
                    .shimmerLoading(GrisC, Blanco)
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(AppRadius.sm)
                    .shimmerLoading(GrisC, Blanco)
            )
            Box(
                Modifier
                    .fillMaxWidth(0.75f)
                    .height(10.dp)
                    .clip(AppRadius.sm)
                    .shimmerLoading(GrisC, Blanco)
            )
        }
    }
}

@Composable
private fun AccionBtn(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = Modifier.height(30.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(3.dp))
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MiniChip(texto: String, bg: Color, textColor: Color) {
    Box(Modifier.clip(RoundedCornerShape(6.dp)).background(bg).padding(horizontal = 7.dp, vertical = 3.dp)) {
        Text(texto, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

// ── Estado vacío ──────────────────────────────────────────────────────────────
@Composable
private fun EstadoVacio(
    filtroActual: String,
    hayBusqueda: Boolean,
    onCrear: () -> Unit,
    onLimpiarFiltro: () -> Unit,
    onLimpiarBusqueda: () -> Unit
) {
    val (emoji, titulo, subtitulo) = when {
        hayBusqueda         -> Triple("🔍", "Sin resultados", "Probá con otro nombre o CUIT")
        filtroActual != "todos" -> Triple("✅", "No hay clientes con ese filtro",
            "Todos tus clientes están bien en esa categoría")
        else -> Triple("👥", "Todavía no tenés clientes",
            "Guardá nombre, CUIT y categoría de cada cliente para gestionarlos fácil")
    }
    Column(
        Modifier.fillMaxWidth().padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(emoji, fontSize = 52.sp)
        Text(titulo, fontWeight = FontWeight.Bold, color = TxOsc,
            style = MaterialTheme.typography.titleMedium)
        Text(subtitulo, style = MaterialTheme.typography.bodySmall,
            color = TxSuave, modifier = Modifier.padding(horizontal = 32.dp))
        if (!hayBusqueda && filtroActual == "todos") {
            Button(onClick = onCrear, colors = ButtonDefaults.buttonColors(containerColor = P),
                shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.PersonAdd, null)
                Spacer(Modifier.width(6.dp))
                Text("Crear primer cliente", fontWeight = FontWeight.Bold)
            }
        } else if (hayBusqueda) {
            OutlinedButton(onClick = onLimpiarBusqueda, shape = RoundedCornerShape(10.dp)) {
                Text("Limpiar búsqueda")
            }
        } else if (filtroActual != "todos") {
            TextButton(onClick = onLimpiarFiltro) { Text("Ver todos los clientes") }
        }
    }
}

// ── Diálogo nuevo cliente ──────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoNuevoCliente(
    onDismiss: () -> Unit,
    onGuardar: (nombre: String, cuit: String, email: String, telefono: String,
                categoria: String?, esVentaMuebles: Boolean) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var cuit by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf<String?>(null) }
    var muebles by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(P),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PersonAdd, null, tint = Blanco, modifier = Modifier.size(20.dp))
                }
                Text("Nuevo cliente", fontWeight = FontWeight.Bold, color = TxOsc)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DialogCampo("Nombre / Razón social *", nombre, Icons.Default.Person) { nombre = it }
                DialogCampo("CUIT", cuit, Icons.Default.Badge) {
                    cuit = it.filter { c -> c.isDigit() || c == '-' }
                }
                DialogCampo("Email", email, Icons.Default.Email) { email = it }
                DialogCampo("Teléfono", telefono, Icons.Default.Phone) { telefono = it }

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = categoria?.let { "Categoría $it" } ?: "Automática",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { Icon(Icons.Default.ExpandMore, null) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = dialogCampoColores()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("Automática (según facturación)") },
                            onClick = { categoria = null; expanded = false })
                        HorizontalDivider()
                        CategoriaMonotributo.entries.forEach { c ->
                            DropdownMenuItem(text = { Text("Categoría ${c.letra}") },
                                onClick = { categoria = c.letra; expanded = false })
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = muebles, onCheckedChange = { muebles = it },
                        colors = CheckboxDefaults.colors(checkedColor = P))
                    Text("Venta de cosas muebles", color = TxOsc,
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onGuardar(nombre, cuit, email, telefono, categoria, muebles) },
                enabled = nombre.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = P),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Guardar", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = TxSuave) }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun DialogCampo(label: String, value: String, icon: ImageVector, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        label = { Text(label, fontSize = 12.sp) },
        leadingIcon = { Icon(icon, null, tint = P, modifier = Modifier.size(18.dp)) },
        singleLine = true, modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = dialogCampoColores()
    )
}

@Composable
private fun dialogCampoColores() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = P, focusedLabelColor = P, cursorColor = P,
    unfocusedBorderColor = Borde
)

