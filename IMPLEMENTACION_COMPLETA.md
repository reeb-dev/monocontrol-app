# 🇦🇷 Límite Monotributo - Resumen de Implementación

## ✅ Todo lo que se implementó

### 🔐 **Firebase Authentication** (Email + Contraseña)
- **Login real** con manejo de errores claros en español
- **Registro** con selector de rol (Personal / Contador)
- **Recupero de contraseña** por email
- **Onboarding** post-registro para precargar nombre, CUIT y rubro
- Logout real con cierre de sesión de Firebase
- Auto-redireccionamiento desde Splash según sesión

### 👤 **Sistema de Roles**
#### **Personal**
- Controla su propio Monotributo
- Vista simplificada sin gestión de clientes
- FAB sin opción "Cliente"
- Perfil sin sección "Clientes guardados"

#### **Contador**
- Administra varios clientes
- Tiene acceso completo a:
  - Gestión de clientes (búsqueda, agregar, eliminar)
  - Exportación a Excel
  - Simulaciones
  - Modo prueba (cambio de categoría manual)

**Toggle de rol en Perfil**: cualquier usuario puede alternar entre vistas (Personal ↔ Contador) desde la tarjeta "Modo de uso" en su perfil.

### 📊 **Topes dinámicos y validación inteligente**
- **Campo Monto** con regex que valida:
  - Solo números + separador decimal (`.` o `,`)
  - Hasta 10 dígitos enteros + 2 decimales
  - Tope dinámico según categoría del usuario
- **Preview formateado** en vivo mientras se tipea
- **Advertencias automáticas**:
  - ⚠️ "Te recategorizás" (pasás de cat. D → F)
  - ⛔ "Quedarías fuera del régimen" (superás cat. K)
- Validación por rol: si es ingreso, no puede superar `K.limiteAnual - totalAnual`

### 🔔 **Notificaciones mejoradas**
- Incluyen **categoría actual** y **próxima categoría** en el mensaje
- Ejemplo: *"🔴 Estás al 92% del límite (cat. H). Si seguís facturando pasarías a la categoría I."*
- Se disparan automáticamente al llegar al 70% del límite

### 🌐 **Configuración remota (tarifas + donaciones)**
- `app_config.json` en `assets/` como fallback
- `RemoteConfigRepository` intenta descargar desde URL remota (GitHub raw recomendado)
- Caché local en `SharedPreferences`
- Botón "Buscar actualizaciones" en Perfil que muestra:
  - ✓ "Actualizado a la versión X"
  - "Ya estás en la última versión"
  - "No se pudo actualizar (sin internet)"
- Actualización del campo `version` en el JSON dispara notificación de novedad

### 🎨 **UX mejorada**
- **Scroll funcional** en pantalla de Ingresos/Gastos (LazyColumn único)
- **Sin doble AppBar**: header celeste del Home reemplazado por chip de rubro
- **Onboarding** visual con validación de CUIT (11 dígitos)
- **Toggle de rol** en Perfil con tarjetas seleccionables + iconos
- **FAB adaptativo** según rol (oculta "Cliente" para Personal)

### 📧 **Notificaciones por Email para Contadores**
- **Campo email en clientes**: cada cliente puede tener un email asociado
- **Botón de recordatorio** en la tarjeta de cliente (icono 📧)
- **Email automático** con:
  - Nombre del cliente y CUIT
  - Categoría actual
  - Monto de cuota mensual
  - Total facturado en el año
  - Fecha de vencimiento (día 20 del mes)
  - Link directo a AFIP Monotributo
- **Usa la app de email predeterminada** (Gmail, Outlook, etc.)
- No requiere backend ni servicios externos

---

## 📁 Estructura de archivos clave

