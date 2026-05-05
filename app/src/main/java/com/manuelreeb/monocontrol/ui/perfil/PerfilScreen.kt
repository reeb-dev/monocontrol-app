package com.manuelreeb.monocontrol.ui.perfil

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manuelreeb.monocontrol.data.remote.RemoteConfigHolder
import com.manuelreeb.monocontrol.data.remote.RemoteConfigRepository
import com.manuelreeb.monocontrol.domain.model.CategoriaMonotributo
import com.manuelreeb.monocontrol.domain.model.ResumenMensual
import com.manuelreeb.monocontrol.domain.model.UserRole
import com.manuelreeb.monocontrol.ui.theme.*
import com.manuelreeb.monocontrol.utils.CurrencyFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel,
    resumen: ResumenMensual? = null,
    onIrAExcel: () -> Unit = {},
    onIrASimulacion: () -> Unit = {},
    onIrAClientes: () -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    onContactoClick: () -> Unit = {}
) {
    val perfilGuardado by viewModel.perfil.collectAsStateWithLifecycle()

    var nombre by rememberSaveable { mutableStateOf("") }
    var cuit by rememberSaveable { mutableStateOf("") }
    var hidratado by rememberSaveable { mutableStateOf(false) }
    var editando by rememberSaveable { mutableStateOf(false) }
    var guardado by remember { mutableStateOf(false) }
    var expandidoSeguridad by remember { mutableStateOf(false) }
    var expandidoNotif by remember { mutableStateOf(false) }
    var expandidoRespaldo by remember { mutableStateOf(false) }
    var mostrarConfirmCerrar by remember { mutableStateOf(false) }
    var notificacionesActivas by remember { mutableStateOf(true) }

    val esVentaMuebles = perfilGuardado?.esVentaMuebles ?: false

    LaunchedEffect(perfilGuardado) {
        val p = perfilGuardado
        if (p != null && !hidratado) {
            nombre = p.nombre
            cuit = p.cuit
            hidratado = true
            editando = p.nombre.isBlank() && p.cuit.isBlank()
        } else if (p == null && !hidratado) {
            editando = true
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
        // ── HEADER GRADIENTE ─────────────────────────────────────────────
        HeaderPerfil(nombre, cuit, perfilGuardado, resumen, esVentaMuebles)

        // ── RESUMEN RÁPIDO (si hay resumen) ──────────────────────────────
        if (resumen != null) {
            ResumenRapidoCard(resumen, esVentaMuebles)
        }

        // ── CUOTA Y VENCIMIENTO ──────────────────────────────────────────
        if (resumen != null) {
            CuotaVencimientoCard(resumen, esVentaMuebles)
        }

        // ── HISTORIAL / TENDENCIA ────────────────────────────────────────
        if (resumen != null) {
            HistorialCard(resumen)
        }

        // ── RUBRO ────────────────────────────────────────────────────────
        RubroCard(esVentaMuebles) { viewModel.cambiarRubro(it) }

        // ── PAGOS MONOTRIBUTO CLIENTES (solo CONTADOR) ───────────────────
        if (UserRole.fromName(perfilGuardado?.rol) == UserRole.CONTADOR) {
            PagosClientesCard(viewModel)
        }

        // ── ROL (PERSONAL / CONTADOR) ────────────────────────────────────
        RolToggleCard(
            actual = UserRole.fromName(perfilGuardado?.rol),
            onSeleccion = { viewModel.cambiarRol(it) }
        )

        // ── MODO PRUEBA (OVERRIDE CATEGORÍA) ─────────────────────────────
        CategoriaOverrideCard(
            actual = perfilGuardado?.categoriaOverride,
            onSeleccion = { viewModel.setCategoriaOverride(it) }
        )

        // ── DATOS PERSONALES ─────────────────────────────────────────────
        DatosPersonalesCard(nombre, cuit, editando, guardado,
            onEditarClick = { editando = true; guardado = false },
            onNombreChange = { nombre = it; guardado = false },
            onCuitChange = {
                cuit = it.filter { c -> c.isDigit() || c == '-' }
                guardado = false
            },
            onGuardarClick = {
                viewModel.guardar(nombre, cuit)
                guardado = true
                editando = false
            }
        )

        // ── ESTADÍSTICAS ─────────────────────────────────────────────────
        if (resumen != null) {
            EstadisticasCard(resumen)
        }

        // ── NOTIFICACIONES ───────────────────────────────────────────────
        NotificacionesCard(
            expandido = expandidoNotif,
            onExpandClick = { expandidoNotif = !expandidoNotif },
            notificacionesActivas = notificacionesActivas,
            onToggleNotif = { notificacionesActivas = it }
        )


        // ── RESPALDO EN NUBE ────────────────────────────────────────────
        RespaldoCard(
            expandido = expandidoRespaldo,
            onExpandClick = { expandidoRespaldo = !expandidoRespaldo }
        )

        // ── SOPORTE Y CONTACTO ───────────────────────────────────────────
        SoporteCard(onContactoClick = onContactoClick)

        // ── CRÉDITOS Y DONACIONES ────────────────────────────────────────
        CreditosCard()

        // ── HERRAMIENTAS ─────────────────────────────────────────────────
        Text(
            "Herramientas",
            style = MaterialTheme.typography.labelLarge,
            color = GrisMedio,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
        )

        ToolItem(
            icon = Icons.Default.Group,
            title = "Clientes guardados",
            subtitle = "Buscá y cambiá entre los clientes que cargaste",
            onClick = onIrAClientes,
            visible = UserRole.fromName(perfilGuardado?.rol) == UserRole.CONTADOR
        )
        ToolItem(
            icon = Icons.Default.Description,
            title = "Exportar a Excel",
            subtitle = "Descargá tus movimientos para tu contador",
            onClick = onIrAExcel
        )
        ToolItem(
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            title = "Simular facturación",
            subtitle = "Probá si subís de categoría con un nuevo monto",
            onClick = onIrASimulacion
        )

        // ── SEGURIDAD Y SESIÓN ───────────────────────────────────────────
        SeguridadCard(
            expandido = expandidoSeguridad,
            onExpandClick = { expandidoSeguridad = !expandidoSeguridad },
            onCerrarSesion = { mostrarConfirmCerrar = true }
        )

        // ── INFORMACIÓN LEGAL Y VERSIÓN ──────────────────────────────────
        TarifasRemotasCard()

        InformacionLegalCard()

        Spacer(Modifier.height(80.dp))
    }

    // Diálogo de confirmación cerrar sesión
    if (mostrarConfirmCerrar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmCerrar = false },
            title = { Text("Cerrar sesión", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro que querés cerrar sesión? Tendrás que ingresar de nuevo.") },
            confirmButton = {
                Button(
                    onClick = { onCerrarSesion(); mostrarConfirmCerrar = false },
                    colors = ButtonDefaults.buttonColors(containerColor = RojoMedio)
                ) {
                    Text("Cerrar sesión", color = BlancoArgentino)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmCerrar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// ── HEADER GRADIENTE ─────────────────────────────────────────────────────────
@Composable
private fun HeaderPerfil(
    nombre: String,
    cuit: String,
    perfil: com.manuelreeb.monocontrol.data.local.entity.PerfilEntity?,
    resumen: ResumenMensual?,
    esVentaMuebles: Boolean
) {
    val diaHoy = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val proximoVencimiento = diaHoy.apply { set(Calendar.DAY_OF_MONTH, 20) }
    if (diaHoy.get(Calendar.DAY_OF_MONTH) > 20) {
        proximoVencimiento.add(Calendar.MONTH, 1)
    }
    val diasVenc = ((proximoVencimiento.timeInMillis - diaHoy.timeInMillis) / (1000L * 60 * 60 * 24)).toInt()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF0D3B6E), CelesteOscuro)
                )
            )
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(BlancoArgentino.copy(alpha = 0.2f))
                        .border(2.dp, SolAmarillo, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (nombre.firstOrNull()?.uppercase() ?: "?") +
                            (nombre.split(" ").getOrNull(1)?.firstOrNull()?.uppercase() ?: ""),
                        color = BlancoArgentino,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        if (nombre.isBlank()) "Tu perfil" else nombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = BlancoArgentino
                    )
                    if (cuit.isNotBlank()) {
                        Text("CUIT $cuit", color = BlancoArgentino.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Info rápida en chips
            if (resumen != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ChipHeader("Cat. ${resumen.categoriaActual.letra}", BlancoArgentino.copy(0.2f), BlancoArgentino)
                    ChipHeader(
                        "${(resumen.porcentajeDelLimite * 100).toInt()}% límite",
                        BlancoArgentino.copy(0.2f),
                        BlancoArgentino
                    )
                    ChipHeader(
                        "💰 " + CurrencyFormatter.formatearCompacto(
                            resumen.categoriaActual.cuotaSegunRubro(esVentaMuebles)
                        ),
                        SolAmarillo.copy(0.3f),
                        BlancoArgentino
                    )
                }
            }

            // Vencimiento cuota
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BlancoArgentino.copy(alpha = 0.1f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DateRange, null, tint = BlancoArgentino, modifier = Modifier.size(16.dp))
                Column {
                    Text(
                        "Próxima cuota vence en $diasVenc días",
                        color = BlancoArgentino.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        SimpleDateFormat("d 'de' MMMM", Locale("es", "AR")).format(proximoVencimiento.time),
                        color = BlancoArgentino,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ChipHeader(text: String, bg: Color, textColor: Color) {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

// ── RESUMEN RÁPIDO ───────────────────────────────────────────────────────────
@Composable
private fun ResumenRapidoCard(resumen: ResumenMensual, esVentaMuebles: Boolean) {
    val cuota = resumen.categoriaActual.cuotaSegunRubro(esVentaMuebles)
    val colorBarra = when {
        resumen.porcentajeDelLimite >= 0.95f -> RojoError
        resumen.porcentajeDelLimite >= 0.80f -> NaranjaAlerta
        resumen.porcentajeDelLimite >= 0.60f -> SolAmarillo
        else -> VerdeExito
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, CelesteBorde)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = CelesteOscuro,
                    modifier = Modifier.size(20.dp))
                Text("Tu situación hoy", fontWeight = FontWeight.Bold,
                    color = TextoOscuro, modifier = Modifier.weight(1f))
            }

            StatRow("Categoría actual", "Monotributo ${resumen.categoriaActual.letra}",
                CelesteOscuro)
            StatRow("Cuota mensual", CurrencyFormatter.formatear(cuota), CelesteOscuro)
            StatRow("Facturado este año", CurrencyFormatter.formatear(resumen.totalIngresos))
            StatRow("Límite anual", CurrencyFormatter.formatearCompacto(resumen.categoriaActual.limiteAnual))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Uso del límite", color = GrisMedio)
                    Text("${(resumen.porcentajeDelLimite * 100).toInt()}%",
                        fontWeight = FontWeight.Bold, color = colorBarra)
                }
                LinearProgressIndicator(
                    progress = { resumen.porcentajeDelLimite.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                    color = colorBarra,
                    trackColor = GrisClaro
                )
            }
        }
    }
}

// ── CUOTA Y VENCIMIENTO ──────────────────────────────────────────────────────
@Composable
private fun CuotaVencimientoCard(resumen: ResumenMensual, esVentaMuebles: Boolean) {
    val cuota = resumen.categoriaActual.cuotaSegunRubro(esVentaMuebles)
    val proxima = CategoriaMonotributo.entries.getOrNull(
        CategoriaMonotributo.entries.indexOf(resumen.categoriaActual) + 1
    )
    val diaHoy = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
    }
    val proximoVenc = diaHoy.apply { set(Calendar.DAY_OF_MONTH, 20) }
    if (diaHoy.get(Calendar.DAY_OF_MONTH) > 20) {
        proximoVenc.add(Calendar.MONTH, 1)
    }
    val diasVenc = ((proximoVenc.timeInMillis - diaHoy.timeInMillis) / (1000L * 60 * 60 * 24)).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, if (diasVenc <= 5) NaranjaAlerta.copy(0.4f) else CelesteBorde)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.DateRange, null, tint = CelesteOscuro, modifier = Modifier.size(20.dp))
                Text("Próxima cuota", fontWeight = FontWeight.Bold, color = TextoOscuro,
                    modifier = Modifier.weight(1f))
                if (diasVenc <= 5) {
                    Box(
                        Modifier.clip(RoundedCornerShape(8.dp)).background(NaranjaAlerta.copy(0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("$diasVenc días", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            color = NaranjaAlerta)
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (diasVenc <= 5) NaranjaClaro else CelesteSuperficie)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text("Vence el", color = GrisMedio, style = MaterialTheme.typography.labelSmall)
                    Text(
                        SimpleDateFormat("dd 'de' MMM", Locale("es", "AR")).format(proximoVenc.time),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = if (diasVenc <= 5) NaranjaAlerta else CelesteOscuro
                    )
                }
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text("Cuota", color = GrisMedio, style = MaterialTheme.typography.labelSmall)
                    Text(CurrencyFormatter.formatear(cuota), fontWeight = FontWeight.Bold,
                        color = if (diasVenc <= 5) NaranjaAlerta else CelesteOscuro)
                }
            }

            if (proxima != null) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoChip("Próxima cat. ${proxima.letra}",
                        CurrencyFormatter.formatear(proxima.cuotaSegunRubro(esVentaMuebles)),
                        Modifier.weight(1f))
                    InfoChip("Diferencia",
                        CurrencyFormatter.formatear(proxima.cuotaSegunRubro(esVentaMuebles) - cuota),
                        Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, valor: String, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(10.dp)).background(GrisClaro).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = GrisMedio)
        Text(valor, fontWeight = FontWeight.Bold, color = TextoOscuro, fontSize = 14.sp)
    }
}

