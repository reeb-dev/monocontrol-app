# Firebase y configuración Android

## Proyecto

El proyecto usa Firebase Authentication (email/contraseña y Google), Remote Config y servicios asociados. El archivo `app/google-services.json` debe coincidir con el **`applicationId`** definido en `app/build.gradle.kts` (actualmente `com.reeb.controlmonotributoar`).

Si cambiás el package/applicationId:

1. En [Firebase Console](https://console.firebase.google.com), agregá una app Android con ese ID o actualizá la existente.
2. Descargá el `google-services.json` nuevo y reemplazá el de `app/`.

## Authentication

En Firebase → **Authentication** → **Sign-in method**:

- Habilitá **Email/Password** si usás login con correo.
- Habilitá **Google** y configurá la pantalla de consentimiento OAuth en Google Cloud si hace falta.

## Huellas SHA (debug y release)

Para Google Sign-In y algunas APIs:

1. **Debug**: obtené SHA con `./gradlew signingReport` o desde Android Studio (**Gradle → android → signingReport**).
2. **Release**: si usás Play App Signing, los SHA oficiales están en Play Console → **Integridad de la app**.

Agregá esos SHA-1 y SHA-256 en Firebase → configuración del proyecto → tu app Android.

## Remote Config

Parámetros remotos (tarifas, textos, URLs de ayuda, etc.) se obtienen en tiempo de ejecución. Valores por defecto locales suelen vivir en `app/src/main/assets/app_config.json`.
