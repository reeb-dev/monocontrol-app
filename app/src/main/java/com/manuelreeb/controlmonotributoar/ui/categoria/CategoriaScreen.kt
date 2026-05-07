package com.reeb.controlmonotributoar.ui.categoria

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.reeb.controlmonotributoar.data.local.entity.ClienteEntity
import com.reeb.controlmonotributoar.domain.model.CategoriaMonotributo
import com.reeb.controlmonotributoar.utils.ArcaInfo
import com.reeb.controlmonotributoar.utils.CurrencyFormatter

private val Celeste     = Color(0xFF75AADB)
private val CelesteOsc  = Color(0xFF4A86C8)
private val Amarillo    = Color(0xFFFBB81C)
private val Blanco      = Color(0xFFFFFFFF)
private val BlancoSuave = Color(0xFFF0F6FF)
private val GrisCeleste = Color(0xFFCCDFF4)
private val TextoOscuro = Color(0xFF0D2A4A)
private val TextoSuave  = Color(0xFF6B7B8C)
private val Verde       = Color(0xFF2E7D32)
private val Rojo        = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriaScreen(viewModel: CategoriaViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val clientes by viewModel.clientes.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    // ── Modal de detalle de categoría ─────────────────────────────────
    var categoriaDetalle by remember { mutableStateOf<CategoriaMonotributo?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    categoriaDetalle?.let { cat ->
        val esActualSheet = estado?.categoriaActual == cat
        ModalBottomSheet(
            onDismissRequest = { categoriaDetalle = null },
            sheetState = sheetState,
            containerColor = Blanco
        ) {
            CategoriaDetalleSheet(
                cat = cat,
                esActual = esActualSheet,
                facturacionAnual = estado?.facturacionAnual ?: 0.0,
                rubroActual = estado?.esVentaMuebles == true,
                esPerfilPersonal = estado?.esPerfilPersonal != false,
                nombreDestino = estado?.nombreCliente.orEmpty(),
                onAplicar = { letra, esVentaMuebles ->
                    viewModel.aplicarCambios(letra, esVentaMuebles)
                    categoriaDetalle = null
                },
                onCerrar = { categoriaDetalle = null }
            )
        }
    }
    // ──────────────────────────────────────────────────────────────────

    Scaffold(
        containerColor = BlancoSuave,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        if (estado == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Celeste)
            }
            return@Scaffold
        }
        val s = estado!!

        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding      = PaddingValues(vertical = 16.dp)
        ) {

            // ── CARD CATEGORÍA ACTUAL ─────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(18.dp),
                    colors   = CardDefaults.cardColors(containerColor = Celeste)
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "📋  Categoría visible",
                            style = MaterialTheme.typography.labelLarge,
                            color = Blanco.copy(alpha = 0.85f)
                        )
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "Categoría ${s.categoriaActual.letra}",
                                style      = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color      = Blanco
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (s.esOverride) Amarillo else Blanco.copy(alpha = 0.25f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    if (s.esOverride) "Forzada" else "Automática",
                                    style      = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color      = if (s.esOverride) TextoOscuro else Blanco
                                )
                            }
                        }
                        if (s.nombreCliente.isNotBlank()) {
                            Text(
                                "Cliente: ${s.nombreCliente}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Blanco.copy(alpha = 0.9f)
                            )
                        }
                        HorizontalDivider(color = Blanco.copy(alpha = 0.3f))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Facturado:", color = Blanco.copy(alpha = 0.9f))
                            Text(
                                CurrencyFormatter.formatear(s.facturacionAnual),
                                fontWeight = FontWeight.Bold, color = Blanco
                            )
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Límite anual:", color = Blanco.copy(alpha = 0.9f))
                            Text(CurrencyFormatter.formatear(s.categoriaActual.limiteAnual), color = Blanco)
                        }
                        LinearProgressIndicator(
                            progress   = { s.porcentaje.coerceIn(0f, 1f) },
                            modifier   = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color      = when {
                                s.porcentaje >= 0.9f -> Rojo
                                s.porcentaje >= 0.7f -> Amarillo
                                else                 -> Blanco
                            },
                            trackColor = Blanco.copy(alpha = 0.3f)
                        )
                        Text(
                            "${(s.porcentaje * 100).toInt()}% del límite  •  " +
                            "Te quedan ${CurrencyFormatter.formatearCompacto(s.margenRestante)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Blanco.copy(alpha = 0.9f)
                        )
                        if (s.esOverride) {
                            TextButton(
                                onClick = { viewModel.setCategoria(null) },
                                colors = ButtonDefaults.textButtonColors(contentColor = Amarillo)
                            ) {
                                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Volver a categoría automática", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // ── BUSCADOR DE CLIENTES ──────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(containerColor = Blanco),
                    border   = BorderStroke(1.dp, GrisCeleste)
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp, 22.dp)
                                    .background(Amarillo, RoundedCornerShape(2.dp))
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Buscar caso de cliente",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextoOscuro
                            )
                        }
                        OutlinedTextField(
                            value = query,
                            onValueChange = viewModel::onQueryChange,
                            placeholder = { Text("Buscar por nombre o CUIT") },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = CelesteOsc) },
                            trailingIcon = {
                                if (query.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                                        Icon(Icons.Default.Close, null)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CelesteOsc,
                                cursorColor = CelesteOsc
                            )
                        )
                        when {
                            clientes.isEmpty() && query.isBlank() -> Text(
                                "Aún no guardaste clientes. Creálos desde Perfil → Clientes guardados.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoSuave
                            )
                            clientes.isEmpty() -> Text(
                                "Sin resultados para \"$query\".",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoSuave
                            )
                            else -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                clientes.take(5).forEach { cli ->
                                    ClienteMiniRow(cli) {
                                        viewModel.aplicarCliente(cli)
                                    }
                                }
                                if (clientes.size > 5) {
                                    Text(
                                        "+ ${clientes.size - 5} más… afiná la búsqueda",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextoSuave
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── INFO FISCAL ────────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(containerColor = Blanco),
                    border   = BorderStroke(1.5.dp, GrisCeleste)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp, 22.dp)
                                    .background(Amarillo, RoundedCornerShape(2.dp))
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Información fiscal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextoOscuro
                            )
                        }
                        Text(
                            "Valores ARCA vigentes desde 01/02/2026",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSuave
                        )
                        Text(
                            "Referencia oficial: afip.gob.ar/monotributo/categorias.asp",
                            style = MaterialTheme.typography.labelSmall,
                            color = CelesteOsc
                        )
                        InfoRow("💰", "Cuota mensual", CurrencyFormatter.formatear(s.categoriaActual.cuotaMensual))
                        InfoRow("📅", "Próxima recategorización", "Enero / Julio")
                        InfoRow("⚠️", "Alerta de límite al", "80% → ${CurrencyFormatter.formatearCompacto(s.categoriaActual.limiteAnual * 0.8)}")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (s.porcentaje >= 0.9f) Rojo.copy(alpha = 0.1f) else Verde.copy(alpha = 0.08f))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (s.porcentaje >= 0.9f) "🔴" else if (s.porcentaje >= 0.7f) "🟡" else "🟢",
                                fontSize = 20.sp
                            )
                            Text(
                                if (s.porcentaje >= 0.9f) "⚡ Cerca del límite — revisar urgente"
                                else if (s.porcentaje >= 0.7f) "Atención — monitorear facturación"
                                else "Facturación en orden",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = if (s.porcentaje >= 0.9f) Rojo else TextoOscuro
                            )
                        }

                        TextButton(
                            onClick = { abrirUrlArca(context) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.OpenInBrowser, null, modifier = Modifier.size(16.dp), tint = CelesteOsc)
                            Spacer(Modifier.width(4.dp))
                            Text("Ver fuente oficial en ARCA", fontWeight = FontWeight.SemiBold, color = CelesteOsc)
                        }
                    }
                }
            }

            // ── TABLA TODAS LAS CATEGORÍAS (clickeable) ───────────────
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(4.dp, 22.dp).background(Celeste, RoundedCornerShape(2.dp)))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Tocá una categoría para ver el detalle",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextoOscuro
                    )
                }
            }

            items(s.todasCategorias) { cat ->
                CategoriaRow(
                    cat = cat,
                    esActual = cat == s.categoriaActual,
                    onClick = { categoriaDetalle = cat }
                )
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun InfoRow(emoji: String, label: String, valor: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 16.sp)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TextoOscuro.copy(alpha = 0.7f))
        }
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextoOscuro)
    }
}