// ── HISTORIAL / TENDENCIA ────────────────────────────────────────────────────
@Composable
private fun HistorialCard(resumen: ResumenMensual) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, CelesteBorde)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.History, null, tint = CelesteOscuro, modifier = Modifier.size(20.dp))
                Text("Este mes", fontWeight = FontWeight.Bold, color = TextoOscuro)
            }

            StatRow("Mes", "${resumen.mes}/${resumen.anio}")
            StatRow("Ingresos del mes", CurrencyFormatter.formatear(resumen.totalIngresos))
            StatRow("Promedio permitido", CurrencyFormatter.formatear(
                resumen.categoriaActual.limiteAnual / 12
            ))

            val porcentajeMes = (resumen.totalIngresos / (resumen.categoriaActual.limiteAnual / 12)).coerceIn(0.0, 1.0)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Uso del promedio", color = GrisMedio)
                    Text("${(porcentajeMes * 100).toInt()}%", fontWeight = FontWeight.Bold,
                        color = if (porcentajeMes >= 0.8) NaranjaAlerta else VerdeExito)
                }
                LinearProgressIndicator(
                    progress = { porcentajeMes.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = if (porcentajeMes >= 0.8f) NaranjaAlerta else VerdeExito,
                    trackColor = GrisClaro
                )
            }
        }
    }
}

