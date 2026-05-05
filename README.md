# 🇦🇷 MonoControl - Monotributo Argentino

**Tu monotributo bajo control** • Facturación. Categorías. Alertas. 

---

## 🔥 Firebase Authentication - Configurado

La app incluye autenticación completa con Firebase:
- ✅ Login y registro con **email/contraseña**
- ✅ Login y registro con **Google Sign-In** 🔵 NUEVO
- ✅ Gestión de sesiones persistentes
- ✅ Roles de usuario (Personal/Contador)
- ✅ Proyecto Firebase: `profile-68864`

Ver **[FIREBASE_IMPLEMENTACION.md](FIREBASE_IMPLEMENTACION.md)** y **[GOOGLE_SIGNIN_IMPLEMENTACION.md](GOOGLE_SIGNIN_IMPLEMENTACION.md)** para detalles.

---

## 📱 Características principales

### 🔐 **Autenticación**
- **Login con email/contraseña** - Firebase Authentication
- **Login con Google** 🔵 - Google Sign-In (OAuth 2.0)
- **Registro de nuevos usuarios** - Con validación completa
- **Roles diferenciados**:
  - 👤 **Personal**: Usuario individual que controla su propio monotributo
  - 🧮 **Contador**: Profesional que gestiona múltiples clientes
- **Sesión persistente** - No necesitas loguearte cada vez
- **Datos automáticos** - Email y nombre desde Google

### 🏠 **Inicio (Home)**
- **Total facturado** del mes actual
- **Categoría actual** con badge dorado
- **Progreso al límite** con barra visual (🟢 verde → 🟡 amarillo → 🔴 rojo)
- **Ingresos vs Gastos** con separador dorado

### 💰 **Ingresos (Movimientos)**
- **Formulario rápido** Ultra UX (2 clics para agregar)
- Selector Ingreso/Gasto con emojis
- **Resumen del día** (Neto = Ingresos - Gastos)
- **Historial ordenado** por fecha descendente (recientes primero)
- Códigos de color: 🟢 ingresos / 🔴 gastos

### 📊 **Categoría**
- **Tu categoría actual** con indicador dorado "Activa"
- **Información fiscal**: cuota mensual, próxima recategorización, alerta de límite
- **Semáforo visual** 🔴🟡🟢 indicador de riesgo (útil para contador)
- **Tabla completa A→K** con límites 2025

### 🔮 **Simulación**
- "¿Qué pasa si facturo X más?"
- Muestra si **te recategorizás** (con flecha)
- Calcula el **impacto en la cuota mensual**
- Tabla de referencia con categorías resaltadas

### 🔔 **Alertas**
- **Contador de pendientes** con badge
- Barra de progreso en cada alerta (% del límite)
- Marcar como leída ✅
- Semáforo 🔴🟡🔵 por urgencia

### 👤 **Perfil**
- Selecciona tu tipo: 🧾 Monotributista / 🧮 Contador / 🏢 Empresa
- Datos: Nombre + CUIT
- **Notas del contador** (para asesores con múltiples clientes)

---

## 🎨 Identidad visual

**Colores de la Bandera Argentina:**
- **Celeste** `#75AADB` — Pantone 284C — Botones, header, acciones
- **Blanco** `#FFFFFF` — Fondos, cards, contraste
- **Oro (Sol de Mayo)** `#FBB81C` — Pantone 1235C — Acentos, alertas
- **Texto oscuro** `#0D2A4A` — Legibilidad

**Dark Mode:**
- Fondo: `#0D1B2A` (azul muy oscuro)
- Surfaces: `#162840`

---

## 🧭 Navegación

| Tab | Ruta | Ícono |
|---|---|---|
| Inicio | `/home` | 🏠 |
| Ingresos | `/movimientos` | 📋 |
| Categoría | `/categoria` | ⭐ |
| Simulación | `/simulacion` | ⋯ |
| Alertas | `/alertas` | 🔔 |
| Perfil | `/perfil` | 👤 |

**Flujo onboarding:**
```
Splash (2s) → Login → Selección de Categoría → Home (Main App)
```

---

## 📦 Estructura del proyecto

