package com.manuelreeb.monocontrol.ui.excel

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcelScreen(viewModel: ExcelViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Lanzador para elegir archivo
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importar(context, it) }
    }

    // Lanzar el intent de compartir cuando export tiene éxito
    LaunchedEffect(uiState) {
        if (uiState is ExcelUiState.ExportSuccess) {
            val intent = (uiState as ExcelUiState.ExportSuccess).intent
            context.startActivity(Intent.createChooser(intent, "Compartir reporte Excel"))
            viewModel.resetState()
        }
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Importar ──
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("📥 Importar planilla Excel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Subí tu planilla con ingresos y gastos. El archivo debe tener el formato:\n" +
                                    "Monto | Tipo | Fecha | Descripción\n\n" +
                                    "✅ Compatible con .xlsx y .xls",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Button(
                            onClick = {
                                filePicker.launch("*/*")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState !is ExcelUiState.Loading
                        ) {
                            Text("Seleccionar archivo Excel")
                        }
                    }
                }
            }

            // ── Exportar ──
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("📤 Exportar reporte Excel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Genera un archivo .xlsx completo con:\n" +
                                    "• Historial de movimientos\n" +
                                    "• Resumen mensual\n" +
                                    "• Categoría actual y tabla de límites\n" +
                                    "• Plantilla para importar",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Button(
                            onClick = { viewModel.exportar(context) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState !is ExcelUiState.Loading
                        ) {
                            Text("Generar y compartir reporte")
                        }
                    }
                }
            }

            // ── Estado ──
            item {
                when (val state = uiState) {
                    is ExcelUiState.Loading -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Procesando...")
                        }
                    }
                    is ExcelUiState.ImportSuccess -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "✅ Importación exitosa",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("${state.importados} movimientos importados correctamente.")
                                if (state.errores.isNotEmpty()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text("⚠️ ${state.errores.size} filas con errores:", fontWeight = FontWeight.SemiBold)
                                    state.errores.forEach { err -> Text("• $err", style = MaterialTheme.typography.bodySmall) }
                                }
                                TextButton(onClick = { viewModel.resetState() }) { Text("Cerrar") }
                            }
                        }
                    }
                    is ExcelUiState.Error -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("❌ Error", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(state.mensaje)
                                TextButton(onClick = { viewModel.resetState() }) { Text("Cerrar") }
                            }
                        }
                    }
                    else -> {}
                }
            }

            // ── Instrucciones de formato ──
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("📋 Formato para importar", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("La fila 1 debe contener los encabezados. A partir de la fila 2, los datos:", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(4.dp))
                        listOf(
                            "Columna A → Monto (ej: 50000)",
                            "Columna B → Tipo: INGRESO o GASTO",
                            "Columna C → Fecha: dd/MM/yyyy",
                            "Columna D → Descripción (opcional)"
                        ).forEach {
                            Text("• $it", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "💡 Tip: Exportá el reporte para obtener una hoja \"Plantilla Importar\" con el formato exacto.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

