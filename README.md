# MonoControl

App Android (**Kotlin**, **Jetpack Compose**, **Room**) para monotributistas y contadores: movimientos, categorías, alertas, clientes, reportes y más.

## Identificación del build

| Campo | Valor |
|--------|--------|
| **applicationId** | `com.reeb.controlmonotributoar` |
| **namespace** | `com.reeb.controlmonotributoar` |

El código fuente puede estar bajo carpetas legacy con otro path físico; el **`package`** en Kotlin y el `applicationId` son los que definen la app en el sistema y en Play Console.

## Requisitos

- Android Studio reciente
- JDK 11 (según `app/build.gradle.kts`)

## Compilar

```bash
./gradlew :app:assembleDebug
```

Release / AAB:

```bash
./gradlew :app:bundleRelease
```

## Documentación

| Archivo | Contenido |
|---------|-----------|
| [docs/FIREBASE.md](docs/FIREBASE.md) | Firebase, `google-services.json`, SHA, Auth |
| [docs/PUBLICACION.md](docs/PUBLICACION.md) | Versión, firma, Play Console, AAB |
| [DESCRIPCION_PLAYSTORE.md](DESCRIPCION_PLAYSTORE.md) | Texto para la ficha de Play Store |
| [CHANGELOG.md](CHANGELOG.md) | Historial de versiones |
| [PRIVACY_POLICY.md](PRIVACY_POLICY.md) | Política de privacidad (enlace en tienda / soporte) |

## Arquitectura (resumen)

MVVM, capas `data` (Room, repos remotos), `domain` (modelos y casos de uso) y `ui` (Compose). Navegación con Navigation Compose.

---

Argentina · Monotributo