// ── RUBRO ────────────────────────────────────────────────────────────────────
@Composable
private fun RubroCard(esVentaMuebles: Boolean, onChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, CelesteBorde)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Store, null, tint = CelesteOscuro, modifier = Modifier.size(20.dp))
                Text("Rubro", fontWeight = FontWeight.Bold, color = TextoOscuro)
            }
            Text(
                "Servicios y venta de muebles pagan cuotas diferentes a partir de la categoría C.",
                style = MaterialTheme.typography.bodySmall,
                color = GrisMedio
            )
            RubroOption("Locaciones / Prestación de servicios", !esVentaMuebles) {
                onChange(false)
            }
            RubroOption("Venta de cosas muebles", esVentaMuebles) { onChange(true) }
        }
    }
}

@Composable
private fun RubroOption(titulo: String, seleccionado: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (seleccionado) CelesteSuperficie else BlancoArgentino)
            .border(
                width = if (seleccionado) 2.dp else 1.dp,
                color = if (seleccionado) CelesteOscuro else CelesteBorde,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = seleccionado,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = CelesteOscuro)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            titulo,
            color = if (seleccionado) CelesteOscuro else TextoOscuro,
            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
    }
}

// ── ROL TOGGLE ───────────────────────────────────────────────────────────────
@Composable
private fun RolToggleCard(actual: UserRole, onSeleccion: (UserRole) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, CelesteBorde)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.SwapHoriz, null, tint = CelesteOscuro,
                    modifier = Modifier.size(20.dp))
                Text("Modo de uso", fontWeight = FontWeight.Bold, color = TextoOscuro,
                    modifier = Modifier.weight(1f))
                Box(
                    Modifier.clip(RoundedCornerShape(8.dp))
                        .background(if (actual == UserRole.CONTADOR) SolAmarillo else CelesteOscuro)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(actual.label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = if (actual == UserRole.CONTADOR) TextoOscuro else BlancoArgentino)
                }
            }
            Text(
                "Cambiá entre vista personal (simple) y vista contador (con clientes y reportes).",
                style = MaterialTheme.typography.bodySmall,
                color = GrisMedio
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                UserRole.entries.forEach { r ->
                    val sel = r == actual
                    val icono = if (r == UserRole.PERSONAL) Icons.Default.Person else Icons.Default.Business
                    OutlinedCard(
                        modifier = Modifier.weight(1f).clickable { onSeleccion(r) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (sel) CelesteOscuro.copy(alpha = 0.1f) else BlancoArgentino
                        ),
                        border = BorderStroke(
                            if (sel) 2.dp else 1.dp,
                            if (sel) CelesteOscuro else CelesteBorde
                        )
                    ) {
                        Column(
                            Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(icono, null, tint = if (sel) CelesteOscuro else GrisMedio)
                            Text(r.label, fontWeight = FontWeight.Bold,
                                color = if (sel) CelesteOscuro else TextoOscuro)
                        }
                    }
                }
            }
        }
    }
}