```
app/src/main/
├── assets/
│   └── app_config.json              # Tarifas y donaciones (fallback)
├── java/.../
│   ├── data/
│   │   ├── remote/
│   │   │   ├── RemoteConfig.kt      # Modelo del JSON
│   │   │   ├── RemoteConfigRepository.kt
│   │   │   └── RemoteConfigHolder.kt
│   │   └── repository/
│   │       └── AuthRepository.kt    # Firebase Auth wrapper
│   ├── domain/model/
│   │   └── UserRole.kt              # PERSONAL | CONTADOR
│   ├── domain/usecase/
│   │   └── EnviarRecordatorioEmailUseCase.kt  # Genera intent de email
│   ├── ui/
│   │   ├── login/
│   │   │   ├── AuthViewModel.kt
│   │   │   ├── LoginScreen.kt       # Email + password real
│   │   │   └── RegisterScreen.kt    # Con selector de rol
│   │   ├── onboarding/
│   │   │   └── OnboardingScreen.kt  # Post-registro: nombre/CUIT/rubro
│   │   ├── perfil/
│   │   │   ├── PerfilViewModel.kt   # + cambiarRol()
│   │   │   └── PerfilScreen.kt      # + RolToggleCard + TarifasRemotasCard
│   │   ├── home/
│   │   │   ├── HomeViewModel.kt     # + rol en HomeState
│   │   │   └── HomeScreen.kt        # FAB adaptativo
│   │   └── movimientos/
│   │       ├── MovimientoViewModel.kt # + TopeState
│   │       └── MovimientoScreen.kt    # Tope dinámico + avisos
│   └── utils/
│       ├── AlertGenerator.kt        # Mensajes con categorías
│       └── NotificacionHelper.kt    # Notif con cat. actual/próxima
└── SETUP_FIREBASE.md                # Instrucciones paso a paso
```

---

## 🚀 Cómo probarlo

### 1. **Sin Firebase** (solo compilación local)
```bash
./gradlew :app:assembleDebug
```
La app compila y arranca, pero al intentar login/registro muestra: *"Firebase no está configurado todavía."*

### 2. **Con Firebase** (funcional completo)
1. Seguir `SETUP_FIREBASE.md`:
   - Crear proyecto en Firebase Console
   - Registrar package `com.example.lmitemonotributo`
   - Descargar `google-services.json` → pegar en `app/`
   - Habilitar Email/Password en Authentication
2. Sincronizar Gradle
3. Correr la app → ya funciona login/registro real

### 3. **Flujo de usuario nuevo**
1. **Splash** → redirige a **Login**
2. Click "Registrate" → **Register** (elegir rol: Personal o Contador)
3. → **Onboarding** (nombre, CUIT, rubro)
4. → **Home** (ya logueado)
5. **Perfil** → "Modo de uso" para cambiar rol Personal ↔ Contador
6. **FAB** en Home: si es Personal, no ve "Cliente"; si es Contador, sí
7. **Movimientos** → campo Monto valida y avisa si cambia de categoría

---

## 🔧 Configuración remota (opcional)

Para actualizar tarifas/donaciones sin recompilar:

1. Subir `app_config.json` a GitHub (repo público):
   ```
   https://github.com/<usuario>/<repo>/main/app_config.json
   ```
2. Copiar URL "raw" (botón Raw en GitHub)
3. Pegar en `RemoteConfigRepository.kt` → `REMOTE_URL`
4. Al cambiar el JSON remoto, incrementar `"version": 2`
5. Usuario → Perfil → "Buscar actualizaciones" → descarga el nuevo JSON

---

## 📝 Notas finales

- **Base de datos local**: `AppDatabase` v6 (campos `email` + `rol` en `PerfilEntity`)
- **Errores de compilación**: solo **warnings** (iconos deprecated, imports no usados) — **no bloquean el build**
- **Dependencias**:
  - Firebase BoM `33.5.1`
  - `firebase-auth-ktx`
  - `kotlinx-coroutines-play-services` (para `Task.await()`)
  - Room, Compose, Navigation, Apache POI (ya estaban)

---

## ✨ Próximas mejoras sugeridas

- [ ] Verificación de email obligatoria (Firebase → Settings → Email verification)
- [ ] Google Sign-In como alternativa
- [ ] Sincronización de datos entre dispositivos (Firestore)
- [ ] Push notifications para vencimientos de cuota
- [ ] Modo offline completo con sync cuando vuelve la conexión
- [ ] Dashboard del contador con resumen de todos sus clientes

---

**Hecho por:** Manuel Reeb  
**Última actualización:** Enero 2026  
**Estado:** ✅ Compilando sin errores, listo para producción

