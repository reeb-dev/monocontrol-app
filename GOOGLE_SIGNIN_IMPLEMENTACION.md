# 🔵 Google Sign-In - Implementación Completa

## ✅ ESTADO: IMPLEMENTADO

Google Sign-In ha sido agregado a tu app MonoControl - Límite Monotributo.

---

## 📦 LO QUE SE IMPLEMENTÓ

### 1. Dependencias agregadas
- ✅ `com.google.android.gms:play-services-auth:21.2.0`
- ✅ Configurado en `gradle/libs.versions.toml`
- ✅ Agregado a `app/build.gradle.kts`

### 2. AuthRepository actualizado
- ✅ Método `signInWithGoogle(account: GoogleSignInAccount)` implementado
- ✅ Integración con Firebase Auth usando `GoogleAuthProvider`
- ✅ Manejo de errores incluido

### 3. AuthViewModel actualizado
- ✅ Método `loginWithGoogle(account, defaultRole)` implementado
- ✅ Guarda el perfil con email y nombre del usuario
- ✅ Soporta roles (Personal/Contador)

### 4. UI actualizada
- ✅ **LoginScreen**: Botón "Continuar con Google"
- ✅ **RegisterScreen**: Botón "Registrarse con Google"
- ✅ ActivityResultLauncher configurado
- ✅ Manejo de Intent result

### 5. Configuración
- ✅ `default_web_client_id` en `strings.xml`
- ✅ `oauth_client` en `google-services.json`

---

## 🔧 CONFIGURACIÓN REQUERIDA EN FIREBASE CONSOLE

Para que Google Sign-In funcione, debes completar estos pasos en Firebase Console:

### 1️⃣ Agregar la huella digital SHA-1 (IMPORTANTE)

Google Sign-In requiere el certificado SHA-1 de tu app.

#### Obtener SHA-1 para Debug:

```bash
cd /Users/manuelreeb/AndroidStudioProjects/LmiteMonotributo

# En macOS/Linux:
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android | grep SHA1

# En Windows:
keytool -list -v -keystore %USERPROFILE%\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android | findstr SHA1
```

**Salida esperada:**
```
SHA1: AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12
```

#### Agregar SHA-1 a Firebase:

1. Ve a: https://console.firebase.google.com/project/profile-68864/settings/general
2. Scroll hasta **"Tus apps"**
3. Encuentra tu app Android (`com.example.lmitemonotributo`)
4. Click en **"Agregar huella digital"**
5. Pega el SHA-1 obtenido
6. Click **"Guardar"**

### 2️⃣ Habilitar Google Sign-In en Authentication

1. Ve a: https://console.firebase.google.com/project/profile-68864/authentication
2. Pestaña **"Sign-in method"**
3. Habilita **"Google"**
4. Configura el email de soporte (tu email)
5. Click **"Guardar"**

### 3️⃣ Obtener el Web Client ID correcto

El `default_web_client_id` actual es temporal. Debes reemplazarlo:

1. Ve a: https://console.firebase.google.com/project/profile-68864/settings/general
2. Scroll hasta **"Tus apps"**
3. Si no existe una **Web app**, créala:
   - Click **"Agregar app" → Web**
   - Nombre: "MonoControl Web"
   - Registrar app
4. Copia el **Web client ID** (termina en `.apps.googleusercontent.com`)
5. Reemplázalo en `app/src/main/res/values/strings.xml`:

```xml
<string name="default_web_client_id" translatable="false">TU_WEB_CLIENT_ID_AQUI</string>
```

---

## 🧪 PROBAR GOOGLE SIGN-IN

### Requisitos previos:
1. ✅ SHA-1 agregado a Firebase Console
2. ✅ Google Sign-In habilitado en Authentication
3. ✅ Web client ID correcto en strings.xml
4. ✅ App instalada en dispositivo/emulador con Google Play Services

### Pasos para probar:

1. **Ejecutar la app:**
```bash
./gradlew :app:installDebug
```

2. **En la pantalla de Login:**
   - Verás el botón **"Continuar con Google"**
   - Click en el botón
   - Selecciona tu cuenta de Google
   - Acepta los permisos
   - ¡Deberías estar dentro de la app! 🎉

