# 🏗️ ARQUITECTURA DEL PROYECTO

## Patrón: MVVM + Repository Pattern

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                       │
│                   (Jetpack Compose)                         │
├─────────────────────────────────────────────────────────────┤
│  UI Screens          │  ViewModels        │  Events         │
│  ├─ HomeScreen       │  ├─ HomeViewModel  │  └─ User Input  │
│  ├─ MovimientoScreen │  ├─ MvmViewModel   │                 │
│  ├─ CategoriaScreen  │  ├─ AlertViewModel │                 │
│  └─ ... más          │  └─ ... más        │                 │
│                      │                    │                 │
└─────────────────────────────────────────────────────────────┘
                            ↓ (Flow/StateFlow)
┌─────────────────────────────────────────────────────────────┐
│                     DOMAIN LAYER                            │
│              (Lógica de negocio - Sin dependencias)         │
├─────────────────────────────────────────────────────────────┤
│  Use Cases                          │  Models               │
│  ├─ AgregarMovimientoUseCase        │  ├─ Movimiento       │
│  ├─ ObtenerResumenMensualUseCase    │  ├─ CategoriaMonot.. │
│  ├─ CalcularCategoriaUseCase        │  ├─ Alerta           │
│  ├─ SimularRecategUseCase           │  ├─ ResumenMensual   │
│  └─ GenerarAlertasUseCase           │  └─ Usuario          │
│                                     │                       │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    DATA LAYER                               │
│          (Fuentes de datos - Abstracciones)                 │
├─────────────────────────────────────────────────────────────┤
│  Repository Pattern                                         │
│  ├─ MovimientoRepository                                    │
│  ├─ AlertaRepository                                        │
│  ├─ UsuarioRepository                                       │
│  └─ CategoriaRepository                                     │
│                                                             │
└─────────────────────────────────────────────────────────────┘
         ↓ Local              ↓ Remote              ↓ Config
    ┌─────────────┐      ┌──────────────┐      ┌──────────┐
    │Room Database│      │API (Future)  │      │Preferences│
    └─────────────┘      └──────────────┘      └──────────┘
```

---

## 📁 Estructura de carpetas detallada

```
com.example.lmitemonotributo/
│
├── 📱 ui/                          (PRESENTATION)
│   ├── home/
│   │   ├── HomeScreen.kt           (Composable)
│   │   └── HomeViewModel.kt        (MVVM)
│   ├── movimientos/
│   │   ├── MovimientoScreen.kt
│   │   ├── MovimientoViewModel.kt
│   │   └── MovimientoUiState.kt
│   ├── categoria/
│   │   ├── CategoriaScreen.kt
│   │   └── CategoriaViewModel.kt
│   ├── simulacion/
│   │   └── SimulacionScreen.kt
│   ├── alertas/
│   │   ├── AlertasScreen.kt
│   │   └── AlertasViewModel.kt
│   ├── perfil/
│   │   └── PerfilScreen.kt
│   ├── dashboard/
│   │   └── DashboardScreen.kt
│   ├── login/
│   │   └── LoginScreen.kt
│   ├── splash/
│   │   └── SplashScreen.kt
│   ├── onboarding/
│   │   └── SeleccionCategoriaScreen.kt
│   ├── excel/
│   │   ├── ExcelScreen.kt
│   │   └── ExcelViewModel.kt
│   ├── components/              (UI reutilizable)
│   │   ├── SolDeMayo.kt
│   │   └── ... más
│   └── theme/                   (Design system)
│       ├── Color.kt             (Paleta Argentina 🇦🇷)
│       ├── Theme.kt             (Material3 + Compose)
│       └── Type.kt              (Tipografía)
│
├── 🧠 domain/                     (DOMAIN LOGIC)
│   ├── model/
│   │   ├── Movimiento.kt
│   │   ├── CategoriaMonotributo.kt  (A→K 2025)
│   │   ├── Alerta.kt
│   │   ├── ResumenMensual.kt
│   │   ├── Usuario.kt
│   │   └── TipoMovimiento.kt       (Enum INGRESO/GASTO)
│   └── usecase/
│       ├── AgregarMovimientoUseCase.kt
│       ├── ObtenerResumenMensualUseCase.kt
│       ├── CalcularCategoriaActualUseCase.kt
│       ├── SimularRecategorizacionUseCase.kt
│       └── GenerarAlertasUseCase.kt
│
├── 📊 data/                       (DATA LAYER)
│   ├── local/
│   │   ├── AppDatabase.kt         (Room)
│   │   ├── MovimientoDao.kt
│   │   ├── AlertaDao.kt
│   │   └── MovimientoEntity.kt
│   ├── remote/
│   │   ├── ApiService.kt          (Retrofit - futuro)
│   │   └── ExcelService.kt
│   └── repository/
│       ├── MovimientoRepository.kt
│       ├── AlertaRepository.kt
│       ├── CategoriaRepository.kt
│       └── UsuarioRepository.kt
│
├── 🧭 navigation/
│   ├── AppNavGraph.kt             (Rutas + composables)
│   └── Routes.kt                  (Constantes)
│
├── 🛠️ utils/
│   ├── CurrencyFormatter.kt        ($$$)
│   ├── DateUtils.kt               (Fechas)
│   ├── CategoriaCalculator.kt      (⭐ Core logic)
│   └── AlertGenerator.kt           (🔔)
│
├── 📱 MainActivity.kt              (Punto de entrada)
└── 📁 res/                         (Recursos)
    ├── drawable/
    │   └── logo1.png              (Sol de Mayo 🇦🇷)
    └── values/
        ├── colors.xml
        └── strings.xml