// ── OVERRIDE CATEGORÍA ───────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoriaOverrideCard(
    actual: String?,
    onSeleccion: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val activo = actual != null
    val labelActual = actual?.let { "Categoría $it (forzada)" } ?: "Automática (según facturación)"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, if (activo) SolAmarillo else CelesteBorde)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Science, null, tint = if (activo) SolAmarillo else CelesteOscuro,
                    modifier = Modifier.size(20.dp))
                Text("Modo prueba", fontWeight = FontWeight.Bold, color = TextoOscuro,
                    modifier = Modifier.weight(1f))
                if (activo) {
                    Box(
                        Modifier.clip(RoundedCornerShape(8.dp)).background(SolAmarillo)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("ACTIVO", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = TextoOscuro)
                    }
                }
            }
            Text(
                "Forzá una categoría A–K para ver cómo cambian límites, cuota, alertas y UI.",
                style = MaterialTheme.typography.bodySmall,
                color = GrisMedio
            )

            ExposedDropdownMenuBox(expanded = expanded,
                onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = labelActual,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría visible") },
                    trailingIcon = { Icon(Icons.Default.ExpandMore, null) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CelesteOscuro,
                        focusedLabelColor = CelesteOscuro
                    )
                )
                ExposedDropdownMenu(expanded = expanded,
                    onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Automática (según facturación)") },
                        onClick = { onSeleccion(null); expanded = false }
                    )
                    HorizontalDivider()
                    CategoriaMonotributo.entries.forEach { cat ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Categoría ${cat.letra}  •  hasta " +
                                        CurrencyFormatter.formatearCompacto(cat.limiteAnual)
                                )
                            },
                            onClick = { onSeleccion(cat.letra); expanded = false }
                        )
                    }
                }
            }

            if (activo) {
                TextButton(onClick = { onSeleccion(null) }) {
                    Text("Volver a automática", color = CelesteOscuro, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── DATOS PERSONALES ─────────────────────────────────────────────────────────
@Composable
private fun DatosPersonalesCard(
    nombre: String,
    cuit: String,
    editando: Boolean,
    guardado: Boolean,
    onEditarClick: () -> Unit,
    onNombreChange: (String) -> Unit,
    onCuitChange: (String) -> Unit,
    onGuardarClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, CelesteBorde)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Person, null, tint = CelesteOscuro,
                        modifier = Modifier.size(20.dp))
                    Text("Datos personales", fontWeight = FontWeight.Bold,
                        color = TextoOscuro)
                }
                if (!editando) {
                    TextButton(onClick = onEditarClick) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp),
                            tint = CelesteOscuro)
                        Spacer(Modifier.width(4.dp))
                        Text("Editar", color = CelesteOscuro, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (editando) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = onNombreChange,
                    label = { Text("Nombre / Razón social") },
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = CelesteOscuro) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CelesteOscuro,
                        focusedLabelColor = CelesteOscuro,
                        cursorColor = CelesteOscuro
                    )
                )
                OutlinedTextField(
                    value = cuit,
                    onValueChange = onCuitChange,
                    label = { Text("CUIT") },
                    leadingIcon = { Icon(Icons.Default.Badge, null, tint = CelesteOscuro) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CelesteOscuro,
                        focusedLabelColor = CelesteOscuro,
                        cursorColor = CelesteOscuro
                    )
                )
                Button(
                    onClick = onGuardarClick,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CelesteOscuro)
                ) {
                    Icon(Icons.Default.Check, null, tint = BlancoArgentino, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Guardar", fontWeight = FontWeight.Bold, color = BlancoArgentino)
                }
            } else {
                LabeledValue("Nombre", nombre.ifBlank { "—" })
                LabeledValue("CUIT", cuit.ifBlank { "—" })
                if (guardado) {
                    Text("✓ Guardado", style = MaterialTheme.typography.bodySmall,
                        color = VerdeExito, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun LabeledValue(label: String, valor: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, color = GrisMedio, style = MaterialTheme.typography.labelSmall)
        Text(valor, color = TextoOscuro, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

// ── HERRAMIENTAS ─────────────────────────────────────────────────────────────
@Composable
private fun ToolItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    destructivo: Boolean = false,
    visible: Boolean = true
) {
    if (!visible) return
    val tint = if (destructivo) RojoMedio else CelesteOscuro
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, CelesteBorde)
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold,
                    color = if (destructivo) RojoMedio else TextoOscuro)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = GrisMedio)
            }
            Icon(Icons.Default.ChevronRight, null, tint = GrisMedio, modifier = Modifier.size(20.dp))
        }
    }
}

