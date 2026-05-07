package com.reeb.controlmonotributoar.ui.home

import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
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
import com.reeb.controlmonotributoar.ui.components.FabAction
import com.reeb.controlmonotributoar.ui.components.SpeedDialFab
import com.reeb.controlmonotributoar.ui.components.shimmerLoading
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import com.reeb.controlmonotributoar.data.remote.RemoteConfigHolder
import com.reeb.controlmonotributoar.domain.model.Alerta
import com.reeb.controlmonotributoar.domain.model.Movimiento
import com.reeb.controlmonotributoar.domain.model.ResumenMensual
import com.reeb.controlmonotributoar.domain.model.TipoMovimiento
import com.reeb.controlmonotributoar.domain.model.UserRole
import com.reeb.controlmonotributoar.utils.CategoriaCalculator
import com.reeb.controlmonotributoar.utils.CurrencyFormatter
import com.reeb.controlmonotributoar.utils.DateUtils
import com.reeb.controlmonotributoar.utils.HomeUxPrefs
import com.reeb.controlmonotributoar.utils.ScreenTourPrefs
import com.reeb.controlmonotributoar.ui.theme.AppRadius
import com.reeb.controlmonotributoar.ui.theme.GrisClaro
import java.text.SimpleDateFormat
import java.util.Calendar
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
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(
    viewModel: HomeViewModel,
    onAgregarIngreso: () -> Unit,
    onVerAlertas: () -> Unit,
    onAgregarGasto: () -> Unit = onAgregarIngreso,
    onAgregarCliente: () -> Unit = {},
    onIrCategoria: () -> Unit = {},
    onAbrirPanelClientes: () -> Unit = {},
    onAbrirCobranzas: () -> Unit = {},
    onIrClientesPorVencer: () -> Unit = {},
    onIrClientesSinEmail: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)
    val appBarAlpha = (0.55f + (topAppBarState.collapsedFraction * 0.37f)).coerceIn(0.55f, 0.92f)
    val configRepo = remember { RemoteConfigHolder.get(context) }
    val config by configRepo.config.collectAsStateWithLifecycle()
    var mostrarOnboardingCorto by rememberSaveable { mutableStateOf(false) }
    var mostrarTourHome by rememberSaveable { mutableStateOf(false) }
    var modoPro by rememberSaveable { mutableStateOf(false) }
    var mostrarHero by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.actualizarAlertas() }
    LaunchedEffect(state.rol) {
        mostrarOnboardingCorto = !HomeUxPrefs.wasOnboardingSeen(context, state.rol)
    }
    LaunchedEffect(Unit) {
        mostrarTourHome = !ScreenTourPrefs.wasSeenHome(context)
    }
    LaunchedEffect(Unit) {
        mostrarHero = true
    }

    Scaffold(
        containerColor = BlancoSuave,
        topBar = {
            TopAppBar(
                title = { Text("Inicio", fontWeight = FontWeight.Bold) },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF75AADB).copy(alpha = appBarAlpha),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            // Si el usuario es PERSONAL, ocultamos la acción "Cliente" del FAB
            // (no necesita administrar clientes, solo su propio Monotributo).
            val acciones = buildList {
                add(FabAction("Ingreso", Icons.Default.TrendingUp, onAgregarIngreso))
                add(FabAction("Gasto", Icons.Default.TrendingDown, onAgregarGasto))
                if (state.rol == com.reeb.controlmonotributoar.domain.model.UserRole.CONTADOR) {
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
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AnimatedVisibility(
                visible = mostrarHero,
                enter = slideInVertically(initialOffsetY = { -it / 3 }) + fadeIn(),
                exit = fadeOut()
            ) {
                HomeHeroCard(
                    rol = state.rol,
                    totalClientes = state.totalClientes,
                    clientesVencen7Dias = state.clientesVencen7Dias
                )
            }

            if (state.rol == UserRole.CONTADOR) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = modoPro,
                        onClick = { modoPro = !modoPro },
                        label = { Text(if (modoPro) "Modo Pro activado" else "Modo Pro") }
                    )
                }
            }
            // Chip de rubro y versión de tarifas
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
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Amarillo.copy(alpha = 0.20f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        "Tarifas v${config.version} • ${config.actualizadoEl.ifBlank { "sin fecha" }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoOscuro,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            val resumen = state.resumen
            if (resumen == null) {
                HomeSkeletonLoading()
            } else {
                HomeSummaryContent(
                    resumen = resumen,
                    esVentaMuebles = state.esVentaMuebles,
                    rol = state.rol,
                    ultimosMovimientos = state.ultimosMovimientos,
                    alertasNoLeidas = state.alertasNoLeidas,
                    totalClientes = state.totalClientes,
                    clientesSinEmail = state.clientesSinEmail,
                    clientesVencen7Dias = state.clientesVencen7Dias,
                    honorariosPendientesHoy = state.honorariosPendientesHoy,
                    honorariosACobrarMes = state.honorariosACobrarMes,
                    mostrarOnboardingCorto = mostrarOnboardingCorto,
                    onCerrarOnboardingCorto = {
                        HomeUxPrefs.markOnboardingSeen(context, state.rol)
                        mostrarOnboardingCorto = false
                    },
                    onAgregarIngreso = onAgregarIngreso,
                    onAgregarCliente = onAgregarCliente,
                    onVerAlertas = onVerAlertas,
                    onAbrirPanelClientes = onAbrirPanelClientes,
                    onAbrirCobranzas = onAbrirCobranzas,
                    onIrClientesPorVencer = onIrClientesPorVencer,
                    onIrClientesSinEmail = onIrClientesSinEmail,
                    compactMode = modoPro
                )
            }
        }
    }

    if (mostrarTourHome) {
        TourDialog(
            titulo = "Tour rápido: Inicio",
            pasos = listOf(
                "1) Mirá el bloque 'Modo hoy' para saber qué hacer primero.",
                "2) Usá 'Acciones rápidas' para ir directo a pendientes.",
                "3) Revisá el progreso al límite para evitar sorpresas."
            ),
            onCerrar = {
                ScreenTourPrefs.markSeenHome(context)
                mostrarTourHome = false
            }
        )
    }
}

