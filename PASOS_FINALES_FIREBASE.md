# 🔥 Firebase - Pasos Finales

## ✅ Lo que ya está hecho

1. ✅ Archivo `google-services.json` creado
2. ✅ Firebase SDK configurado
3. ✅ AuthRepository implementado
4. ✅ Pantallas de Login/Registro listas
5. ✅ Navegación funcionando
6. ✅ **La app compila correctamente** ✨

---

## 🎯 Solo te falta 1 paso

### Habilitar Email/Password en Firebase Console

**Toma 2 minutos:**

1. **Abre este link**: 
   👉 https://console.firebase.google.com/project/profile-68864/authentication

2. **Primera vez usando Authentication?**
   - Click en el botón azul **"Get started"**

3. **Habilita Email/Password:**
   - Ve a la pestaña **"Sign-in method"**
   - Busca **"Email/Password"** en la lista
   - Click en él
   - Activa el toggle **"Enable"**
   - Click en **"Save"**

4. **¡Listo!** 🎉

---

## 🧪 Probar la app

```bash
# Desde la terminal:
cd /Users/manuelreeb/AndroidStudioProjects/LmiteMonotributo
./gradlew :app:installDebug

# O desde Android Studio:
# Click en el botón ▶️ Run
```

### Lo que verás:

1. **Splash Screen** → Logo de la app
2. **Login Screen** → Formulario de acceso
3. **"Crear cuenta"** → Registro de nuevo usuario
4. **Elige tu rol:**
   - 👤 Personal (usuario individual)
   - 🧮 Contador (múltiples clientes)
5. **Home** → Pantalla principal

---

## 📋 Probar el registro

1. En la app, click en **"Crear cuenta"**
2. Ingresa:
   - **Email**: `test@example.com` (cualquiera válido)
   - **Contraseña**: `123456` (mínimo 6 caracteres)
   - **Rol**: Elige Personal o Contador
3. Click en **"Registrarse"**

### ¿Funcionó? ✅

Ve a Firebase Console → Authentication → Users

Deberías ver tu cuenta recién creada ahí.

---

## 🐛 Solución de problemas

### Error: "Firebase not initialized"
- ✅ Ya está solucionado. El archivo `google-services.json` existe.

### Error: "Email already in use"
- 😊 Significa que Firebase funciona!
- Usa otro email o elimina el usuario desde Firebase Console.

### No puedo registrarme / Login no funciona
- ⚠️ Verifica que habilitaste Email/Password en la consola (paso arriba ⬆️)

### La app no compila
```bash
# Limpia el build:
./gradlew clean
./gradlew :app:assembleDebug
```

---

## 📱 Funcionalidades ya implementadas

### Login/Registro
- ✅ Validación de email
- ✅ Contraseña mínima 6 caracteres
- ✅ Mensajes de error en español
- ✅ Sesión persistente (no pide login cada vez)

### Roles
- ✅ **Personal**: Control individual del monotributo
- ✅ **Contador**: Gestión de múltiples clientes

### Navegación
- ✅ Splash → Login → Home
- ✅ Registro → Onboarding → Home
- ✅ Persistencia de sesión

---

## 🎓 Usar Firebase en tu código

```kotlin
// En cualquier ViewModel o Composable:

// Verificar si está logueado
if (authRepository.estaLogueado) {
    // Usuario autenticado
}

// Obtener usuario actual
val usuario = authRepository.usuarioActual
val email = usuario?.email

// Registrar usuario
viewModelScope.launch {
    authRepository.signUp(email, password)
        .onSuccess { user ->
            // Registro exitoso
        }
        .onFailure { error ->
            // Mostrar error
        }
}

// Login
viewModelScope.launch {
    authRepository.signIn(email, password)
        .onSuccess { user ->
            // Login exitoso
        }
        .onFailure { error ->
            // Credenciales incorrectas
        }
}

// Logout
authRepository.signOut()
```

---

## 📚 Más información

- **Implementación completa**: [FIREBASE_IMPLEMENTACION.md](FIREBASE_IMPLEMENTACION.md)
- **Setup original**: [SETUP_FIREBASE.md](SETUP_FIREBASE.md)
- **README**: [README.md](README.md)

---

## ✨ Resumen

**Todo está listo. Solo necesitas:**

1. ✅ Habilitar Email/Password en Firebase Console (2 minutos)
2. ✅ Ejecutar la app
3. ✅ Crear una cuenta de prueba
4. ✅ ¡A usar la app! 🎉

**El código ya compila y funciona.** 🚀