@Composable
private fun ClienteMiniRow(cliente: ClienteEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(BlancoSuave)
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Celeste.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PersonOutline, null, tint = CelesteOsc, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(cliente.nombre, fontWeight = FontWeight.SemiBold, color = TextoOscuro, fontSize = 14.sp)
            Text(
                buildString {
                    append(if (cliente.cuit.isBlank()) "Sin CUIT" else "CUIT ${cliente.cuit}")
                    append("  •  ")
                    append(cliente.categoria?.let { "Cat. $it" } ?: "Auto")
                    if (cliente.esVentaMuebles) append("  •  Muebles")
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextoSuave
            )
        }
        Icon(Icons.Default.Check, "Cargar", tint = CelesteOsc)
    }
}

@Composable
private fun CategoriaRow(
    cat: CategoriaMonotributo,
    esActual: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (esActual) Celeste else Blanco
        ),
        border = if (!esActual) BorderStroke(1.dp, GrisCeleste) else null
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (esActual) Blanco.copy(alpha = 0.25f) else GrisCeleste),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    cat.letra,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 18.sp,
                    color      = if (esActual) Blanco else CelesteOsc
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Categoría ${cat.letra}",
                    fontWeight = if (esActual) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color      = if (esActual) Blanco else TextoOscuro
                )
                Text(
                    "Servicios: ${CurrencyFormatter.formatear(cat.cuotaMensual)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (esActual) Blanco.copy(alpha = 0.85f) else TextoOscuro.copy(alpha = 0.6f)
                )
                Text(
                    "Muebles: ${CurrencyFormatter.formatear(cat.cuotaVentaMuebles)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (esActual) Blanco.copy(alpha = 0.85f) else TextoOscuro.copy(alpha = 0.6f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "hasta ${CurrencyFormatter.formatearCompacto(cat.limiteAnual)}",
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color      = if (esActual) Amarillo else CelesteOsc
                )
                if (esActual) {
                    Text(
                        "✓ activa",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Amarillo
                    )
                } else {
                    Text(
                        "ver detalle ›",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoSuave
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
//  MODAL DETALLE DE CATEGORÍA
// ─────────────────────────────────────────────────────────────────────
@Composable
private fun CategoriaDetalleSheet(
    cat: CategoriaMonotributo,
    esActual: Boolean,
    facturacionAnual: Double,
    rubroActual: Boolean,
    esPerfilPersonal: Boolean,
    nombreDestino: String,
    onAplicar: (letra: String, esVentaMuebles: Boolean) -> Unit,
    onCerrar: () -> Unit
) {
    val proyeccion = (facturacionAnual / cat.limiteAnual).toFloat().coerceIn(0f, 1.5f)
    val cabe = facturacionAnual <= cat.limiteAnual
    val margen = (cat.limiteAnual - facturacionAnual).coerceAtLeast(0.0)

    // Estado editable dentro del modal
    var rubroMuebles by remember(rubroActual) { mutableStateOf(rubroActual) }
    val cuotaSegunRubro = if (rubroMuebles) cat.cuotaVentaMuebles else cat.cuotaMensual
    val huboCambios = !esActual || rubroMuebles != rubroActual

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header con letra grande
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Celeste),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    cat.letra,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    color = Blanco
                )
            }
            Column(Modifier.weight(1f)) {
                Text("Categoría ${cat.letra}", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = TextoOscuro)
                Text(
                    "Monotributo · vigente según configuración activa",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSuave
                )
            }
            if (esActual) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Verde.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Activa", fontWeight = FontWeight.Bold, color = Verde, fontSize = 12.sp)
                }
            }
        }

        // Banner de destino (a quién se aplican los cambios)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (esPerfilPersonal) Celeste.copy(alpha = 0.10f)
                    else Amarillo.copy(alpha = 0.15f)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (esPerfilPersonal) "👤" else "🧾", fontSize = 18.sp)
                Column(Modifier.weight(1f)) {
                    Text(
                        if (esPerfilPersonal) "Aplicará a: tu perfil personal"
                        else "Aplicará al cliente",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextoOscuro
                    )
                    if (!esPerfilPersonal && nombreDestino.isNotBlank()) {
                        Text(
                            nombreDestino,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoOscuro.copy(alpha = 0.85f)
                        )
                    } else if (esPerfilPersonal && nombreDestino.isNotBlank()) {
                        Text(
                            nombreDestino,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoOscuro.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = GrisCeleste)

        // ── Selector de rubro ──
        SeccionTitulo("🏷️ Rubro de la actividad")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(BlancoSuave)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RubroChip(
                texto = "Servicios",
                seleccionado = !rubroMuebles,
                modifier = Modifier.weight(1f),
                onClick = { rubroMuebles = false }
            )
            RubroChip(
                texto = "Venta de muebles",
                seleccionado = rubroMuebles,
                modifier = Modifier.weight(1f),
                onClick = { rubroMuebles = true }
            )
        }
        Text(
            "Cuota según rubro elegido: ${CurrencyFormatter.formatear(cuotaSegunRubro)}/mes",
            style = MaterialTheme.typography.bodySmall,
            color = CelesteOsc,
            fontWeight = FontWeight.SemiBold
        )

        HorizontalDivider(color = GrisCeleste)

        val parametrosArca = parametrosArca(cat)

        // Datos económicos
        SeccionTitulo("💵 Topes e ingresos")
        DatoFila("Ingresos brutos máx. anual", CurrencyFormatter.formatear(cat.limiteAnual))
        DatoFila(
            "Ingresos brutos máx. mensual (1/12)",
            CurrencyFormatter.formatear(cat.limiteAnual / 12.0)
        )
        DatoFila("Alquileres devengados anuales", CurrencyFormatter.formatear(parametrosArca.alquileresDevengadosAnuales))
        DatoFila("Precio unitario máximo", CurrencyFormatter.formatear(parametrosArca.precioUnitarioMaxVentaMuebles))
        DatoFila(
            "Alerta al 80% del tope",
            CurrencyFormatter.formatear(cat.limiteAnual * 0.80),
            color = Amarillo
        )

        HorizontalDivider(color = GrisCeleste)

        // Cuotas
        SeccionTitulo("📑 Cuota mensual a pagar")
        DatoFila(
            "Locaciones / servicios",
            "${CurrencyFormatter.formatear(cat.cuotaMensual)}/mes",
            destaque = !rubroMuebles
        )
        DatoFila(
            "Venta de cosas muebles",
            "${CurrencyFormatter.formatear(cat.cuotaVentaMuebles)}/mes",
            destaque = rubroMuebles
        )
        DatoFila(
            "Impuesto integrado",
            CurrencyFormatter.formatear(
                if (rubroMuebles) parametrosArca.impuestoIntegradoVentaMuebles
                else parametrosArca.impuestoIntegradoServicios
            )
        )
        DatoFila("Aporte SIPA", CurrencyFormatter.formatear(parametrosArca.aporteSipa))
        DatoFila("Aporte obra social", CurrencyFormatter.formatear(parametrosArca.aporteObraSocial))
        Text(
            "Incluye impuesto integrado + aportes al SIPA + obra social.",
            style = MaterialTheme.typography.bodySmall,
            color = TextoSuave
        )

        HorizontalDivider(color = GrisCeleste)

        // Parámetros físicos (orientativos)
        SeccionTitulo("📐 Parámetros físicos (referencia ARCA)")
        DatoFila("Superficie afectada", parametroSuperficie(cat))
        DatoFila("Energía eléctrica anual", parametroEnergia(cat))
        Text(
            "Estos parámetros no se aplican en localidades de menos de 40.000 habitantes.",
            style = MaterialTheme.typography.bodySmall,
            color = TextoSuave
        )

        HorizontalDivider(color = GrisCeleste)

        // Comparativa con tu facturación
        if (facturacionAnual > 0.0) {
            SeccionTitulo("📊 Tu situación con esta categoría")
            LinearProgressIndicator(
                progress = { proyeccion.coerceAtMost(1f) },
                modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                color = if (cabe) Verde else Rojo,
                trackColor = GrisCeleste
            )
            Text(
                buildString {
                    append("Llevás facturado ")
                    append(CurrencyFormatter.formatear(facturacionAnual))
                    append(" — ")
                    append("${(proyeccion * 100).toInt()}% del tope")
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextoOscuro
            )
            if (cabe) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Verde.copy(alpha = 0.10f))
                        .padding(10.dp)
                ) {
                    Text(
                        "✅ Tu facturación entra en esta categoría. Te quedan ${CurrencyFormatter.formatear(margen)} de margen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Verde,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Rojo.copy(alpha = 0.10f))
                        .padding(10.dp)
                ) {
                    Text(
                        "⛔ Tu facturación supera el límite de esta categoría en ${CurrencyFormatter.formatear(facturacionAnual - cat.limiteAnual)}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Rojo,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // Botones de acción
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onCerrar,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextoOscuro),
                border = BorderStroke(1.dp, GrisCeleste)
            ) { Text("Cerrar", fontWeight = FontWeight.SemiBold) }

            Button(
                onClick = { onAplicar(cat.letra, rubroMuebles) },
                enabled = huboCambios,
                modifier = Modifier.weight(1.4f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Celeste,
                    disabledContainerColor = GrisCeleste
                )
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    when {
                        !huboCambios -> "Sin cambios"
                        esActual -> "Guardar cambios"
                        else -> "Aplicar categoría ${cat.letra}"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RubroChip(
    texto: String,
    seleccionado: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (seleccionado) Celeste else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (seleccionado) Blanco else TextoSuave
        )
    }
}

@Composable
private fun SeccionTitulo(texto: String) {
    Text(
        texto,
        fontWeight = FontWeight.Bold,
        color = TextoOscuro,
        fontSize = 14.sp
    )
}

@Composable
private fun DatoFila(label: String, valor: String, color: Color = TextoOscuro, destaque: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSuave,
            modifier = Modifier.weight(1f)
        )
        Text(
            valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (destaque) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = color,
            fontSize = if (destaque) 15.sp else 14.sp
        )
    }
}

