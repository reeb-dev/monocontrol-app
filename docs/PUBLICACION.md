# Publicación en Play Store

Guía mínima para generar el bundle y subir actualizaciones.

## Versión en Gradle

En `app/build.gradle.kts`:

- `versionCode`: entero que debe **subir** en cada release que subís a Play Console.
- `versionName`: texto visible para usuarios (ej. `1.0.2`).

Después de cambiar versión, compilá **release** y subí el `.aab`.

## Firma (release)

1. Keystore: archivo `.jks` y alias conocidos; guardá **contraseñas** en un lugar seguro (no commitear secretos en el repo).
2. En Android Studio: **Build → Generate Signed Bundle / APK → Android App Bundle**.
3. O por línea de comandos (ajustá rutas y propiedades según tu entorno):

```bash
./gradlew :app:bundleRelease
```

El AAB suele quedar en `app/build/outputs/bundle/release/app-release.aab`.

## Play Console

1. **Release → Producción (o prueba interna)** → Crear release nuevo.
2. Subí el `.aab`, completá notas de la versión y publicá/revisá.
3. Si cambiaste el **applicationId**, Play Console lo trata como **otra aplicación**: hay que crear una ficha nueva o volver al ID anterior si ya tenías usuarios en el ID viejo.

## Política de privacidad (URL obligatoria)

Configurá en Play Console la URL pública de la política. Guía: **[POLITICA_PLAY_CONSOLE.md](POLITICA_PLAY_CONSOLE.md)** (GitHub Pages con `docs/index.html`).

## Screenshots y texto de tienda

Usá `DESCRIPCION_PLAYSTORE.md` en la raíz como base para la descripción corta/larga. Las capturas: teléfono obligatorio; tablet opcional según políticas vigentes.

## SHA para Firebase / Google Sign-In

Si Play firma la app con **Play App Signing**, la firma que ven los usuarios es la de Google. Copiá los SHA-1 y SHA-256 desde Play Console (**Integridad de la app**) y agregalos en Firebase Console en la app Android con el mismo `applicationId`.
