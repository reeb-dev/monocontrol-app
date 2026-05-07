package com.reeb.controlmonotributoar.ui.login

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reeb.controlmonotributoar.R
import com.reeb.controlmonotributoar.domain.model.UserRole
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import android.widget.Toast

private val Celeste     = Color(0xFF75AADB)
private val CelesteOsc  = Color(0xFF4A86C8)
private val Amarillo    = Color(0xFFFBB81C)
private val Blanco      = Color(0xFFFFFFFF)
private val BlancoSuave = Color(0xFFF0F6FF)
private val GrisCeleste = Color(0xFFCCDFF4)
private val TextoOscuro = Color(0xFF0D2A4A)
private val TextoSuave  = Color(0xFF6B7B8C)

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegistroExitoso: (UserRole) -> Unit,
    onVolverALogin: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val cargando = state is AuthUiState.Loading
    val errorMsg = (state as? AuthUiState.Error)?.mensaje

    var email      by remember { mutableStateOf("") }
    var password   by remember { mutableStateOf("") }
    var password2  by remember { mutableStateOf("") }
    var rol        by remember { mutableStateOf(UserRole.PERSONAL) }
    var localError by remember { mutableStateOf<String?>(null) }

    // Configurar Google Sign-In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // Launcher para el Intent de Google Sign-In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                // Usar el rol seleccionado cuando se registra con Google
                viewModel.loginWithGoogle(account, rol)
            } catch (e: ApiException) {
                Toast.makeText(context, "Error al registrarse con Google: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(state) {
        (state as? AuthUiState.Success)?.let {
            onRegistroExitoso(it.rol)
            viewModel.resetEstado()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlancoSuave)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            "Crear cuenta",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold, color = TextoOscuro
            )
        )
        Text(
            "Registrate para guardar tus movimientos y sincronizar con Firebase.",
            style = MaterialTheme.typography.bodySmall,
            color = TextoSuave
        )

        // ── Selector de rol ─────────────────────────────────────────────
        Text("¿Para qué vas a usar la app?",
            style = MaterialTheme.typography.titleSmall,
            color = TextoOscuro,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp))

        UserRole.entries.forEach { opcion ->
            RolOptionCard(
                rol = opcion,
                seleccionado = rol == opcion,
                icono = if (opcion == UserRole.PERSONAL) Icons.Default.Person else Icons.Default.Business,
                onClick = { rol = opcion }
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(20.dp),
            colors   = CardDefaults.cardColors(containerColor = Blanco),
            border   = BorderStroke(1.dp, GrisCeleste)
        ) {
            Column(
                Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value           = email,
                    onValueChange   = { email = it; localError = null; if (errorMsg != null) viewModel.resetEstado() },
                    label           = { Text("Email") },
                    leadingIcon     = { Icon(Icons.Default.Email, null, tint = CelesteOsc) },
                    modifier        = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine      = true,
                    enabled         = !cargando,
                    shape           = RoundedCornerShape(12.dp),
                    colors          = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CelesteOsc,
                        focusedLabelColor  = CelesteOsc
                    )
                )
                OutlinedTextField(
                    value                = password,
                    onValueChange        = { password = it; localError = null; if (errorMsg != null) viewModel.resetEstado() },
                    label                = { Text("Contraseña (mín. 6)") },
                    leadingIcon          = { Icon(Icons.Default.Lock, null, tint = CelesteOsc) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier             = Modifier.fillMaxWidth(),
                    keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine           = true,
                    enabled              = !cargando,
                    shape                = RoundedCornerShape(12.dp),
                    colors               = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CelesteOsc,
                        focusedLabelColor  = CelesteOsc
                    )
                )
                OutlinedTextField(
                    value                = password2,
                    onValueChange        = { password2 = it; localError = null },
                    label                = { Text("Repetir contraseña") },
                    leadingIcon          = { Icon(Icons.Default.Lock, null, tint = CelesteOsc) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier             = Modifier.fillMaxWidth(),
                    keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine           = true,
                    enabled              = !cargando,
                    shape                = RoundedCornerShape(12.dp),
                    isError              = password2.isNotEmpty() && password2 != password,
                    colors               = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CelesteOsc,
                        focusedLabelColor  = CelesteOsc
                    )
                )

                (localError ?: errorMsg)?.let {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE53935).copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(it, color = Color(0xFFC62828),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold)
                    }
                }

                Button(
                    onClick = {
                        when {
                            password != password2 -> localError = "Las contraseñas no coinciden"
                            else -> viewModel.registrar(email, password, rol)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(12.dp),
                    enabled  = !cargando,
                    colors   = ButtonDefaults.buttonColors(containerColor = CelesteOsc)
                ) {
                    if (cargando) {
                        CircularProgressIndicator(color = Blanco, strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp))
                    } else {
                        Text("Crear cuenta",
                            fontWeight = FontWeight.Bold, color = Blanco, fontSize = 16.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                    Text(
                        "   o   ",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                }

                // Botón de Google Sign-In
                OutlinedButton(
                    onClick = {
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !cargando,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Blanco,
                        contentColor = TextoOscuro
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔵", fontSize = 20.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Registrarse con Google",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                TextButton(
                    onClick = onVolverALogin,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !cargando
                ) {
                    Text("¿Ya tenés cuenta? Iniciá sesión",
                        textAlign = TextAlign.Center,
                        color = CelesteOsc,
                        fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun RolOptionCard(
    rol: UserRole,
    seleccionado: Boolean,
    icono: ImageVector,
    onClick: () -> Unit
) {
    val borderColor = if (seleccionado) Amarillo else GrisCeleste
    val tint = if (seleccionado) Amarillo else CelesteOsc
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = Blanco),
        border   = BorderStroke(if (seleccionado) 2.dp else 1.dp, borderColor)
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(tint.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) { Icon(icono, null, tint = tint) }
            Column(Modifier.weight(1f)) {
                Text(rol.label, fontWeight = FontWeight.Bold, color = TextoOscuro)
                Text(rol.descripcion,
                    style = MaterialTheme.typography.bodySmall, color = TextoSuave)
            }
            RadioButton(
                selected = seleccionado,
                onClick  = onClick,
                colors   = RadioButtonDefaults.colors(selectedColor = Amarillo)
            )
        }
    }
}

