# 📸 Guía Completa: Capturar Screenshots para Play Store

## 📋 Requisitos de Play Store

- **Dimensiones**: 1080 x 1920 px (orientación vertical)
- **Cantidad**: Mínimo 2, máximo 8 screenshots
- **Formato**: PNG o JPEG
- **Ubicación en Play Store**: "Screenshots" en Store Listing

---

## 🎯 Screenshots Recomendadas para MonoControl

### 1. **Login/Splash** (Pantalla de bienvenida)
- Muestra: Logo + Botones Login/Registro
- Propósito: Primera impresión
- Importante: Transmitir profesionalismo

### 2. **Home/Dashboard** (Pantalla principal)
- Muestra: Total facturado, categoría actual, barra de progreso
- Propósito: Mostrar información fiscal en tiempo real
- Caption: "Controla tu facturación en tiempo real"

### 3. **Categoría** (Tabla de categorías)
- Muestra: Límites, cuotas mensuales, categoría actual destacada
- Propósito: Mostrar información fiscal completa
- Caption: "Categorías actualizadas 2026"

### 4. **Simulación** (Herramienta de simulación)
- Muestra: Campo de entrada, cálculo, recategorización
- Propósito: Demostrar la simulación fiscal
- Caption: "Simula cambios antes de que ocurran"

### 5. **Movimientos** (Historial de ingresos/gastos)
- Muestra: Formulario + Historial con códigos de color
- Propósito: Gestión de movimientos
- Caption: "Registro rápido de movimientos"

### 6. **Alertas** (Sistema de notificaciones)
- Muestra: Alertas con semáforo visual y barra de progreso
- Propósito: Sistema de alertas inteligente
- Caption: "Alertas automáticas antes de recategorizarte"

### 7. **Perfil/Clientes** (Para contadores)
- Muestra: Gestión de clientes o datos personales
- Propósito: Mostrar vista de contador
- Caption: "Gestión de múltiples clientes"

### 8. **Seguridad** (Opcional)
- Muestra: Pantalla de Perfil con datos de seguridad
- Propósito: Transmitir confianza y seguridad
- Caption: "Tus datos seguros con Firebase"

---

## 🚀 Pasos para Capturar Screenshots

### Opción 1: Android Studio Emulator (Recomendado)

#### 1. Crear Emulador
```bash
# Abre Android Studio
# Menu: Tools → Device Manager → Create Device
# Selecciona: Pixel 6 (1080x1920)
# Selecciona: Android 12 o superior
# Clic: Finish
```

#### 2. Instalar APK en Emulador
```bash
# Ejecuta el emulador
# Opción A: Arrastra el APK al emulador
# Opción B: Comando de terminal
adb install app/build/outputs/bundle/release/app-release.aab

# O usa el APK de debug
./gradlew :app:installDebug
```

#### 3. Abrir la App
```bash
# La app aparecerá en el home del emulador
# Tócala para abrir
```

#### 4. Capturar Screenshots
```bash
# En Android Studio: Botón "Screenshot" en Device Manager
# O: Ctrl+Shift+T en Windows/Linux / Cmd+Shift+T en Mac
# O: Usa la combinación: Ctrl+F12 (Emulator menu)
```

#### 5. Guardar Files
```bash
# Las capturas se guardan automáticamente
# Ubicación por defecto: ~/Pictures/
# O configura carpeta en Device Manager
```

---

### Opción 2: Con Device Real

```bash
# Conecta tu Android por USB
# Habilita "Depuración USB" en Configuración → Opciones de Desarrollador

# Ver dispositivos
adb devices

# Instalar app
adb install app/build/outputs/apk/release/app-release.apk

# Capturar screenshot
adb shell screencap -p /sdcard/screenshot.png

# Descargar screenshot
adb pull /sdcard/screenshot.png ~/Pictures/

# Arrancar en bucle para capturar múltiples
adb shell screencap -p /sdcard/screenshot_1.png
```

---

### Opción 3: Con Herramientas Externas

**Android Studio Profiler**
- Tools → Profiler → Screenshots

**ADB Screenshot Helper**
- Instala: `brew install android-screenshot-utility`

**Google Play Console Preview**
- Sube screenshot y Google te muestra preview en diferentes dispositivos

---

## ✏️ Cómo Capturar la Mejor Versión

### Antes de capturar:
1. **Datos de prueba**: Carga movimientos para que se vea realista
   - Home: muestra ingresos/gastos ($100k+)
   - Categoría: debería ser B,C,D (no A)
   - Movimientos: al menos 5 movimientos
   - Alertas: carga una alerta

