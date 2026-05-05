# 📧 Configuración Avanzada de Envío - IMPLEMENTADO

## 🎯 Lo que se implementó

Agregamos una **pantalla completa de configuración de envío** por cliente, accesible desde el perfil del cliente con el botón **"Configurar mensaje, programación y WhatsApp"**.

### Funcionalidades nuevas

#### 1. ✏️ **Email totalmente configurable**
- **Asunto editable** (con default si lo dejás en blanco)
- **Cuerpo del mensaje editable** (también con default que incluye datos del cliente, cuota, vencimiento, etc.)
- **Plantillas inteligentes**: si no escribís nada se usa el mensaje automático.

#### 2. 📝 **Mensajes tipo borrador**
- Botón **"Guardar borrador"** para guardar el mensaje sin enviarlo.
- Aviso visual amarillo: *"Tenés un borrador sin enviar"*.
- Botón **"Descartar"** para limpiar el borrador.
- Al volver a abrir la pantalla, el borrador se carga automáticamente.

#### 3. 📎 **Adjunto extra (PDF o foto)**
- Botón **"Elegir adjunto"** que abre el selector de archivos del sistema.
- Acepta **PDF** e **imágenes** (`application/pdf` + `image/*`).
- Se ve claramente el archivo seleccionado con **chip verde + nombre del archivo + ✕ para quitar**.
- Se persisten los permisos de lectura del Uri (no se pierde entre sesiones).
- El email final manda **factura PDF generada + adjunto extra**.

#### 4. 📅 **Envío automático con fecha y hora**
- Toggle **"Activar recordatorio mensual"**.
- Selector de **día del mes (1-28)**.
- Selector de **hora con TimePicker** estándar de Material 3.
- Sugerencia visual: *"día 18 a las 09:00 (2 días antes del vencimiento)"*.
- **WorkManager** revisa cada hora y dispara una **notificación** cuando coincide día/hora.
- Al tocar la notificación se abre la pantalla de envío con todo pre-cargado.

> ⚠️ **Nota técnica importante**: Android no permite enviar emails 100% sin intervención del usuario sin un servidor SMTP propio (cumplimiento de Play Store + GDPR). La app dispara una **notificación en la fecha/hora elegida** con todo listo, y el contador toca para enviar. Es la forma estándar y segura de hacerlo.

#### 5. 💬 **WhatsApp integrado**
- Toggle **"Enviar también por WhatsApp"**.
- Campo para el **número con código de país** (sólo dígitos, validado).
- Botón verde dedicado **"WhatsApp"** que abre WhatsApp con:
  - El **mensaje pre-cargado** (mismo que el email).
  - El **adjunto pre-cargado** (foto o PDF).
- Funciona con WhatsApp normal y WhatsApp Business.
- Si WhatsApp no está instalado, abre `wa.me/...` en el navegador.

---

## 📁 Archivos creados / modificados

### Nuevos
| Archivo | Qué hace |
|---|---|
| `ui/clientes/ConfigurarEnvioScreen.kt` | Pantalla completa con todas las opciones |
| `ui/clientes/ConfigurarEnvioViewModel.kt` | Lógica + persistencia + borradores |
| `domain/usecase/EnviarRecordatorioWhatsAppUseCase.kt` | Genera Intent de WhatsApp |
| `data/notifications/RecordatorioNotificacionHelper.kt` | Notificación al contador |
| `data/work/RecordatorioEnvioWorker.kt` | Worker periódico de WorkManager |
| `data/work/RecordatorioScheduler.kt` | Programa el worker cada 1h |

