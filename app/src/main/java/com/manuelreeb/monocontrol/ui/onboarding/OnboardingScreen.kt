package com.manuelreeb.monocontrol.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manuelreeb.monocontrol.ui.perfil.PerfilViewModel

private val Celeste     = Color(0xFF75AADB)
private val CelesteOsc  = Color(0xFF4A86C8)
private val Amarillo    = Color(0xFFFBB81C)
private val Blanco      = Color(0xFFFFFFFF)
private val BlancoSuave = Color(0xFFF0F6FF)
private val GrisCeleste = Color(0xFFCCDFF4)
private val TextoOscuro = Color(0xFF0D2A4A)
private val TextoSuave  = Color(0xFF6B7B8C)

/**
 * Pantalla mostrada inmediatamente después del registro para precargar
 * los datos básicos del usuario: nombre/razón social, CUIT y rubro.
 * Se puede saltar; los datos se editan luego en Perfil.
 */
@Composable
fun OnboardingScreen(
    viewModel: PerfilViewModel,
    onListo: () -> Unit
) {
    val perfil by viewModel.perfil.collectAsStateWithLifecycle()

    var nombre by rememberSaveable { mutableStateOf("") }
    var cuit by rememberSaveable { mutableStateOf("") }
    var ventaMuebles by rememberSaveable { mutableStateOf(false) }
    var hidratado by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(perfil) {
        val p = perfil
        if (p != null && !hidratado) {
            nombre = p.nombre
            cuit = p.cuit
            ventaMuebles = p.esVentaMuebles
            hidratado = true
        }
    }

    val cuitValido = cuit.replace("-", "").let { it.isEmpty() || it.length == 11 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlancoSuave)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Celeste, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("¡Bienvenido!", color = Blanco, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                Text(
                    "Cargá tus datos para que la app calcule tus límites y cuotas correctas. " +
                        "Podés saltarlo y completarlo después.",
                    color = Blanco.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Blanco),
            border = BorderStroke(1.dp, GrisCeleste)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Datos personales", fontWeight = FontWeight.Bold, color = TextoOscuro)

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre o Razón Social") },
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = CelesteOsc) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CelesteOsc,
                        focusedLabelColor = CelesteOsc
                    )
                )

                OutlinedTextField(
                    value = cuit,
                    onValueChange = { nuevo ->
                        // Solo permitimos dígitos y guiones, máx 13 caracteres (xx-xxxxxxxx-x).
                        val limpio = nuevo.filter { it.isDigit() || it == '-' }.take(13)
                        cuit = limpio
                    },
                    label = { Text("CUIT (11 dígitos)") },
                    leadingIcon = { Icon(Icons.Default.Badge, null, tint = CelesteOsc) },
                    isError = !cuitValido,
                    supportingText = {
                        if (!cuitValido) Text("El CUIT debe tener 11 dígitos", color = Color(0xFFC62828))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CelesteOsc,
                        focusedLabelColor = CelesteOsc
                    )
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Blanco),
            border = BorderStroke(1.dp, GrisCeleste)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("¿Cuál es tu rubro?", fontWeight = FontWeight.Bold, color = TextoOscuro)
                Text(
                    "Servicios y venta de muebles pagan cuotas distintas a partir de la categoría C.",
                    style = MaterialTheme.typography.bodySmall, color = TextoSuave
                )
                RubroOpt(
                    titulo = "Locaciones / Prestación de servicios",
                    seleccionado = !ventaMuebles,
                    onClick = { ventaMuebles = false }
                )
                RubroOpt(
                    titulo = "Venta de cosas muebles",
                    seleccionado = ventaMuebles,
                    onClick = { ventaMuebles = true }
                )
            }
        }

        Button(
            onClick = {
                viewModel.guardar(nombre, cuit)
                viewModel.cambiarRubro(ventaMuebles)
                onListo()
            },
            enabled = cuitValido,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CelesteOsc)
        ) {
            Text("Continuar", fontWeight = FontWeight.Bold, color = Blanco, fontSize = 16.sp)
        }
        TextButton(onClick = onListo, modifier = Modifier.fillMaxWidth()) {
            Text("Saltar y completar después", color = TextoSuave)
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun RubroOpt(titulo: String, seleccionado: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (seleccionado) Celeste.copy(alpha = 0.12f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = seleccionado, onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = if (seleccionado) Amarillo else CelesteOsc)
        )
        Spacer(Modifier.width(4.dp))
        Text(titulo, color = TextoOscuro,
            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal)
    }
}