@Composable
private fun HomeHeroCard(
    rol: UserRole,
    totalClientes: Int,
    clientesVencen7Dias: Int
) {
    val fecha = remember {
        SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "AR")).format(Calendar.getInstance().time)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "AR")) else it.toString() }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppRadius.md,
        colors = CardDefaults.cardColors(containerColor = Blanco),
        border = BorderStroke(1.dp, GrisCeleste)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Hola 👋", color = CelesteOsc, fontWeight = FontWeight.Bold)
            Text(fecha, color = TextoSuave, style = MaterialTheme.typography.bodySmall)
            Text(
                if (rol == UserRole.CONTADOR) "Panel del estudio contable" else "Resumen de tu monotributo",
                color = TextoOscuro,
                fontWeight = FontWeight.SemiBold
            )
            if (rol == UserRole.CONTADOR) {
                Text(
                    "$totalClientes clientes · $clientesVencen7Dias vencen esta semana",
                    color = TextoSuave,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun HomeSummaryContent(
    resumen: ResumenMensual,
    esVentaMuebles: Boolean,
    rol: UserRole,
    ultimosMovimientos: List<Movimiento> = emptyList(),
    alertasNoLeidas: List<Alerta> = emptyList(),
    totalClientes: Int = 0,
    clientesSinEmail: Int = 0,
    clientesVencen7Dias: Int = 0,
    honorariosPendientesHoy: Int = 0,
    honorariosACobrarMes: Double = 0.0,
    mostrarOnboardingCorto: Boolean = false,
    onCerrarOnboardingCorto: () -> Unit = {},
    onAgregarIngreso: () -> Unit = {},
    onAgregarCliente: () -> Unit = {},
    onVerAlertas: () -> Unit = {},
    onAbrirPanelClientes: () -> Unit = {},
    onAbrirCobranzas: () -> Unit = {},
    onIrClientesPorVencer: () -> Unit = {},
    onIrClientesSinEmail: () -> Unit = {},
    compactMode: Boolean = false
) {
    val cuota = resumen.categoriaActual.cuotaSegunRubro(esVentaMuebles)
    val facturadoAnual   = resumen.totalIngresosAnual          // ⬅ Suma total del año
    val proxima          = CategoriaCalculator.proximaCategoria(facturadoAnual)
    val promedioPermitido = CategoriaCalculator.promedioMensualPermitido(facturadoAnual)
    val porcentaje = resumen.porcentajeDelLimite

    if (mostrarOnboardingCorto) {
        OnboardingCortoCard(
            rol = rol,
            onCerrar = onCerrarOnboardingCorto
        )
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ModoHoyChecklistCard(
                rol = rol,
                tieneMovimientos = ultimosMovimientos.isNotEmpty(),
                tieneAlertas = alertasNoLeidas.isNotEmpty(),
                porcentajeLimite = porcentaje,
                totalClientes = totalClientes,
                onAgregarIngreso = onAgregarIngreso,
                onVerAlertas = onVerAlertas
            )

            AccionesRapidasCard(
                rol = rol,
                totalClientes = totalClientes,
                clientesSinEmail = clientesSinEmail,
                clientesVencen7Dias = clientesVencen7Dias,
                cantidadAlertas = alertasNoLeidas.size,
                porcentajeLimite = porcentaje,
                honorariosPendientesHoy = honorariosPendientesHoy,
                honorariosACobrarMes = honorariosACobrarMes,
                onAgregarCliente = onAgregarCliente,
                onAbrirPanelClientes = onAbrirPanelClientes,
                onAbrirCobranzas = onAbrirCobranzas,
                onVerAlertas = onVerAlertas,
                onIrClientesPorVencer = onIrClientesPorVencer,
                onIrClientesSinEmail = onIrClientesSinEmail
            )
        }
    }

    if (rol == UserRole.CONTADOR) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = AppRadius.md,
            colors = CardDefaults.cardColors(containerColor = Blanco),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, GrisCeleste)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("🧾 Modo contador", color = TextoOscuro, fontWeight = FontWeight.Bold)
                    Text("Abrí métricas por cliente y ranking de riesgo", color = TextoSuave, style = MaterialTheme.typography.bodySmall)
                }
                OutlinedButton(onClick = onAbrirPanelClientes, shape = RoundedCornerShape(10.dp)) {
                    Text("Ver panel")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = AppRadius.md,
            colors = CardDefaults.cardColors(containerColor = Blanco),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, GrisCeleste)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("💼 Honorarios del estudio", color = TextoOscuro, fontWeight = FontWeight.Bold)
                Text(
                    "Pendientes hoy: $honorariosPendientesHoy",
                    color = TextoSuave,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "A cobrar este mes: ${CurrencyFormatter.formatear(honorariosACobrarMes)}",
                    color = CelesteOsc,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedButton(onClick = onAbrirCobranzas, shape = RoundedCornerShape(10.dp)) {
                    Text("Ver cobranzas")
                }
            }
        }
    }

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

    // ── Métricas personales rápidas ──
    MetricasPersonalesCard(resumen = resumen, compactMode = compactMode)

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
private fun MetricasPersonalesCard(resumen: ResumenMensual, compactMode: Boolean = false) {
    val hoy = Calendar.getInstance()
    val mesActual = hoy.get(Calendar.MONTH) + 1
    val mesesRestantes = (12 - mesActual + 1).coerceAtLeast(1)
    val proyeccionAnual = resumen.totalIngresosAnual + (resumen.totalIngresos * (mesesRestantes - 1))
    val margenRestante = (resumen.categoriaActual.limiteAnual - resumen.totalIngresosAnual).coerceAtLeast(0.0)

    val calVto = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 20)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (timeInMillis < System.currentTimeMillis()) add(Calendar.MONTH, 1)
    }
    val diasVto = ((calVto.timeInMillis - System.currentTimeMillis()) / (1000L * 60 * 60 * 24)).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, GrisCeleste)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("📈 Métricas personales", fontWeight = FontWeight.Bold, color = TextoOscuro)
            InfoLine(
                label = "Proyección anual simple",
                valor = CurrencyFormatter.formatear(proyeccionAnual),
                hint = if (compactMode) "" else "estimada con tu ritmo mensual actual"
            )
            InfoLine(
                label = "Margen restante categoría ${resumen.categoriaActual.letra}",
                valor = CurrencyFormatter.formatear(margenRestante),
                hint = if (compactMode) "" else "hasta tope anual vigente"
            )
            InfoLine(
                label = "Próximo vencimiento",
                valor = "$diasVto días",
                hint = if (compactMode) "" else "recordatorio sugerido: 15/7/1 días antes"
            )
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
        if (hint.isNotBlank()) {
            Text(hint, style = MaterialTheme.typography.bodySmall, color = TextoSuave)
        }
    }
}

