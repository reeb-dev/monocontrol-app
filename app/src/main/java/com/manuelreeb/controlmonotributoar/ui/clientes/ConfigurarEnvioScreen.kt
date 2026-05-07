package com.reeb.controlmonotributoar.ui.clientes

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

private val Celeste     = Color(0xFF75AADB)
private val CelesteOsc  = Color(0xFF4A86C8)
private val Amarillo    = Color(0xFFFBB81C)
private val Blanco      = Color(0xFFFFFFFF)
private val BlancoSuave = Color(0xFFF0F6FF)
private val GrisCeleste = Color(0xFFCCDFF4)
private val TextoOscuro = Color(0xFF0D2A4A)
private val TextoSuave  = Color(0xFF6B7B8C)
private val Verde       = Color(0xFF25D366) // verde WhatsApp
private val Rojo        = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurarEnvioScreen(
    clienteId: Long,
    viewModel: ConfigurarEnvioViewModel,
    viewModelClientes: ClientesViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cliente by viewModel.cliente.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()

    LaunchedEffect(clienteId) { viewModel.cargar(clienteId) }

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    // ── Estado local de los campos ──────────────────────────────────────
    var asunto by remember { mutableStateOf("") }
    var mensajeBody by remember { mutableStateOf("") }
    var adjuntoUri by remember { mutableStateOf("") }
    var adjuntoNombre by remember { mutableStateOf("") }
    var envioAuto by remember { mutableStateOf(false) }
    var diaMes by remember { mutableStateOf(18) }
    var hora by remember { mutableStateOf(9) }
    var minuto by remember { mutableStateOf(0) }
    var whatsappNumero by remember { mutableStateOf("") }
    var whatsappActivo by remember { mutableStateOf(false) }
    var whatsappMensaje by remember { mutableStateOf("") }

    // Hidrata los campos cuando se carga el cliente.
    LaunchedEffect(cliente) {
        cliente?.let { c ->
            asunto = c.emailAsunto
            // Si hay borrador, se prefiere; sino el mensaje guardado.
            mensajeBody = if (c.emailBorrador.isNotBlank()) c.emailBorrador else c.emailMensaje
            adjuntoUri = c.emailAdjuntoUri
            adjuntoNombre = c.emailAdjuntoNombre
            envioAuto = c.envioAutomatico
            diaMes = c.envioDiaMes
            hora = c.envioHora
            minuto = c.envioMinuto
            whatsappNumero = c.whatsappNumero
            whatsappActivo = c.whatsappEnviar
            whatsappMensaje = c.whatsappMensaje
        }
    }

    // ── Picker de adjunto (PDF/imagen) ──────────────────────────────────
    val pickerAdjunto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            // Persistimos el permiso de lectura para usarlo después.
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            adjuntoUri = uri.toString()
            adjuntoNombre = uri.lastPathSegment?.substringAfterLast('/') ?: "archivo adjunto"
        }
    }

    // ── Diálogos de hora ───────────────────────────────────────────────
    var mostrarTimePicker by remember { mutableStateOf(false) }
    if (mostrarTimePicker) {
        val state = rememberTimePickerState(initialHour = hora, initialMinute = minuto, is24Hour = true)
        AlertDialog(
            onDismissRequest = { mostrarTimePicker = false },
            title = { Text("Hora de envío") },
            text = { TimePicker(state = state) },
            confirmButton = {
                TextButton(onClick = {
                    hora = state.hour
                    minuto = state.minute
                    mostrarTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarTimePicker = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        containerColor = BlancoSuave,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Configurar envío") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blanco,
                    titleContentColor = TextoOscuro
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            cliente?.let { c ->
                // Encabezado con datos del cliente
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Celeste)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(c.nombre, color = Blanco, fontWeight = FontWeight.Bold)
                        if (c.email.isNotBlank()) {
                            Text("📧 ${c.email}", color = Blanco.copy(alpha = 0.9f))
                        }
                        if (c.email.isBlank()) {
                            Text(
                                "⚠️ Este cliente no tiene email cargado. Editalo desde su perfil.",
                                color = Blanco
                            )
                        }
                    }
                }
            }

            // ============== ASUNTO ==============
            CardSeccion(titulo = "Asunto del email", icono = Icons.Default.Title) {
                OutlinedTextField(
                    value = asunto,
                    onValueChange = { asunto = it },
                    placeholder = { Text("Recordatorio: Vencimiento Monotributo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CelesteOsc,
                        focusedLabelColor = CelesteOsc
                    )
                )
                Text(
                    "Si lo dejás en blanco, se usa el asunto por defecto.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSuave
                )
            }

            // ============== MENSAJE / BORRADOR ==============
            CardSeccion(titulo = "Mensaje", icono = Icons.AutoMirrored.Filled.Send) {
                OutlinedTextField(
                    value = mensajeBody,
                    onValueChange = { mensajeBody = it },
                    placeholder = { Text("Hola [Nombre], te recordamos que el día 20…") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CelesteOsc,
                        focusedLabelColor = CelesteOsc
                    )
                )

                val tieneBorrador = (cliente?.emailBorrador?.isNotBlank() == true)
                if (tieneBorrador) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(Amarillo.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, null, tint = Amarillo, modifier = Modifier.size(16.dp))
                        Text(
                            "Tenés un borrador sin enviar",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoOscuro,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.descartarBorrador() }) {
                            Text("Descartar", color = Rojo)
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.guardarBorrador(mensajeBody) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Amarillo)
                    ) {
                        Icon(Icons.Default.Save, null, tint = Amarillo, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Guardar borrador", color = TextoOscuro)
                    }
                }
            }

            // ============== ADJUNTO ==============
            CardSeccion(titulo = "Archivo adjunto (PDF o foto)", icono = Icons.Default.AttachFile) {
                if (adjuntoUri.isNotBlank()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(Verde.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Verde, modifier = Modifier.size(20.dp))
                        Text(
                            adjuntoNombre.ifBlank { "Archivo seleccionado" },
                            modifier = Modifier.weight(1f),
                            color = TextoOscuro
                        )
                        IconButton(onClick = {
                            adjuntoUri = ""
                            adjuntoNombre = ""
                        }) {
                            Icon(Icons.Default.Close, "Quitar adjunto", tint = Rojo)
                        }
                    }
                } else {
                    Text(
                        "Sin adjunto extra. Igual se envía la factura PDF generada automáticamente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSuave
                    )
                }

                Button(
                    onClick = {
                        pickerAdjunto.launch(arrayOf("application/pdf", "image/*"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CelesteOsc)
                ) {
                    Icon(Icons.Default.AttachFile, null, tint = Blanco, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (adjuntoUri.isBlank()) "Elegir adjunto" else "Cambiar adjunto",
                        color = Blanco
                    )
                }
            }

            // ============== PROGRAMACIÓN AUTOMÁTICA ==============
            CardSeccion(titulo = "Envío automático", icono = Icons.Default.Schedule) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Activar recordatorio mensual", fontWeight = FontWeight.SemiBold, color = TextoOscuro)
                        Text(
                            "Te llega una notificación el día y hora elegidos para mandar el aviso.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSuave
                        )
                    }
                    Switch(
                        checked = envioAuto,
                        onCheckedChange = { envioAuto = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CelesteOsc)
                    )
                }

                if (envioAuto) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = diaMes.toString(),
                            onValueChange = { v -> diaMes = v.filter { it.isDigit() }.toIntOrNull()?.coerceIn(1, 28) ?: diaMes },
                            label = { Text("Día (1-28)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = "%02d:%02d".format(hora, minuto),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Hora") },
                            modifier = Modifier.weight(1f),
                            trailingIcon = {
                                IconButton(onClick = { mostrarTimePicker = true }) {
                                    Icon(Icons.Default.Schedule, "Elegir hora")
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Text(
                        "💡 Sugerido: día 18 a las 09:00 (2 días antes del vencimiento del 20).",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSuave
                    )
                }
            }

            // ============== WHATSAPP ==============
            CardSeccion(titulo = "WhatsApp", icono = Icons.AutoMirrored.Filled.Chat, colorTitulo = Verde) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Enviar también por WhatsApp", fontWeight = FontWeight.SemiBold, color = TextoOscuro)
                        Text(
                            "Abre WhatsApp con el mensaje y el adjunto pre-cargados.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSuave
                        )
                    }
                    Switch(
                        checked = whatsappActivo,
                        onCheckedChange = { whatsappActivo = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Verde)
                    )
                }

                OutlinedTextField(
                    value = whatsappNumero,
                    onValueChange = { whatsappNumero = it.filter { c -> c.isDigit() }.take(15) },
                    label = { Text("Número (con código país, sin +)") },
                    placeholder = { Text("5491122334455") },
                    leadingIcon = { Icon(Icons.Default.Phone, null, tint = Verde) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Verde,
                        focusedLabelColor = Verde
                    )
                )

                OutlinedTextField(
                    value = whatsappMensaje,
                    onValueChange = { whatsappMensaje = it },
                    label = { Text("Mensaje de WhatsApp guardado") },
                    placeholder = { Text("Hola [Nombre], te recuerdo mis honorarios del mes...") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 110.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Verde,
                        focusedLabelColor = Verde
                    )
                )
            }

            // ============== ACCIONES ==============
            Button(
                onClick = {
                    viewModel.guardarConfig(
                        emailAsunto = asunto,
                        emailMensaje = mensajeBody,
                        emailAdjuntoUri = adjuntoUri,
                        emailAdjuntoNombre = adjuntoNombre,
                        envioAutomatico = envioAuto,
                        envioDiaMes = diaMes,
                        envioHora = hora,
                        envioMinuto = minuto,
                        whatsappNumero = whatsappNumero,
                        whatsappEnviar = whatsappActivo,
                        whatsappMensaje = whatsappMensaje
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CelesteOsc)
            ) {
                Icon(Icons.Default.Save, null, tint = Blanco)
                Spacer(Modifier.width(6.dp))
                Text("Guardar configuración", color = Blanco, fontWeight = FontWeight.Bold)
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            cliente?.let { c ->
                                // Guardamos primero la config para que el envío use los valores actuales.
                                viewModel.guardarConfig(
                                    asunto, mensajeBody, adjuntoUri, adjuntoNombre,
                                    envioAuto, diaMes, hora, minuto,
                                    whatsappNumero, whatsappActivo, whatsappMensaje
                                )
                                val intent = viewModelClientes.enviarRecordatorio(context, c)
                                intent?.let { runCatching { context.startActivity(it) } }
                            }
                        }
                    },
                    enabled = cliente?.email?.isNotBlank() == true,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Amarillo)
                ) {
                    Icon(Icons.Default.Email, null, tint = Blanco)
                    Spacer(Modifier.width(6.dp))
                    Text("Enviar Email", color = Blanco, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        scope.launch {
                            cliente?.let { c ->
                                viewModel.guardarConfig(
                                    asunto, mensajeBody, adjuntoUri, adjuntoNombre,
                                    envioAuto, diaMes, hora, minuto,
                                    whatsappNumero, whatsappActivo, whatsappMensaje
                                )
                                val intent = viewModelClientes.enviarRecordatorioWhatsApp(context, c)
                                intent?.let { runCatching { context.startActivity(it) } }
                            }
                        }
                    },
                    enabled = whatsappNumero.isNotBlank(),
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Verde)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, null, tint = Blanco)
                    Spacer(Modifier.width(6.dp))
                    Text("WhatsApp", color = Blanco, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CardSeccion(
    titulo: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    colorTitulo: Color = CelesteOsc,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        border = BorderStroke(1.dp, GrisCeleste)
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icono, null, tint = colorTitulo, modifier = Modifier.size(20.dp))
                Text(titulo, fontWeight = FontWeight.Bold, color = TextoOscuro)
            }
            contenido()
        }
    }
}

// Pequeño helper visual para no repetir el "Card + Header" en cada sección.