// Datos físicos según la tabla oficial ARCA
private fun parametroSuperficie(cat: CategoriaMonotributo): String = when (cat) {
    CategoriaMonotributo.A -> "Hasta 30 m²"
    CategoriaMonotributo.B -> "Hasta 45 m²"
    CategoriaMonotributo.C -> "Hasta 60 m²"
    CategoriaMonotributo.D -> "Hasta 85 m²"
    CategoriaMonotributo.E -> "Hasta 110 m²"
    CategoriaMonotributo.F -> "Hasta 150 m²"
    CategoriaMonotributo.G,
    CategoriaMonotributo.H,
    CategoriaMonotributo.I,
    CategoriaMonotributo.J,
    CategoriaMonotributo.K -> "Hasta 200 m²"
}

private fun parametroEnergia(cat: CategoriaMonotributo): String = when (cat) {
    CategoriaMonotributo.A -> "Hasta 3.330 kW"
    CategoriaMonotributo.B -> "Hasta 5.000 kW"
    CategoriaMonotributo.C -> "Hasta 6.700 kW"
    CategoriaMonotributo.D -> "Hasta 10.000 kW"
    CategoriaMonotributo.E -> "Hasta 13.000 kW"
    CategoriaMonotributo.F -> "Hasta 16.500 kW"
    CategoriaMonotributo.G,
    CategoriaMonotributo.H,
    CategoriaMonotributo.I,
    CategoriaMonotributo.J,
    CategoriaMonotributo.K -> "Hasta 20.000 kW"
}