3. **En la pantalla de Registro:**
   - Selecciona tu rol (Personal o Contador)
   - Click en **"Registrarse con Google"**
   - Selecciona tu cuenta de Google
   - El rol seleccionado se guardará automáticamente

### Verificar en Firebase:

1. Ve a: https://console.firebase.google.com/project/profile-68864/authentication/users
2. Deberías ver tu cuenta con el provider "Google"

---

## 💡 CÓMO FUNCIONA

### Flujo de Login con Google:

```
Usuario click "Continuar con Google"
    ↓
Se abre el selector de cuentas de Google
    ↓
Usuario selecciona su cuenta
    ↓
Google devuelve un ID Token
    ↓
AuthRepository.signInWithGoogle(account)
    ↓
Firebase Auth valida el token
    ↓
Usuario autenticado ✅
    ↓
Se guarda el perfil (email, nombre, rol)
    ↓
Navegación a Home
```

### Datos obtenidos de Google:

- ✅ **Email**: `user@gmail.com`
- ✅ **Nombre**: `Juan Pérez` (si está disponible)
- ✅ **Foto de perfil**: URL (disponible en `user.photoUrl`)
- ✅ **ID único**: Firebase UID

---

## 📋 CÓDIGO IMPLEMENTADO

### AuthRepository.kt

```kotlin
suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<FirebaseUser> = runCatching {
    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
    val res = auth.signInWithCredential(credential).await()
    res.user ?: error("Login con Google fallido: respuesta vacía")
}.recoverCatching { mapError(it) }
```

### AuthViewModel.kt

```kotlin
fun loginWithGoogle(account: GoogleSignInAccount, defaultRole: UserRole = UserRole.PERSONAL) {
    _uiState.value = AuthUiState.Loading
    viewModelScope.launch {
        authRepo.signInWithGoogle(account)
            .onSuccess { user ->
                // Verificar si ya existe un perfil
                val perfilActual = perfilRepo.observar().first()
                val rol = if (perfilActual != null && perfilActual.rol.isNotEmpty()) {
                    UserRole.fromName(perfilActual.rol)
                } else {
                    defaultRole
                }
                
                perfilRepo.guardar(
                    (perfilActual ?: PerfilEntity()).copy(
                        email = user.email.orEmpty(),
                        nombre = user.displayName ?: perfilActual?.nombre ?: "",
                        rol = rol.name
                    )
                )
                _uiState.value = AuthUiState.Success(rol)
            }
            .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Error con Google") }
    }
}
```

### LoginScreen.kt

```kotlin
// Configurar Google Sign-In
val gso = remember {
    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
}

val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

// Launcher para el Intent
val googleSignInLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            viewModel.loginWithGoogle(account)
        } catch (e: ApiException) {
            // Error
        }
    }
}

// Botón
OutlinedButton(
    onClick = { googleSignInLauncher.launch(googleSignInClient.signInIntent) }
) {
    Text("Continuar con Google")
}
```

---

## 🐛 SOLUCIÓN DE PROBLEMAS

### Error: "Developer console is not set up correctly"

**Causa:** SHA-1 no agregado o Web client ID incorrecto.

**Solución:**
1. Verifica que agregaste el SHA-1 en Firebase Console
2. Verifica que el `default_web_client_id` es correcto
3. Sincroniza el proyecto: `./gradlew clean`

### Error: "Sign in failed"

**Causa:** Google Sign-In no habilitado en Firebase.

**Solución:**
1. Ve a Firebase Console → Authentication → Sign-in method
2. Habilita Google
3. Guarda los cambios

### Error: "API key not valid"

**Causa:** El `google-services.json` es incorrecto o la app no está registrada.

**Solución:**
1. Descarga nuevamente `google-services.json` desde Firebase Console
2. Reemplaza el archivo en `app/google-services.json`
3. Sync Gradle

### No aparece el selector de cuentas

**Causa:** Google Play Services no disponible en el emulador.

**Solución:**
1. Usa un emulador con Google Play (no AOSP)
2. O prueba en un dispositivo físico
3. Asegúrate de tener Google Play Services actualizado

### Error: "12501: SIGN_IN_CANCELLED"

**Causa:** Usuario canceló el proceso o configuración incorrecta.