// ── SEGURIDAD Y SESIÓN ───────────────────────────────────────────────────────
@Composable
private fun SeguridadCard(
    expandido: Boolean,
    onExpandClick: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, CelesteBorde)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.fillMaxWidth().clickable { onExpandClick() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Lock, null, tint = CelesteOscuro, modifier = Modifier.size(20.dp))
                Text("Seguridad y sesión", fontWeight = FontWeight.Bold, color = TextoOscuro,
                    modifier = Modifier.weight(1f))
                Icon(
                    if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null,
                    tint = GrisMedio
                )
            }

            AnimatedVisibility(
                visible = expandido,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HorizontalDivider(color = GrisClaro)
                    InfoAuditoria()
                    Button(
                        onClick = onCerrarSesion,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RojoMedio)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = BlancoArgentino,
                            modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Cerrar sesión", fontWeight = FontWeight.Bold, color = BlancoArgentino)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoAuditoria() {
    val hoy = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "AR")).format(Date())
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Info, null, tint = AzulInfo, modifier = Modifier.size(16.dp))
            Text("Sesión activa desde hoy", style = MaterialTheme.typography.bodySmall,
                color = GrisMedio)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Watch, null, tint = AzulInfo, modifier = Modifier.size(16.dp))
            Text(hoy, style = MaterialTheme.typography.bodySmall, color = TextoOscuro)
        }
    }
}

