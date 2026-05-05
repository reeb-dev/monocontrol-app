#!/bin/bash

# Script de verificación de configuración de Firebase
# Ejecuta: chmod +x verificar-firebase.sh && ./verificar-firebase.sh

echo "🔥 Verificando configuración de Firebase..."
echo ""

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Contador de checks
TOTAL=0
PASSED=0

# Función para verificar
check() {
    TOTAL=$((TOTAL + 1))
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ $2${NC}"
        PASSED=$((PASSED + 1))
        return 0
    else
        echo -e "${RED}❌ $2${NC}"
        return 1
    fi
}

# 1. Verificar que google-services.json existe
if [ -f "app/google-services.json" ]; then
    check 0 "Archivo google-services.json existe"

    # Verificar que contiene las credenciales correctas
    if grep -q "profile-68864" app/google-services.json; then
        check 0 "Project ID correcto (profile-68864)"
    else
        check 1 "Project ID en google-services.json"
    fi

    if grep -q "com.example.lmitemonotributo" app/google-services.json; then
        check 0 "Package name correcto"
    else
        check 1 "Package name en google-services.json"
    fi
else
    check 1 "Archivo google-services.json no encontrado"
fi

# 2. Verificar que build.gradle.kts tiene Firebase configurado
if grep -q "firebase-bom" app/build.gradle.kts; then
    check 0 "Firebase BOM en dependencies"
else
    check 1 "Firebase BOM no encontrado en build.gradle.kts"
fi

if grep -q "firebase-auth-ktx" app/build.gradle.kts; then
    check 0 "Firebase Auth en dependencies"
else
    check 1 "Firebase Auth no encontrado en build.gradle.kts"
fi

# 3. Verificar que el plugin de Google Services está configurado
if grep -q "google.services" build.gradle.kts; then
    check 0 "Google Services plugin en build.gradle.kts raíz"
else
    check 1 "Google Services plugin no encontrado"
fi

# 4. Verificar que AuthRepository existe
if [ -f "app/src/main/java/com/example/lmitemonotributo/data/repository/AuthRepository.kt" ]; then
    check 0 "AuthRepository implementado"
else
    check 1 "AuthRepository no encontrado"
fi

# 5. Verificar que LoginScreen existe
if [ -f "app/src/main/java/com/example/lmitemonotributo/ui/login/LoginScreen.kt" ]; then
    check 0 "LoginScreen implementado"
else
    check 1 "LoginScreen no encontrado"
fi

# 6. Verificar que RegisterScreen existe
if [ -f "app/src/main/java/com/example/lmitemonotributo/ui/login/RegisterScreen.kt" ]; then
    check 0 "RegisterScreen implementado"
else
    check 1 "RegisterScreen no encontrado"
fi

# Resumen
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if [ $PASSED -eq $TOTAL ]; then
    echo -e "${GREEN}✅ Todos los checks pasaron ($PASSED/$TOTAL)${NC}"
    echo ""
    echo -e "${YELLOW}⚠️  SIGUIENTE PASO:${NC}"
    echo "Habilita Email/Password en Firebase Console:"
    echo "https://console.firebase.google.com/project/profile-68864/authentication"
else
    echo -e "${RED}❌ Algunos checks fallaron ($PASSED/$TOTAL)${NC}"
    echo ""
    echo "Revisa los errores arriba y consulta:"
    echo "- FIREBASE_IMPLEMENTACION.md"
    echo "- SETUP_FIREBASE.md"
fi
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Intentar compilar
echo "🔨 Intentando compilar el proyecto..."
echo ""

if ./gradlew :app:processDebugGoogleServices --quiet; then
    echo -e "${GREEN}✅ Google Services procesado correctamente${NC}"
else
    echo -e "${RED}❌ Error procesando Google Services${NC}"
fi

# Exit con código apropiado
if [ $PASSED -eq $TOTAL ]; then
    exit 0
else
    exit 1
fi

