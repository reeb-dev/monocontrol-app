package com.reeb.controlmonotributoar.ui.reportes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun ReportesScreen(onBack: () -> Unit) {
    var selectedFormat by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = BlancoSuave,
        topBar = {
            TopAppBar(
                title = { Text("Reportes", fontWeight = FontWeight.Bold) },
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
            Text("Selecciona el tipo de reporte", style = MaterialTheme.typography.labelLarge, color = GrisMedio)

            ReporteOptionSimple("Resumen Mensual", "Facturación, gastos y estado", selectedFormat?.startsWith("mensual") ?: false) { selectedFormat = "mensual" }
            ReporteOptionSimple("Reporte Anual", "Resumen fiscal del año", selectedFormat?.startsWith("anual") ?: false) { selectedFormat = "anual" }
            ReporteOptionSimple("Historial de Cambios", "Todas las modificaciones", selectedFormat?.startsWith("cambios") ?: false) { selectedFormat = "cambios" }
            ReporteOptionSimple("Certificado", "Para presentar en ARCA", selectedFormat?.startsWith("certificado") ?: false) { selectedFormat = "certificado" }

            if (selectedFormat != null) {
                Column(Modifier.fillMaxWidth()) {
                    Text("Formato", style = MaterialTheme.typography.labelMedium, color = GrisMedio)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { }, modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RojoMedio)) {
                            Icon(Icons.Default.PictureAsPdf, null, tint = BlancoArgentino, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("PDF", fontWeight = FontWeight.Bold, color = BlancoArgentino)
                        }
                        Button(onClick = { }, modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeExito)) {
                            Icon(Icons.Default.TableChart, null, tint = BlancoArgentino, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Excel", fontWeight = FontWeight.Bold, color = BlancoArgentino)
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ReporteOptionSimple(titulo: String, subtitulo: String, seleccionado: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (seleccionado) CelesteOscuro.copy(alpha = 0.1f) else BlancoArgentino),
        border = BorderStroke(if (seleccionado) 2.dp else 1.dp, if (seleccionado) CelesteOscuro else CelesteBorde)
    ) {
        Row(Modifier.fillMaxWidth().clickable { onClick() }.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Description, null, tint = CelesteOscuro, modifier = Modifier.size(24.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.Bold, color = TextoOscuro)
                Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = GrisMedio)
            }
            if (seleccionado) Icon(Icons.Default.Check, null, tint = CelesteOscuro)
        }
    }
}