```
app/src/main/java/com/example/lmitemonotributo/

├── data/
│   ├── local/          (Room Database)
│   ├── remote/         (API, Excel import/export)
│   └── repository/     (Data access layer)
│
├── domain/
│   ├── model/          (Dominio: Movimiento, CategoriaMonotributo, etc)
│   └── usecase/        (Lógica de negocio)
│
├── ui/
│   ├── home/
│   ├── movimientos/
│   ├── categoria/
│   ├── simulacion/
│   ├── alertas/
│   ├── perfil/
│   ├── dashboard/
│   ├── login/
│   ├── splash/
│   ├── onboarding/
│   ├── excel/
│   ├── components/     (UI reutilizable)
│   └── theme/          (Colores, tipografía)
│
└── navigation/         (NavGraph, Routes)
```

---

## 🚀 Tecnología

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose + Material3
- **Database:** Room (SQLite)
- **Architecture:** MVVM + Repository Pattern
- **Nav:** Jetpack Navigation Compose

---

## 💡 Casos de uso

### 👤 Para Monotributistas:
✅ Controlar facturación vs límite anual  
✅ Saber si se recategorizan  
✅ Recibir alertas automáticas  
✅ Exportar reporte para contador  

### 🧮 Para Contadores:
✅ Gestionar múltiples clientes  
✅ Ver semáforo de riesgo por cliente  
✅ Notas internas por cliente  
✅ Generar reportes profesionales  

---

## 📋 Categorías 2025

| Cat | Límite anual | Cuota/mes |
|---|---|---|
| A | $7.4M | $7,500 |
| B | $11M | $12,500 |
| C | $15.6M | $18,000 |
| D | $21.6M | $26,000 |
| E | $26.6M | $42,000 |
| F | $31.6M | $55,000 |
| G | $36.7M | $72,000 |
| H | $45.6M | $115,000 |
| I | $52.7M | $155,000 |
| J | $59.3M | $200,000 |
| K | $68.7M | $250,000 |

---

## 🚀 Instalación y Configuración

### Requisitos
- Android Studio Hedgehog (2023.1.1) o superior
- JDK 11
- Android SDK 26+
- Cuenta de Firebase (gratuita)

### Pasos de instalación

1. **Clona el repositorio**
```bash
git clone [tu-repo]
cd LmiteMonotributo
```

2. **Abre el proyecto en Android Studio**
```bash
open -a "Android Studio" .
```

3. **Configura Firebase** (⚠️ IMPORTANTE)

El archivo `app/google-services.json` ya está incluido con las credenciales del proyecto `profile-68864`.

**Debes habilitar Email/Password en la consola:**
1. Ve a: https://console.firebase.google.com/project/profile-68864/authentication
2. Click en "Get started" (primera vez)
3. En "Sign-in method", habilita "Email/Password"
4. Guarda los cambios

Ver **[FIREBASE_IMPLEMENTACION.md](FIREBASE_IMPLEMENTACION.md)** para más detalles.

4. **Compila y ejecuta**
```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

O simplemente presiona ▶️ Run en Android Studio.

### Estructura del proyecto

```
app/src/main/java/com/example/lmitemonotributo/
├── data/
│   ├── local/          # Room database, DAOs, entities
│   └── repository/     # AuthRepository, PerfilRepository, etc.
├── domain/
│   └── usecase/        # Lógica de negocio (casos de uso)
├── navigation/         # NavGraph de Compose
├── ui/
│   ├── clientes/       # Pantallas de gestión de clientes
│   ├── home/           # Pantalla principal
│   ├── login/          # Login y registro
│   ├── perfil/         # Perfil de usuario
│   └── components/     # Componentes reutilizables
└── utils/              # Helpers y utilidades
```

---

## 🔮 Próximas mejoras

- [x] Autenticación con Firebase
- [x] Roles de usuario (Personal/Contador)
- [x] Multi-cliente (para contadores)
- [ ] Notificaciones push
- [ ] Integración con bancos
- [ ] Análisis predictivo (ML)
- [ ] Modo offline
- [ ] Temas personalizables

---

## 📞 Contacto

Diseñado para **CABA y Argentina** 🇦🇷  
Régimen: **Monotributo 2025**

---

**v1.0** • Made with ❤️ for Argentine entrepreneurs