2. **Configuración visual**:
   - Light Mode (mejor para Play Store)
   - Resolución Pixel 6 (1080x1920)
   - Sin notificaciones superiores

3. **Datos de usuario**:
   - Nombre de prueba: "Juan Pérez"
   - CUIT: "12345678901"
   - Email: "prueba@gmail.com"

---

## 📐 Dimensiones y Compresión

```bash
# Redimensionar si es necesario (ImageMagick)
convert screenshot.png -resize 1080x1920 screenshot_resized.png

# Optimizar para web (reduce tamaño)
convert screenshot.png -quality 90 screenshot_optimized.jpg

# Con ffmpeg
ffmpeg -i screenshot.png -s 1080x1920 screenshot_final.png
```

---

## 🎨 Mejoras Opcionales (con Herramientas)

### Agregar Captions/Textos
```bash
# Con GIMP (GUI)
# O con ImageMagick:
convert screenshot.png -gravity South -pointsize 40 -fill white \
  -annotate +0+50 "Controla tu facturación en tiempo real" \
  screenshot_with_text.png
```

### Agregar Marco de Dispositivo
```bash
# Descargar marcos de Google:
# https://github.com/google/android-screenshots-pack

# O usar online:
# https://www.mockupworld.co/mockups/android-devices/
```

---

## 📝 Captions Recomendadas

```
Screenshot 1 (Login):
"Inicia sesión con email o Google"

Screenshot 2 (Home):
"Controla tu facturación en tiempo real"

Screenshot 3 (Categoría):
"Categorías 2026 actualizadas según AFIP"

Screenshot 4 (Simulación):
"Simula cambios de categoría antes de que ocurran"

Screenshot 5 (Movimientos):
"Registra ingresos y gastos en 2 clics"

Screenshot 6 (Alertas):
"Alertas automáticas cuando llegas al 70%"

Screenshot 7 (Clientes):
"Gestiona múltiples clientes en una app"

Screenshot 8 (Seguridad):
"Tus datos seguros con Firebase"
```

---

## 🎬 Script Automatizado (Opcional)

Copia este script para automatizar capturas (requiere Android SDK):

```bash
#!/bin/bash
# Archivo: capture_screenshots.sh

EMULATOR_NAME="Pixel_6_API_31"
SCREENSHOTS_DIR=~/Pictures/MonoControl_Screenshots
COUNTER=1

# Crear directorio
mkdir -p $SCREENSHOTS_DIR

# Iniciar emulador
emulator -avd $EMULATOR_NAME &
sleep 30

# Tomar múltiples screenshots con delay
for i in {1..8}; do
    echo "Tomando screenshot $i..."
    adb shell screencap -p /sdcard/screenshot_$i.png
    adb pull /sdcard/screenshot_$i.png $SCREENSHOTS_DIR/screenshot_$i.png
    sleep 3
done

echo "✅ Capturas guardadas en $SCREENSHOTS_DIR"
```

---

## 📧 Checklist Final

- [ ] 8 screenshots en 1080x1920
- [ ] Nombres descriptivos (screenshot_1, screenshot_2, etc.)
- [ ] Captions/descripciones
- [ ] Sin notificaciones del sistema
- [ ] Datos de prueba realistas
- [ ] Light mode (mejor visualización)
- [ ] Guardadas como PNG o JPG
- [ ] Comprimidas para web

---

## 🆘 Troubleshooting

### No aparece la app después de instalar
```bash
adb install -r -s app/build/outputs/apk/release/app-release.apk
```

### Emulador lento
- Aumenta RAM en Device Manager (8GB mínimo)
- Usa Pixel 6 o inferior (menos recursos)

### Screenshots con mala resolución
- Verifica emulador: 1080x1920
- Descarga de: adb shell wm size

### No funciona el login
- Verifica Firebase está configurado
- O usa datos sin login (debug mode)

---

## 💡 Pro Tips

1. **Toma 3 versiones de cada pantalla**: Una versión normal, una con datos máximos, una con datos mínimos

2. **Usa tema claro**: Mejor contraste y visualización en tiendas

3. **Muestra diferentes roles**: Home Personal vs Home Contador

4. **Datos realistas**: Montos que parezcan reales para Argentina ($100k, $500k, etc.)

5. **Ordena cronológicamente**: Screenshot 1 → 8 debe contar una historia

---

**⏰ Tiempo estimado**: 30-45 minutos

**📱 Dispositivo recomendado**: Emulador Pixel 6 (mejor resolución)

¿Necesitas ayuda con algún paso específico? 📸
