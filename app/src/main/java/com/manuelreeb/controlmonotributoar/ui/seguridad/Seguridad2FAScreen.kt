package com.reeb.controlmonotributoar.ui.seguridad

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.reeb.controlmonotributoar.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Seguridad2FAScreen(onBack: () -> Unit) {
    var twoFaActivo by remember { mutableStateOf(false) }
    var biometriaActiva by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BlancoSuave,
        topBar = {
            TopAppBar(
                title = { Text("Seguridad avanzada", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── AUTENTICACIÓN DE 2 FACTORES ──────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
                border = BorderStroke(1.dp, CelesteBorde)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Security, null, tint = if (twoFaActivo) VerdeExito else RojoMedio,
                            modifier = Modifier.size(20.dp))
                        Text("Autenticación de 2 Factores (2FA)", fontWeight = FontWeight.Bold,
                            color = TextoOscuro, modifier = Modifier.weight(1f))
                        Switch(
                            checked = twoFaActivo,
                            onCheckedChange = { twoFaActivo = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = VerdeExito,
                                checkedTrackColor = VerdeExito.copy(alpha = 0.3f)
                            )
                        )
                    }

                    Text(
                        if (twoFaActivo)
                            "✓ Protección con 2FA activa. Se pedirá un código cada vez que inicies sesión."
                        else
                            "Agrega una capa extra de seguridad. Se pedirá un código de tu app de autenticación.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GrisMedio
                    )

                    if (!twoFaActivo) {
                        Button(
                            onClick = { },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CelesteOscuro)
                        ) {
                            Icon(Icons.Default.QrCode, null, tint = BlancoArgentino, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Configurar con QR", fontWeight = FontWeight.Bold, color = BlancoArgentino)
                        }
                    }
                }
            }

            // ── BIOMETRÍA ────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BlancoArgentino),
                border = BorderStroke(1.dp, CelesteBorde)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Fingerprint, null, tint = if (biometriaActiva) VerdeExito else GrisMedio,
                            modifier = Modifier.size(20.dp))
                        Text("Desbloqueo biométrico", fontWeight = FontWeight.Bold,
                            color = TextoOscuro, modifier = Modifier.weight(1f))
                        Switch(
                            checked = biometriaActiva,
                            onCheckedChange = { biometriaActiva = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = VerdeExito
                            )
                        )
                    }

                    Text(
                        "Usa tu huella dactilar o reconocimiento facial para abrir la app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GrisMedio
                    )
                }
            }

            // ── HISTORIAL DE INTENTOS ────────────────────────────────
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
                        Text("Historial de inicios de sesión", fontWeight = FontWeight.Bold, color = TextoOscuro)
                    }

                    repeat(3) { i ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(GrisClaro)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Inicio de sesión exitoso", fontWeight = FontWeight.SemiBold,
                                    color = TextoOscuro, fontSize = MaterialTheme.typography.labelSmall.fontSize)
                                Text("Hoy a las ${14 - i}:${32 - i * 5}  •  Buenos Aires, AR",
                                    style = MaterialTheme.typography.labelSmall, color = GrisMedio)
                            }
                            Icon(Icons.Default.Check, null, tint = VerdeExito, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // ── CERRAR SESIÓN ────────────────────────────────────────
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RojoMedio)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = BlancoArgentino, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión", fontWeight = FontWeight.Bold, color = BlancoArgentino)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

