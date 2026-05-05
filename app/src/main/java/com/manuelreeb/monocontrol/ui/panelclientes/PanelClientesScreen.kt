package com.manuelreeb.monocontrol.ui.panelclientes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.manuelreeb.monocontrol.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelClientesScreen(viewModel: Any, onBack: () -> Unit) {
    Scaffold(
        containerColor = BlancoSuave,
        topBar = {
            TopAppBar(
                title = { Text("Panel de clientes", fontWeight = FontWeight.Bold) },
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
            Text("Panel de gestión de clientes", style = MaterialTheme.typography.titleMedium, color = TextoOscuro)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BlancoArgentino)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Group, null, tint = CelesteOscuro, modifier = Modifier.size(20.dp))
                        Text("Cartera de clientes", fontWeight = FontWeight.Bold, color = TextoOscuro)
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CelesteOscuro.copy(alpha = 0.08f))) {
                            Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total", style = MaterialTheme.typography.labelSmall, color = GrisMedio, fontSize = MaterialTheme.typography.labelSmall.fontSize)
                                Text("12 clientes", fontWeight = FontWeight.Bold, color = CelesteOscuro)
                            }
                        }
                        Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = VerdeExito.copy(alpha = 0.08f))) {
                            Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Activos", style = MaterialTheme.typography.labelSmall, color = GrisMedio)
                                Text("10 clientes", fontWeight = FontWeight.Bold, color = VerdeExito)
                            }
                        }
                        Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = RojoMedio.copy(alpha = 0.08f))) {
                            Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Riesgo", style = MaterialTheme.typography.labelSmall, color = GrisMedio)
                                Text("2 clientes", fontWeight = FontWeight.Bold, color = RojoMedio)
                            }
                        }
                    }

                    Text("📊 Utiliza el panel para monitorear la facturación de tus clientes y detectar cambios de categoría automáticamente.",
                        style = MaterialTheme.typography.bodySmall, color = GrisMedio)
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