// ── TARIFAS REMOTAS ──────────────────────────────────────────────────────────
@Composable
private fun TarifasRemotasCard() {
    val context = LocalContext.current
    val repo = remember { RemoteConfigHolder.get(context) }
    val config by repo.config.collectAsStateWithLifecycle()
    val ts by repo.ultimaActualizacion.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, CelesteBorde)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.CloudDownload, null, tint = CelesteOscuro,
                    modifier = Modifier.size(20.dp))
                Text("Tarifas y actualizaciones", fontWeight = FontWeight.Bold, color = TextoOscuro,
                    modifier = Modifier.weight(1f))
            }
            Text(
                "Versión ${config.version}  •  vigencia ${config.actualizadoEl.ifBlank { "—" }}",
                style = MaterialTheme.typography.bodySmall,
                color = GrisMedio
            )
            if (ts > 0) {
                val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "AR")).format(Date(ts))
                Text("Última verificación: $fecha", style = MaterialTheme.typography.labelSmall,
                    color = GrisMedio)
            }
            mensaje?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = CelesteOscuro,
                    fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = {
                    cargando = true
                    mensaje = null
                    scope.launch {
                        when (val r = repo.refrescar()) {
                            is RemoteConfigRepository.RefreshResult.Updated ->
                                mensaje = "✓ Actualizado a la versión ${r.nuevaVersion}"
                            RemoteConfigRepository.RefreshResult.NoChanges ->
                                mensaje = "Ya estás en la última versión"
                            is RemoteConfigRepository.RefreshResult.Error ->
                                mensaje = "No se pudo actualizar (${r.mensaje})"
                        }
                        cargando = false
                    }
                },
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CelesteOscuro)
            ) {
                if (cargando) {
                    CircularProgressIndicator(color = BlancoArgentino, strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp))
                } else {
                    Text("Buscar actualizaciones", fontWeight = FontWeight.Bold, color = BlancoArgentino)
                }
            }
        }
    }
}