private fun abrirUrlArca(context: android.content.Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ArcaInfo.CATEGORIAS))
    context.startActivity(intent)
}

private data class ParametrosArcaCategoria(
    val alquileresDevengadosAnuales: Double,
    val precioUnitarioMaxVentaMuebles: Double,
    val impuestoIntegradoServicios: Double,
    val impuestoIntegradoVentaMuebles: Double,
    val aporteSipa: Double,
    val aporteObraSocial: Double
)

private fun parametrosArca(cat: CategoriaMonotributo): ParametrosArcaCategoria = when (cat) {
    CategoriaMonotributo.A -> ParametrosArcaCategoria(
        alquileresDevengadosAnuales = 2_390_229.80,
        precioUnitarioMaxVentaMuebles = 613_492.31,
        impuestoIntegradoServicios = 4_780.46,
        impuestoIntegradoVentaMuebles = 4_780.46,
        aporteSipa = 15_616.17,
        aporteObraSocial = 21_990.11
    )
    CategoriaMonotributo.B -> ParametrosArcaCategoria(
        alquileresDevengadosAnuales = 2_390_229.80,
        precioUnitarioMaxVentaMuebles = 613_492.31,
        impuestoIntegradoServicios = 9_082.88,
        impuestoIntegradoVentaMuebles = 9_082.88,
        aporteSipa = 17_177.79,
        aporteObraSocial = 21_990.11
    )
    CategoriaMonotributo.C -> ParametrosArcaCategoria(
        alquileresDevengadosAnuales = 3_266_647.39,
        precioUnitarioMaxVentaMuebles = 613_492.31,
        impuestoIntegradoServicios = 15_616.17,
        impuestoIntegradoVentaMuebles = 14_341.38,
        aporteSipa = 18_895.57,
        aporteObraSocial = 21_990.11
    )
    CategoriaMonotributo.D -> ParametrosArcaCategoria(
        alquileresDevengadosAnuales = 3_266_647.39,
        precioUnitarioMaxVentaMuebles = 613_492.31,
        impuestoIntegradoServicios = 25_495.79,
        impuestoIntegradoVentaMuebles = 23_742.95,
        aporteSipa = 20_785.13,
        aporteObraSocial = 26_133.18
    )
    CategoriaMonotributo.E -> ParametrosArcaCategoria(
        alquileresDevengadosAnuales = 4_143_064.98,
        precioUnitarioMaxVentaMuebles = 613_492.31,
        impuestoIntegradoServicios = 47_804.60,
        impuestoIntegradoVentaMuebles = 37_924.98,
        aporteSipa = 22_863.64,
        aporteObraSocial = 31_869.73
    )
    CategoriaMonotributo.F -> ParametrosArcaCategoria(
        alquileresDevengadosAnuales = 4_143_064.98,
        precioUnitarioMaxVentaMuebles = 613_492.31,
        impuestoIntegradoServicios = 67_245.13,
        impuestoIntegradoVentaMuebles = 49_398.08,
        aporteSipa = 25_150.00,
        aporteObraSocial = 36_650.19
    )
    CategoriaMonotributo.G -> ParametrosArcaCategoria(
        alquileresDevengadosAnuales = 4_939_808.23,
        precioUnitarioMaxVentaMuebles = 613_492.31,
        impuestoIntegradoServicios = 122_379.76,
        impuestoIntegradoVentaMuebles = 61_189.87,
        aporteSipa = 35_210.00,
        aporteObraSocial = 39_518.47
    )
    CategoriaMonotributo.H -> ParametrosArcaCategoria(
        alquileresDevengadosAnuales = 7_170_689.39,
        precioUnitarioMaxVentaMuebles = 613_492.31,
        impuestoIntegradoServicios = 350_567.04,
        impuestoIntegradoVentaMuebles = 175_283.51,
        aporteSipa = 49_294.00,
        aporteObraSocial = 47_485.89
    )
    CategoriaMonotributo.I -> ParametrosArcaCategoria(
        alquileresDevengadosAnuales = 7_170_689.39,
        precioUnitarioMaxVentaMuebles = 613_492.31,
        impuestoIntegradoServicios = 697_150.35,
        impuestoIntegradoVentaMuebles = 278_860.14,
        aporteSipa = 69_011.60,
        aporteObraSocial = 58_640.31
    )
    CategoriaMonotributo.J -> ParametrosArcaCategoria(
        alquileresDevengadosAnuales = 7_170_689.39,
        precioUnitarioMaxVentaMuebles = 613_492.31,
        impuestoIntegradoServicios = 836_580.42,
        impuestoIntegradoVentaMuebles = 334_632.18,
        aporteSipa = 96_616.24,
        aporteObraSocial = 65_810.99
    )
    CategoriaMonotributo.K -> ParametrosArcaCategoria(
        alquileresDevengadosAnuales = 7_170_689.39,
        precioUnitarioMaxVentaMuebles = 613_492.31,
        impuestoIntegradoServicios = 1_171_212.59,
        impuestoIntegradoVentaMuebles = 390_404.20,
        aporteSipa = 135_262.74,
        aporteObraSocial = 75_212.57
    )
}