```

---

## 🔄 Flujo de datos

### Ejemplo: Agregar un movimiento

```
User Input (UI)
    ↓
MovimientoScreen (Composable)
    ↓
MovimientoViewModel.agregar()
    ↓
AgregarMovimientoUseCase
    ↓
MovimientoRepository.insert()
    ↓
MovimientoDao.insertMovimiento()
    ↓
Room Database 💾
    ↓
Emit: Estado de éxito
    ↓
UI actualiza (Recompose)
```

---

## 📦 Dependencias principales

```kotlin
// build.gradle.kts

// Jetpack
androidx.compose.bom = "2024.01.00"
androidx.lifecycle:lifecycle-runtime-compose = "2.7.0"
androidx.navigation:navigation-compose = "2.7.5"
androidx.room:room-runtime = "2.6.1"

// Database
androidx.room:room-ktx = "2.6.1"

// Material Design 3
androidx.compose.material3 = "1.2.0"

// Hilt (DI - preparado para futuro)
com.google.dagger:hilt-android = "2.50"
```

---

## 🎯 Responsabilidades por capa

### Presentation Layer (UI)
- ✅ Renderizar UI
- ✅ Capturar user input
- ✅ Mostrar estados
- ✅ Navegar entre pantallas
- ❌ NO lógica de negocio

### Domain Layer
- ✅ Lógica de negocio (core)
- ✅ Use cases / operaciones
- ✅ Models / Entidades
- ✅ Sin dependencias externas
- ❌ NO acceso a datos directo

### Data Layer
- ✅ Acceso a datos (local/remote)
- ✅ Abstraer fuentes
- ✅ Mapeo Entity ↔ Model
- ✅ Orquestación
- ❌ NO lógica de negocio

---

## 🔌 Patrones de diseño usados

### 1. **MVVM (Model-View-ViewModel)**
```kotlin
// ViewModel expone Estado
class HomeViewModel @Inject constructor() : ViewModel() {
    val resumen: StateFlow<ResumenMensual> = ...
}

// UI consume Estado
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val resumen by viewModel.resumen.collectAsStateWithLifecycle()
    // Renderizar resumen
}
```

### 2. **Repository Pattern**
```kotlin
interface MovimientoRepository {
    suspend fun insert(mov: Movimiento)
    fun getAll(): Flow<List<Movimiento>>
}