// ── INFORMACIÓN LEGAL ────────────────────────────────────────────────────────
@Composable
private fun InformacionLegalCard() {
    var expandido by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, CelesteBorde)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.fillMaxWidth().clickable { expandido = !expandido },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Info, null, tint = CelesteOscuro, modifier = Modifier.size(20.dp))
                Text("Información legal", fontWeight = FontWeight.Bold, color = TextoOscuro,
                    modifier = Modifier.weight(1f))
                Icon(if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null,
                    tint = GrisMedio)
            }

            AnimatedVisibility(visible = expandido,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider(color = GrisClaro)
                    Text("Términos y condiciones", style = MaterialTheme.typography.bodySmall,
                        color = CelesteOscuro, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { })
                    Text("Política de privacidad", style = MaterialTheme.typography.bodySmall,
                        color = CelesteOscuro, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { })
                    HorizontalDivider(color = GrisClaro)
                    Text("App: Monotributo Al Día v1.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = GrisMedio, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                    Text("© 2026 - Código abierto",
                        style = MaterialTheme.typography.labelSmall,
                        color = GrisMedio, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, valor: String, valorColor: Color = TextoOscuro) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = GrisMedio, style = MaterialTheme.typography.bodyMedium)
        Text(valor, color = valorColor, fontWeight = FontWeight.Bold)
    }
}

// ── ESTADÍSTICAS ─────────────────────────────────────────────────────────────
@Composable
private fun EstadisticasCard(resumen: ResumenMensual) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, CelesteBorde)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.BarChart, null, tint = CelesteOscuro, modifier = Modifier.size(20.dp))
                Text("Estadísticas", fontWeight = FontWeight.Bold, color = TextoOscuro)
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatisticaChip("Mes", "${resumen.mes}", CelesteOscuro, Modifier.weight(1f))
                StatisticaChip("Promedio/día", CurrencyFormatter.formatearCompacto(
                    resumen.totalIngresos / 30.0
                ), VerdeExito, Modifier.weight(1f))
                StatisticaChip("Año", resumen.anio.toString(), SolAmarillo, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatisticaChip(label: String, valor: String, color: Color, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = GrisMedio)
        Text(valor, fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
    }
}

// ── NOTIFICACIONES ───────────────────────────────────────────────────────────
@Composable
private fun NotificacionesCard(
    expandido: Boolean,
    onExpandClick: () -> Unit,
    notificacionesActivas: Boolean,
    onToggleNotif: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, CelesteBorde)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.fillMaxWidth().clickable { onExpandClick() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.NotificationsActive, null, tint = CelesteOscuro, modifier = Modifier.size(20.dp))
                Text("Notificaciones", fontWeight = FontWeight.Bold, color = TextoOscuro,
                    modifier = Modifier.weight(1f))
                Switch(
                    checked = notificacionesActivas,
                    onCheckedChange = onToggleNotif,
                    modifier = Modifier.scale(0.8f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = VerdeExito,
                        checkedTrackColor = VerdeExito.copy(alpha = 0.3f)
                    )
                )
            }

            AnimatedVisibility(
                visible = expandido,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HorizontalDivider(color = GrisClaro)
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()) {
                        Checkbox(checked = notificacionesActivas, onCheckedChange = onToggleNotif,
                            colors = CheckboxDefaults.colors(checkedColor = CelesteOscuro))
                        Column(Modifier.weight(1f)) {
                            Text("Alertas de vencimiento", fontWeight = FontWeight.SemiBold, color = TextoOscuro)
                            Text("Te recordamos 5 días antes", style = MaterialTheme.typography.bodySmall, color = GrisMedio)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()) {
                        Checkbox(checked = true, onCheckedChange = { },
                            colors = CheckboxDefaults.colors(checkedColor = CelesteOscuro))
                        Column(Modifier.weight(1f)) {
                            Text("Cambios de categoría", fontWeight = FontWeight.SemiBold, color = TextoOscuro)
                            Text("Cuando subes o bajas de categoría", style = MaterialTheme.typography.bodySmall, color = GrisMedio)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()) {
                        Checkbox(checked = true, onCheckedChange = { },
                            colors = CheckboxDefaults.colors(checkedColor = CelesteOscuro))
                        Column(Modifier.weight(1f)) {
                            Text("Actualizaciones de tarifas", fontWeight = FontWeight.SemiBold, color = TextoOscuro)
                            Text("Nuevas categorías o cambios AFIP", style = MaterialTheme.typography.bodySmall, color = GrisMedio)
                        }
                    }
                }
            }
        }
    }
}


