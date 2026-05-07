package com.reeb.controlmonotributoar.ui.login

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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

private val Celeste      = Color(0xFF75AADB)
private val CelesteOsc   = Color(0xFF4A86C8)
private val SolAmarillo  = Color(0xFFFBB81C)
private val Blanco       = Color(0xFFFFFFFF)
private val TextoOscuro  = Color(0xFF0D2A4A)

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (UserRole) -> Unit,
    onIrARegistro: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var email       by remember { mutableStateOf(viewModel.emailRecordado()) }
    var password    by remember { mutableStateOf(viewModel.passwordRecordado()) }
    var passVisible by remember { mutableStateOf(false) }
    var recordarme  by remember { mutableStateOf(viewModel.recordarme()) }

    val cargando = state is AuthUiState.Loading
    val errorMsg = (state as? AuthUiState.Error)?.mensaje

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
                viewModel.loginWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(context, "Error al iniciar sesión con Google: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(state) {
        (state as? AuthUiState.Success)?.let {
            onLoginSuccess(it.rol)
            viewModel.resetEstado()
        }
    }

    val logoScale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        logoScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
    }

    Box(modifier = Modifier.fillMaxSize().background(Blanco))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.logo1),
            contentDescription = "Monotributo Al Día",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .scale(logoScale.value)
                .size(320.dp) // 👈 lo hacés explícitamente más grande
        )

        Spacer(Modifier.height(8.dp))
        Box(Modifier.width(200.dp).height(4.dp).background(Celeste, RoundedCornerShape(2.dp)))
        Spacer(Modifier.height(2.dp))
        Box(Modifier.width(140.dp).height(3.dp).background(SolAmarillo, RoundedCornerShape(2.dp)))
        Spacer(Modifier.height(20.dp))

        Card(
            modifier  = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
            shape     = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(10.dp),
            colors    = CardDefaults.cardColors(containerColor = Blanco)
        ) {
            Column(
                modifier            = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    Box(Modifier.size(5.dp, 26.dp).background(SolAmarillo, RoundedCornerShape(3.dp)))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Iniciar sesión",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold, color = TextoOscuro
                        )
                    )
                    Spacer(Modifier.width(10.dp))
                    Box(Modifier.size(5.dp, 26.dp).background(SolAmarillo, RoundedCornerShape(3.dp)))
                }

                OutlinedTextField(
                    value           = email,
                    onValueChange   = { email = it; if (errorMsg != null) viewModel.resetEstado() },
                    label           = { Text("Email") },
                    leadingIcon     = { Icon(Icons.Default.Email, null, tint = CelesteOsc) },
                    modifier        = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine      = true,
                    shape           = RoundedCornerShape(12.dp),
                    enabled         = !cargando,
                    colors          = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CelesteOsc,
                        focusedLabelColor  = CelesteOsc,
                        cursorColor        = CelesteOsc
                    )
                )

                OutlinedTextField(
                    value                = password,
                    onValueChange        = { password = it; if (errorMsg != null) viewModel.resetEstado() },
                    label                = { Text("Contraseña") },
                    leadingIcon          = { Icon(Icons.Default.Lock, null, tint = CelesteOsc) },
                    trailingIcon         = {
                        IconButton(onClick = { passVisible = !passVisible }) {
                            Text(if (passVisible) "🙈" else "👁", fontSize = 18.sp)
                        }
                    },
                    visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier             = Modifier.fillMaxWidth(),
                    keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine           = true,
                    shape                = RoundedCornerShape(12.dp),
                    enabled              = !cargando,
                    isError              = errorMsg != null,
                    colors               = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CelesteOsc,
                        focusedLabelColor  = CelesteOsc,
                        cursorColor        = CelesteOsc
                    )
                )

                errorMsg?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE53935).copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(it, color = Color(0xFFC62828),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold)
                    }
                }

                // Checkbox "Recordarme" — guarda email/password en almacén
                // encriptado (AES-256 con Android Keystore) si está tildado.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = recordarme,
                        onCheckedChange = { recordarme = it },
                        enabled = !cargando,
                        colors = CheckboxDefaults.colors(checkedColor = CelesteOsc)
                    )
                    Text(
                        "Recordar email y contraseña",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextoOscuro),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Button(
                    onClick = { viewModel.login(email, password, recordarme) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(12.dp),
                    enabled  = !cargando,
                    colors   = ButtonDefaults.buttonColors(containerColor = Celeste)
                ) {
                    if (cargando) {
                        CircularProgressIndicator(color = Blanco, strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp))
                    } else {
                        Text("Ingresar",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold, color = Blanco))
                    }
                }

                TextButton(
                    onClick = {
                        viewModel.recuperarPassword(email) { mensaje ->
                            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled  = !cargando
                ) {
                    Text("¿Olvidaste tu contraseña?",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall.copy(color = CelesteOsc))
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                    Text(
                        "   o continuar con   ",
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
                        // Icono de Google (puedes usar un painterResource si tienes el logo)
                        Text("🔵", fontSize = 20.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Continuar con Google",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                HorizontalDivider(color = SolAmarillo, thickness = 2.dp)

                TextButton(
                    onClick  = onIrARegistro,
                    modifier = Modifier.fillMaxWidth(),
                    enabled  = !cargando
                ) {
                    Text("¿No tenés cuenta? Registrate",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = CelesteOsc, fontWeight = FontWeight.Bold))
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Disclaimer de la app
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFF3E0).copy(alpha = 0.7f))
                .padding(12.dp)
        ) {
            Text(
                "⚠️ Esta aplicación no es oficial ni representa a la ARCA. Use información actualizada de arca.gob.ar",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6D4C41),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(36.dp))
    }
}

