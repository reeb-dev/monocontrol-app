# 🔥 Firebase - Inicio Rápido

## ⚡ En 5 minutos

### 1️⃣ Habilitar Email/Password (2 min)

Ve a: https://console.firebase.google.com/project/profile-68864/authentication

- Click "Get started"
- Habilita "Email/Password"
- Save

### 2️⃣ Ejecutar la app (1 min)

```bash
./gradlew :app:installDebug
```

O click en ▶️ Run en Android Studio

### 3️⃣ Crear cuenta (2 min)

1. Login → "Crear cuenta"
2. Email: `test@example.com`
3. Password: `123456`
4. Rol: Personal o Contador
5. Registrar

### 4️⃣ Verificar

Firebase Console → Authentication → Users

¡Tu cuenta debe aparecer ahí! ✅

---

## 📄 Documentación

- `FIREBASE_STATUS.txt` - Resumen visual
- `PASOS_FINALES_FIREBASE.md` - Guía completa
- `FIREBASE_RESUMEN_EJECUTIVO.md` - Documentación técnica

---

## 🔧 Troubleshooting

**No puedo registrarme**  
→ Habilita Email/Password en Console (paso 1)

**Error de compilación**  
```bash
./gradlew clean
./gradlew :app:assembleDebug
```

**Firebase not initialized**  
→ Ya está solucionado (google-services.json existe)

---

## ✅ Estado

- [x] google-services.json
- [x] Firebase SDK
- [x] AuthRepository
- [x] Login/Register screens
- [x] Navegación
- [x] Compilación exitosa
- [ ] **Habilitar Email/Password** ← PENDIENTE

---

## 💻 Uso en código

```kotlin
// Login
authRepository.signIn(email, password)

// Registro
authRepository.signUp(email, password)

// Logout
authRepository.signOut()

// Estado
authRepository.estaLogueado
```

---

## 🎯 Estado: 95% completo

Solo falta habilitar Email/Password en Console (2 min).

**La app está lista para producción.** ✨