// ── RESPALDO EN NUBE ─────────────────────────────────────────────────────────
@Composable
private fun RespaldoCard(expandido: Boolean, onExpandClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, CelesteBorde)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.fillMaxWidth().clickable { onExpandClick() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Cloud, null, tint = VerdeExito, modifier = Modifier.size(20.dp))
                Text("Respaldo en nube", fontWeight = FontWeight.Bold, color = TextoOscuro,
                    modifier = Modifier.weight(1f))
                Box(
                    Modifier.clip(RoundedCornerShape(6.dp)).background(VerdeExito.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Sincronizado", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VerdeExito)
                }
                Icon(if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null,
                    tint = GrisMedio)
            }

            AnimatedVisibility(
                visible = expandido,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HorizontalDivider(color = GrisClaro)
                    Text("✓ Todos tus datos están sincronizados en Firestore",
                        style = MaterialTheme.typography.bodySmall, color = VerdeExito)
                    Text("Última sincronización: hace 2 minutos",
                        style = MaterialTheme.typography.labelSmall, color = GrisMedio)
                    Button(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VerdeExito)
                    ) {
                        Icon(Icons.Default.CloudUpload, null, tint = BlancoArgentino, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Forzar sincronización", fontWeight = FontWeight.Bold, color = BlancoArgentino, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ── SOPORTE Y CONTACTO ───────────────────────────────────────────────────────
@Composable
private fun SoporteCard(onContactoClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, CelesteBorde)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Support, null, tint = CelesteOscuro, modifier = Modifier.size(20.dp))
                Text("Soporte y contacto", fontWeight = FontWeight.Bold, color = TextoOscuro)
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ContactoBtn("📧 Email", "soporte@app.com", CelesteOscuro, Modifier.weight(1f))
                ContactoBtn("💬 Chat", "Discord", CelesteOscuro, Modifier.weight(1f))
                ContactoBtn("❓ FAQ", "Ver preguntas", CelesteOscuro, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ContactoBtn(label: String, valor: String, color: Color, modifier: Modifier) {
    OutlinedButton(
        onClick = { },
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
            Text(valor, fontSize = 8.sp, color = GrisMedio)
        }
    }
}

// ── CRÉDITOS Y DONACIONES ────────────────────────────────────────────────────
@Composable
private fun CreditosCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
        border = BorderStroke(1.dp, SolAmarillo.copy(alpha = 0.4f))
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.FavoriteBorder, null, tint = SolAmarillo, modifier = Modifier.size(20.dp))
                Text("¿Te gusta la app?", fontWeight = FontWeight.Bold, color = TextoOscuro)
            }

            Text(
                "Si esta app te ayuda, considera dejar una propina o reseña en Play Store ❤️",
                style = MaterialTheme.typography.bodySmall,
                color = GrisMedio
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SolAmarillo)
                ) {
                    Icon(Icons.Default.Favorite, null, tint = SolAmarillo, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Cafecito", color = SolAmarillo, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SolAmarillo)
                ) {
                    Icon(Icons.Default.Star, null, tint = TextoOscuro, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reseña", color = TextoOscuro, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}


