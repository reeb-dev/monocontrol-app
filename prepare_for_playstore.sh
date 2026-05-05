#!/bin/bash

# Script para preparar la app para publicación en Play Store
# Uso: ./prepare_for_playstore.sh

echo "🚀 Preparando MonoControl para Play Store"
echo "========================================"

# Verificar que estamos en el directorio correcto
if [ ! -f "build.gradle.kts" ]; then
    echo "❌ Error: Ejecutar desde el directorio raíz del proyecto"
    exit 1
fi

# 1. Generar keystore si no existe
if [ ! -f "app/keystore.jks" ]; then
    echo "🔐 Generando keystore..."
    echo "Te pedirá información. Usa datos reales para la publicación."

    keytool -genkey -v -keystore app/keystore.jks \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -alias key0

    if [ $? -ne 0 ]; then
        echo "❌ Error generando keystore"
        exit 1
    fi

    echo "✅ Keystore generado: app/keystore.jks"
else
    echo "✅ Keystore ya existe"
fi

# 2. Pedir contraseñas
echo ""
echo "🔑 Configura las variables de entorno:"
echo "export STORE_PASSWORD='tu_password_keystore'"
echo "export KEY_ALIAS='key0'"
echo "export KEY_PASSWORD='tu_password_clave'"
echo ""

read -p "¿Ya configuraste las variables? (y/n): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Configura las variables y vuelve a ejecutar el script"
    exit 1
fi

# 3. Build de prueba
echo "🔨 Ejecutando build de prueba..."
./gradlew clean :app:assembleRelease

if [ $? -ne 0 ]; then
    echo "❌ Error en el build. Revisa los logs arriba."
    exit 1
fi

echo "✅ Build exitoso"

# 4. Generar AAB
echo "📦 Generando Android App Bundle (AAB)..."
./gradlew :app:bundleRelease

if [ $? -ne 0 ]; then
    echo "❌ Error generando AAB"
    exit 1
fi

echo "✅ AAB generado en: app/build/outputs/bundle/release/app-release.aab"

# 5. Verificar archivos
echo ""
echo "📋 Checklist final:"
echo "- ✅ Keystore: $([ -f app/keystore.jks ] && echo 'PRESENTE' || echo 'FALTA')"
echo "- ✅ AAB: $([ -f app/build/outputs/bundle/release/app-release.aab ] && echo 'GENERADO' || echo 'FALTA')"
echo "- ✅ Privacy Policy: $([ -f PRIVACY_POLICY.md ] && echo 'LISTA' || echo 'FALTA')"
echo "- ✅ Guía: $([ -f GUIA_PUBLICACION_PLAYSTORE.md ] && echo 'LISTA' || echo 'FALTA')"

echo ""
echo "🎉 ¡Listo para Play Store!"
echo ""
echo "Próximos pasos:"
echo "1. Crear cuenta en https://play.google.com/console/ ($25)"
echo "2. Subir app-release.aab"
echo "3. Completar store listing"
echo "4. Publicar y esperar revisión (1-7 días)"
echo ""
echo "📖 Lee GUIA_PUBLICACION_PLAYSTORE.md para detalles completos"