### Modificados
| Archivo | Cambio |
|---|---|
| `data/local/entity/ClienteEntity.kt` | +10 campos: asunto, mensaje, borrador, adjunto, programación, WhatsApp |
| `data/local/AppDatabase.kt` | DB v7 → **v8** (destructive migration ya estaba habilitada) |
| `domain/usecase/EnviarRecordatorioEmailUseCase.kt` | Acepta `asuntoCustom`, `mensajeCustom`, lista de `adjuntos` |
| `ui/clientes/ClientesViewModel.kt` | +`enviarRecordatorioWhatsApp()` y email con custom config |
| `ui/clientes/ClienteDetalleScreen.kt` | +botón "Configurar mensaje, programación y WhatsApp" |
| `navigation/AppNavGraph.kt` | +ruta `cliente_envio/{id}` |
| `MainActivity.kt` | +`RecordatorioScheduler.schedule(this)` en `onCreate` |
| `AndroidManifest.xml` | +permisos `<queries>` para WhatsApp en Android 11+ |
| `app/build.gradle.kts` + `libs.versions.toml` | +WorkManager 2.9.1 |

---

## 🚀 Cómo usarlo (paso a paso)

1. **Iniciá sesión como contador** (rol Contador).
2. Ir a **Clientes → Tocar un cliente**.
3. Asegurate de tener cargado el **email del cliente** (sino, editar y agregarlo).
4. Tocar el botón **"Configurar mensaje, programación y WhatsApp"**.
5. En la nueva pantalla:
   - **Asunto**: escribilo o dejalo en blanco para el default.
   - **Mensaje**: escribilo y tocá *"Guardar borrador"* si querés seguir después.
   - **Adjunto**: tocá *"Elegir adjunto"* y seleccioná el PDF o foto de la factura.
   - **Envío automático**: activá el switch, elegí día y hora.
   - **WhatsApp**: activá el switch, ingresá el número con código de país (`5491122334455` para Argentina).
6. Tocá **"Guardar configuración"**.
7. Para enviar **ya mismo**:
   - Botón amarillo **"Enviar Email"** → abre Gmail/Outlook con todo cargado.
   - Botón verde **"WhatsApp"** → abre WhatsApp con todo cargado.
8. El día configurado, te llega una **notificación 🔔** "Recordatorio para [Cliente]". La tocás y se abre la pantalla con todo listo para mandar.

---

## 🔧 Detalles técnicos

### Por qué notificación y no envío directo
Android (y Play Store) **no permiten** enviar emails sin intervención del usuario salvo que:
- Tengas tu propio servidor SMTP (requiere infraestructura, costos, configuración del contador).
- Uses una API tipo SendGrid/Mailgun (idem).

La solución implementada es la que usan apps similares: programar una **notificación recordatoria** que abre el composer pre-cargado. El contador da OK con un tap. Es **legal, seguro, gratis, sin servidor**.

### WorkManager
- Worker periódico de **1 hora** (mínimo permitido por Android).
- Política `KEEP`: si ya está programado no se duplica.
- Se ejecuta incluso si la app está cerrada.
- Tolera reinicios del dispositivo.
- Margen de 1 hora desde la hora configurada para disparar (ej: configurás 09:00 → puede dispararse entre 09:00 y 10:00).

### WhatsApp
- Usa `whatsapp://send?phone=...&text=...` (deep-link nativo).
- Si hay adjunto, usa `Intent.ACTION_SEND` con `setPackage("com.whatsapp")` para abrir el selector de chat con el archivo.
- Cae a `https://wa.me/...` si WhatsApp no está instalado.

### Manifest queries
Agregadas las `<queries>` necesarias para Android 11+:
- `com.whatsapp` y `com.whatsapp.w4b` (Business).
- Intents `mailto:` y `ACTION_SEND` para detectar apps de email.

---

## ✅ Estado del build

**Todo compila correctamente.** Los warnings que muestra el IDE son cosméticos:
- `"never used"` → falsos positivos (las funciones SÍ se usan vía navegación).
- `"Locale deprecated"` → es de Java 19+, no afecta funcionalidad.
- `"Assigned value never read"` → falso positivo en lambdas que setean estado de Compose.

---

**Implementado:** 20 de Abril, 2026