@Composable
private fun HomeSkeletonLoading() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(3) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, GrisCeleste),
                colors = CardDefaults.cardColors(containerColor = Blanco)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier
                            .fillMaxWidth(0.55f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .shimmerLoading(GrisCeleste, Blanco)
                            .alpha(0.7f)
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .shimmerLoading(GrisClaro, Blanco)
                    )
                    Box(
                        Modifier
                            .fillMaxWidth(0.75f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .shimmerLoading(GrisClaro, Blanco)
                    )
                }
            }
        }
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Todavía no registraste movimientos. Cargá tu primer ingreso y activá alertas para no pasarte del límite.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSuave
                    )
                    OutlinedButton(onClick = onVerAlertas, shape = RoundedCornerShape(10.dp)) {
                        Text("Revisar alertas sugeridas")
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingCortoCard(rol: UserRole, onCerrar: () -> Unit) {
    val pasos = if (rol == UserRole.CONTADOR) {
        listOf(
            "1) Cargá tu cartera de clientes con CUIT/categoría.",
            "2) Revisá el panel para detectar riesgos y vencimientos.",
            "3) Exportá el ranking semanal para seguimiento."
        )
    } else {
        listOf(
            "1) Cargá ingresos y gastos de esta semana.",
            "2) Revisá el % del límite y próximas alertas.",
            "3) Planificá tu margen para evitar recategorización inesperada."
        )
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppRadius.md,
        colors = CardDefaults.cardColors(containerColor = Blanco),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, GrisCeleste)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🚀 Guía rápida (${if (rol == UserRole.CONTADOR) "contador" else "personal"})", fontWeight = FontWeight.Bold, color = TextoOscuro)
                TextButton(onClick = onCerrar) { Text("Entendido") }
            }
            pasos.forEach { Text(it, color = TextoSuave, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun ModoHoyChecklistCard(
    rol: UserRole,
    tieneMovimientos: Boolean,
    tieneAlertas: Boolean,
    porcentajeLimite: Float,
    totalClientes: Int,
    onAgregarIngreso: () -> Unit,
    onVerAlertas: () -> Unit
) {
    val check1 = tieneMovimientos
    val check2 = !tieneAlertas
    val check3 = porcentajeLimite < 0.85f
    val check4 = rol != UserRole.CONTADOR || totalClientes > 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppRadius.md,
        colors = CardDefaults.cardColors(containerColor = Blanco),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, GrisCeleste)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("✅ Modo hoy", color = TextoOscuro, fontWeight = FontWeight.Bold)
            ChecklistRow("Cargar al menos un movimiento hoy", check1)
            ChecklistRow("Revisar alertas pendientes", check2)
            ChecklistRow("Mantener margen sano (<85%)", check3)
            if (rol == UserRole.CONTADOR) ChecklistRow("Tener al menos 1 cliente activo", check4)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onAgregarIngreso, shape = RoundedCornerShape(10.dp)) { Text("Cargar movimiento") }
                OutlinedButton(onClick = onVerAlertas, shape = RoundedCornerShape(10.dp)) { Text("Ver alertas") }
            }
        }
    }
}

