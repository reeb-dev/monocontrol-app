# 🔧 SETUP & DESARROLLO

## Requisitos previos

- Android Studio Hedgehog o superior
- JDK 17+
- Gradle 8.x

## 🚀 Primeros pasos

### 1. Clonar / Abrir proyecto
```bash
git clone <repo-url>
cd LmiteMonotributo
```

### 2. Build & Run
```bash
./gradlew build
./gradlew installDebug
```

O desde Android Studio: `Run > Run 'app'`

## 📁 Estructura de carpetas importante

```
app/src/main/
├── AndroidManifest.xml
├── java/com/example/lmitemonotributo/
│   ├── MainActivity.kt          ← Punto de entrada
│   ├── navigation/
│   │   ├── AppNavGraph.kt       ← Rutas
│   │   └── Routes.kt
│   ├── ui/
│   │   └── theme/
│   │       ├── Color.kt         ← Paleta Argentina
│   │       ├── Theme.kt         ← Material3 Setup
│   │       └── Type.kt
│   ├── data/
│   │   ├── local/
│   │   │   └── AppDatabase.kt   ← Room DB
│   │   └── repository/          ← Data Layer
│   └── domain/
│       └── model/               ← Dominio (Sin dependencias)
└── res/
    ├── drawable/
    │   └── logo1.png            ← 🇦🇷 Sol de Mayo
    └── values/
        ├── colors.xml
        └── strings.xml
```

## 🎯 Flujo de desarrollo

### Agregar una nueva pantalla

1. **Crear composable** en `ui/nuevapantalla/NuevaPantallaScreen.kt`
```kotlin
@Composable
fun NuevaPantallaScreen() {
    // Tu UI aquí
}
```

2. **Agregar ruta** en `navigation/Routes.kt`
```kotlin
const val NUEVA_PANTALLA = "nueva_pantalla"
```

3. **Agregar al NavGraph** en `navigation/AppNavGraph.kt`
```kotlin
composable(Routes.NUEVA_PANTALLA) {
    NuevaPantallaScreen()
}
```

4. **Agregar a barra de nav** (si es principal) en `MainActivity.kt`
```kotlin
NavItem("Label", Icons.Default.Icon, Routes.NUEVA_PANTALLA)
```

### Usar colores de la bandera

En cualquier composable:
```kotlin
private val Celeste     = Color(0xFF75AADB)   // Primary
private val Amarillo    = Color(0xFFFBB81C)   // Accents
private val Blanco      = Color(0xFFFFFFFF)   // Surface

Box(modifier = Modifier.background(Celeste))
```

### Agregar un ViewModel

```kotlin
@HiltViewModel
class MiViewModel @Inject constructor(
    private val repository: MiRepository
) : ViewModel() {
    val estado = MutableStateFlow<UiState>(UiState.Loading)
    
    init {
        viewModelScope.launch {
            // Cargar datos
        }
    }
}
```

## 🧪 Testing (próximo)

```kotlin
@RunWith(AndroidJUnit4::class)
class MiScreenTest {
    @get:Rule
    val composeRule = createComposeRule()
    
    @Test
    fun testRender() {
        composeRule.setContent {
            MiScreen()
        }
        composeRule.onNodeWithText("Texto").assertExists()
    }
}
```

## 🐛 Debugging

### Logcat
```kotlin
Log.d("TAG", "Mensaje: $variable")
```

### Database Inspector (Android Studio)
```
View > Tool Windows > Device File Explorer
> /data/data/com.example.lmitemonotributo/databases
```

### Compose Inspector
```
Tools > Layout Inspector
```

## 📦 Dependencias principales

```toml
# build.gradle.kts

androidx-compose-bom = "2024.01.00"
androidx-lifecycle = "2.7.0"
androidx-navigation = "2.7.5"
androidx-room = "2.6.1"
androidx-datastore = "1.0.0"
hilt-android = "2.50"
retrofit = "2.10.0"
```

## 🔐 Secrets & Config

Para APIs (si se agrega):
```
Create: local.properties
GOOGLE_CLIENT_ID=<tu_id>
API_KEY=<tu_key>
```

Nunca commitear `local.properties` 🔒

## 📈 Performance Tips

1. **Lazy loading**: Usar `LazyColumn` en listas largas
2. **Recomposición**: Evitar lambdas inline (`key {}`)
3. **Images**: Usar `painterResource()` con caching
4. **Memory**: Usar `rememberUpdatedState()` para valores

## 🚢 Build & Release

### Debug
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release (signing)
```bash
./gradlew bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab
```

## 📋 Checklist antes de release

- [ ] Versión en `build.gradle.kts` actualizada
- [ ] `AndroidManifest.xml` OK
- [ ] Permisos necesarios declarados
- [ ] Strings en `strings.xml` (no hardcoded)
- [ ] Tests pasando
- [ ] Screenshots/GIFs actualizados

## 🤝 Contribuir

1. Fork el proyecto
2. Branch: `git checkout -b feature/nueva-feature`
3. Commit: `git commit -am 'Add feature'`
4. Push: `git push origin feature/nueva-feature`
5. Pull Request

## 📚 Recursos

- [Compose Docs](https://developer.android.com/develop/ui/compose)
- [Material Design 3](https://m3.material.io)
- [Room + Database](https://developer.android.com/training/data-storage/room)
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)

---

**Happy coding! 🚀 🇦🇷**