class MovimientoRepositoryImpl(
    private val dao: MovimientoDao
) : MovimientoRepository { ... }
```

### 3. **Use Case Pattern**
```kotlin
class AgregarMovimientoUseCase(
    private val repo: MovimientoRepository
) {
    suspend operator fun invoke(mov: Movimiento) {
        repo.insert(mov)
    }
}
```

### 4. **State Management con Flow**
```kotlin
val estado: StateFlow<UiState> = 
    MutableStateFlow(UiState.Loading)
        .stateIn(scope, SharingStarted.Eagerly, initial)
```

---

## 🔐 Inyección de dependencias (Preparado para Hilt)

```kotlin
// Futuro setup con Hilt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "monotributo.db")
            .build()
    
    @Provides
    fun provideMovimientoDao(db: AppDatabase) = db.movimientoDao()
}
```

---

## 📊 Flujo de eventos (User Story)

```
┌─────────────────────────────────────────────────────────────┐
│ Usuario abre app                                            │
├─────────────────────────────────────────────────────────────┤
│ Splash Screen (2s)                                          │
│     ↓                                                       │
│ LoginScreen                                                 │
│     ↓                                                       │
│ SeleccionCategoriaScreen                                    │
│     ↓                                                       │
│ HomeScreen (Main App)  ← AppNavGraph routing                │
├─────────────────────────────────────────────────────────────┤
│ Usuario navega tabs:                                        │
│ Home → Movimientos → Categoría → Simulación → Alertas → Perfil
│                                                             │
│ Cada pantalla:                                              │
│ 1. Composable renderiza                                     │
│ 2. ViewModel proporciona estado                             │
│ 3. UseCase obtiene datos                                    │
│ 4. Repository consulta DB                                  │
│ 5. Datos llegan a UI                                        │
└─────────────────────────────────────────────────────────────┘
```

---

## 🧪 Testing (Estructura preparada)

```kotlin
// Unitario (Domain)
class CalcularCategoriaTest {
    @Test
    fun `debería retornar categoría A para facturación <= 7.4M`() {
        val categoria = CategoriaCalculator.calcular(5_000_000.0)
        assertEquals(CategoriaMonotributo.A, categoria)
    }
}

// Integration (Repository)
class MovimientoRepositoryTest {
    @Test
    fun `debería insertar y recuperar movimiento`() = runTest {
        // Arrange
        val mov = Movimiento(...)
        // Act
        repository.insert(mov)
        // Assert
        val result = repository.getAll().first()
        assertTrue(result.contains(mov))
    }
}

// UI (Compose)
class HomeScreenTest {
    @Test
    fun `debería mostrar categoría actual`() {
        composeRule.setContent {
            HomeScreen(viewModel)
        }
        composeRule.onNodeWithText("Categoría").assertExists()
    }
}
```

---

## 🚀 Escalabilidad

### Agregar nueva Feature (ej: "Exportar PDF")

1. **Domain**: Crear `ExportarPdfUseCase.kt`
2. **Data**: Agregar `PdfRepository.kt`
3. **Presentation**: Crear `ExportScreen.kt` + `ExportViewModel.kt`
4. **Navigation**: Agregar ruta en `Routes.kt` + `AppNavGraph`
5. **UI**: Agregar tab en `MainActivity.kt`

**Total: 5 archivos, arquitectura clara, fácil testing**

---

## 📈 Performance

- ✅ Lazy loading en listas (`LazyColumn`)
- ✅ State hoisting para recomposición eficiente
- ✅ Flow para streams reactivos
- ✅ Room para DB local rápida
- ✅ Compose compiler optimization

---

## 🎓 Conclusión

La arquitectura **MVVM + Repository** permite:

1. **Mantenibilidad**: Código organizado y predecible
2. **Testabilidad**: Capas desacopladas = tests fáciles
3. **Escalabilidad**: Agregar features sin quebrar lo existente
4. **Colaboración**: Equipos pueden trabajar en paralelo
5. **Reutilización**: Componentes reutilizables

**= Desarrollo sostenible a largo plazo 🚀**

---

*Arquitectura diseñada por Manuel Reeb • 2025 • 🇦🇷*

