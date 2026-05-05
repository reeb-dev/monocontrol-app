# 🚀 Guía de Publicación - Google Play Console

## 📋 Checklist Completo para Publicar

### ✅ Lo que YA tienes listo:
- ✅ App completamente funcional
- ✅ Firebase Authentication configurado
- ✅ Build exitoso (APK generado)
- ✅ Arquitectura MVVM + Clean Architecture
- ✅ UI moderna con Material 3
- ✅ Base de datos Room
- ✅ Manejo de permisos

### ⚠️ Lo que FALTA para publicar:

## 1. 🔐 Firma Digital (Keystore)
```bash
# Generar keystore (ejecutar en terminal)
keytool -genkey -v -keystore app/keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias key0

# Te pedirá:
# - Contraseña del keystore
# - Nombre, organización, etc.
# - Contraseña de la clave (puede ser la misma)
```

## 2. 🔧 Variables de Entorno
Crear archivo `.env` o usar variables de sistema:
```bash
export STORE_PASSWORD="tu_password_keystore"
export KEY_ALIAS="key0"
export KEY_PASSWORD="tu_password_clave"
```

## 3. 📱 Generar AAB (Android App Bundle)
```bash
# Build signed AAB
./gradlew :app:bundleRelease

# El archivo se genera en:
# app/build/outputs/bundle/release/app-release.aab
```

## 4. 🏪 Crear Cuenta Google Play Console
- Ir a: https://play.google.com/console/
- Crear cuenta de desarrollador ($25 USD one-time)
- Verificar identidad con documento

## 5. 📝 Store Listing
### Información requerida:
- **Título**: "MonoControl - Monotributo Argentino"
- **Descripción corta**: "Controla tu monotributo fácilmente"
- **Descripción completa**: [Usar el texto del README.md]
- **Categoría**: Finance
- **Screenshots**: 2-8 imágenes (1080x1920px)
- **Ícono**: 512x512px (ya tienes ic_launcher)
- **Feature Graphic**: 1024x500px
- **Package Name**: `com.manuelreeb.monocontrol` ✅

### Capturas de pantalla necesarias:
1. Pantalla de login
2. Home con datos
3. Pantalla de categorías
4. Simulación
5. Gestión de clientes (modo contador)

## 6. 🔒 Privacy Policy
Crear política de privacidad (hosting gratuito en GitHub Pages):
```markdown
# Política de Privacidad - MonoControl

## Información que recopilamos:
- Email y nombre (Firebase Auth)
- Datos fiscales (CUIT, categoría)
- Información de clientes (solo para contadores)

## Uso de datos:
- Autenticación de usuarios
- Gestión de perfiles fiscales
- Envío de recordatorios (con consentimiento)

## Compartir datos:
- No vendemos datos
- Datos encriptados en Firebase
- Solo el usuario controla sus datos

## Contacto: [tu_email]
```

## 7. 📊 Content Rating
Completar cuestionario de clasificación de contenido:
- **Categoría**: Finance
- **Público**: Everyone
- **Contenido**: No violence, no gambling

## 8. 💰 Precios y Distribución
- **Precio**: Gratis
- **Países**: Argentina (o global)
- **Dispositivos**: Teléfonos y tablets

## 9. 📤 Subir y Publicar
1. Subir AAB en Play Console
2. Completar store listing
3. Enviar a revisión
4. Esperar aprobación (1-7 días)

---

## 🛠️ Comandos Útiles

```bash
# Verificar build
./gradlew :app:assembleRelease

# Generar AAB
./gradlew :app:bundleRelease

# Limpiar y rebuild
./gradlew clean :app:bundleRelease

# Ver dependencias
./gradlew :app:dependencies --configuration releaseRuntimeClasspath
```

## 📞 Soporte
Si hay problemas:
1. Verificar keystore existe: `ls -la app/keystore.jks`
2. Verificar variables: `echo $STORE_PASSWORD`
3. Revisar logs: `./gradlew :app:bundleRelease --info`

---

**⏰ Tiempo estimado**: 2-3 horas (más 1-7 días de revisión)

**💰 Costo**: $25 (cuenta Play Console) + opcional hosting privacy policy

¡Tu app está lista para brillar en Play Store! 🌟