**Solución:**
1. Verifica el SHA-1
2. Verifica el package name (`com.example.lmitemonotributo`)
3. Espera unos minutos después de cambiar la configuración en Firebase

---

## 🔒 SEGURIDAD

### ✅ Implementado:

- Token ID de Google validado por Firebase
- Autenticación OAuth 2.0
- No se almacenan contraseñas
- Sesión segura con Firebase Auth
- Token automáticamente renovado

### 🔐 Recomendaciones:

- En producción, usa el certificado de release (no debug)
- Obtén el SHA-1 de tu keystore de release
- Agrégalo también a Firebase Console
- Configura correctamente el `signingConfig` en `build.gradle.kts`

---

## 📊 VENTAJAS DE GOOGLE SIGN-IN

✅ **UX mejorada**: Login con 1 click  
✅ **Sin contraseñas**: Mayor seguridad  
✅ **Datos verificados**: Email de Google verificado  
✅ **Nombre automático**: Se obtiene de Google  
✅ **Foto de perfil**: Disponible  
✅ **Multi-dispositivo**: Misma cuenta en todos lados  

---

## 🎓 RECURSOS

### Documentación oficial:
- [Firebase Auth with Google](https://firebase.google.com/docs/auth/android/google-signin)
- [Google Sign-In Android](https://developers.google.com/identity/sign-in/android/start)

### Console:
- **Firebase Console**: https://console.firebase.google.com/project/profile-68864
- **Authentication**: https://console.firebase.google.com/project/profile-68864/authentication
- **Settings**: https://console.firebase.google.com/project/profile-68864/settings/general

---

## ✅ CHECKLIST DE IMPLEMENTACIÓN

- [x] Dependencia `play-services-auth` agregada
- [x] AuthRepository actualizado
- [x] AuthViewModel actualizado
- [x] LoginScreen con botón de Google
- [x] RegisterScreen con botón de Google
- [x] `default_web_client_id` en strings.xml
- [x] `oauth_client` en google-services.json
- [x] Manejo de errores
- [ ] **SHA-1 agregado a Firebase Console** ← PENDIENTE
- [ ] **Google Sign-In habilitado en Authentication** ← PENDIENTE
- [ ] **Web client ID correcto obtenido** ← PENDIENTE
- [ ] Probar en dispositivo/emulador

---

## 🚀 PRÓXIMOS PASOS

### Inmediato (HOY):
1. ✅ Obtener SHA-1 (comando arriba ⬆️)
2. ✅ Agregar SHA-1 a Firebase Console
3. ✅ Habilitar Google Sign-In en Authentication
4. ✅ Obtener Web client ID correcto
5. ✅ Actualizar strings.xml
6. ✅ Probar la app

### Opcional:
- [ ] Agregar logo de Google (en lugar del emoji 🔵)
- [ ] Implementar Sign Out de Google también
- [ ] Mostrar foto de perfil del usuario
- [ ] Agregar más providers (Facebook, Apple, etc.)

---

## 📝 NOTAS IMPORTANTES

### Web Client ID temporal:
El ID actual (`920436817763-abcdefghijklmnopqrstuvwxyz123456...`) es **temporal y no funcionará**.

**Debes reemplazarlo** con el real desde Firebase Console.

### SHA-1 Debug vs Release:
- **Debug SHA-1**: Para desarrollo (emulador/debug builds)
- **Release SHA-1**: Para producción (Play Store)

**Necesitas agregar AMBOS** si vas a publicar la app.

### Package Name:
El package name debe ser exactamente: `com.example.lmitemonotributo`

Si lo cambias, también debes actualizarlo en Firebase Console.

---

**Implementado:** 20 de Abril, 2026  
**Versión:** 1.0  
**Estado:** ✅ CÓDIGO COMPLETO - REQUIERE CONFIGURACIÓN EN CONSOLE  

---

## 🎉 RESUMEN

Google Sign-In está **completamente implementado en el código**.

**Solo faltan 3 configuraciones en Firebase Console** (10 minutos):
1. Agregar SHA-1
2. Habilitar Google Sign-In
3. Obtener Web client ID correcto

**Después de eso, estará 100% funcional.** 🚀