@Composable
private fun ChecklistRow(texto: String, done: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Checkbox(checked = done, onCheckedChange = null)
        Text(texto, color = TextoSuave, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun AccionesRapidasCard(
    rol: UserRole,
    totalClientes: Int,
    clientesSinEmail: Int,
    clientesVencen7Dias: Int,
    cantidadAlertas: Int,
    porcentajeLimite: Float,
    honorariosPendientesHoy: Int,
    honorariosACobrarMes: Double,
    onAgregarCliente: () -> Unit,
    onAbrirPanelClientes: () -> Unit,
    onAbrirCobranzas: () -> Unit,
    onVerAlertas: () -> Unit,
    onIrClientesPorVencer: () -> Unit,
    onIrClientesSinEmail: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppRadius.md,
        colors = CardDefaults.cardColors(containerColor = Blanco),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, GrisCeleste)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("⚡ Acciones rápidas", color = TextoOscuro, fontWeight = FontWeight.Bold)
            if (rol == UserRole.CONTADOR && totalClientes == 0) {
                Text("Te faltan clientes para aprovechar métricas y recordatorios.", color = TextoSuave, style = MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick = onAgregarCliente) { Text("Crear primer cliente") }
            }
            if (rol == UserRole.CONTADOR && clientesVencen7Dias > 0) {
                Text("Vencen $clientesVencen7Dias clientes esta semana.", color = TextoSuave, style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onIrClientesPorVencer) { Text("Abrir por vencer") }
                    OutlinedButton(onClick = onAbrirPanelClientes) { Text("Ver críticos") }
                }
            }
            if (rol == UserRole.CONTADOR && clientesSinEmail > 0) {
                Text("Hay $clientesSinEmail clientes sin email configurado.", color = TextoSuave, style = MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick = onIrClientesSinEmail) { Text("Abrir sin email") }
            }
            if (rol == UserRole.CONTADOR && honorariosPendientesHoy > 0) {
                Text(
                    "Tenés $honorariosPendientesHoy honorarios para cobrar hoy (${CurrencyFormatter.formatear(honorariosACobrarMes)} en total).",
                    color = TextoSuave,
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedButton(onClick = onAbrirPanelClientes) { Text("Gestionar cobranzas") }
                OutlinedButton(onClick = onAbrirCobranzas) { Text("Abrir vista cobranzas") }
            }
            if (cantidadAlertas > 0) {
                Text("Tenés $cantidadAlertas alertas sin leer.", color = TextoSuave, style = MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick = onVerAlertas) { Text("Resolver alertas") }
            }
            if (porcentajeLimite >= 0.85f) {
                Text("Estás cerca del límite anual. Revisá la planificación.", color = Color(0xFFE53935), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun TourDialog(
    titulo: String,
    pasos: List<String>,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(titulo, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                pasos.forEach { Text(it, style = MaterialTheme.typography.bodySmall, color = TextoSuave) }
            }
        },
        confirmButton = {
            Button(onClick = onCerrar) { Text("Entendido") }
        }
    )
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


