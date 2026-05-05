# 📱 Screenshots Específicas de MonoControl

## 🎯 8 Screenshots Recomendadas (Orden Sugerido)

### Screenshot 1: SPLASH/LOGIN 
**Qué mostrar:**
- Pantalla de bienvenida MonoControl
- Logo centrado
- Botones "Iniciar Sesión" y "Registrarse"
- Colores argentinosceleste + oro

**Caption:**
"Inicia sesión con email o Google"

**Por qué:** Primera impresión, transmite profesionalismo

---

### Screenshot 2: HOME - VISTA GENERAL
**Qué mostrar:**
- Header con saludo personalizado
- Total facturado: $450,000 (ejemplo)
- Categoría actual: "Categoría C" (con badge dorado)
- Barra de progreso: 65% (color amarillo)
- Botón "Ingresos vs Gastos"
- Cards de ingresos ($120k) y gastos ($30k)

**Caption:**
"Tu facturación en tiempo real"

**Datos sugeridos:**
```
Mes: Mayo 2026
Total facturado: $450,000
Categoría: C (Límite: $15.6M)
% del límite: 65%
Ingresos mes: $120,000
Gastos mes: $30,000
```

**Por qué:** Muestra la funcionalidad principal

---

### Screenshot 3: CATEGORÍA - TABLA COMPLETA
**Qué mostrar:**
- Header "Categorías 2026"
- Tabla con todas las categorías A-K
- Columnas: Categoría | Límite Anual | Cuota Mensual
- Tu categoría (C) destacada/resaltada en oro
- Badge "Activa" al lado de tu categoría
- Información fiscal en el header

**Caption:**
"Categorías según AFIP 2026"

**Ejemplo de datos:**
```
A: $7.4M - $7,500
B: $11M - $12,500
C: $15.6M - $18,000 ← TU CATEGORÍA (ACTIVA)
D: $21.6M - $26,000
...
K: $68.7M - $250,000
```

**Por qué:** Demuestra información fiscal actualizada

---

### Screenshot 4: SIMULACIÓN
**Qué mostrar:**
- Campo de entrada: "¿Cuánto más facturas?"
- Valor ingresado: $2,500,000 (ejemplo)
- Resultados:
  - Total nuevo: $452,500,000
  - Nueva categoría: D
  - Nueva cuota: $26,000
- Flecha roja: C → D (cambio de categoría)
- Tabla de referencia pequeña

**Caption:**
"Simula cambios antes de que ocurran"

**Por qué:** Herramienta única y valiosa

---

### Screenshot 5: MOVIMIENTOS (Historial)
**Qué mostrar:**
- Pantalla: Movimientos
- Formulario superior (colapsado o visible)
- Historial con:
  - Ingreso: +$150,000 (verde)
  - Ingreso: +$80,000 (verde)
  - Gasto: -$15,000 (rojo)
  - Ingreso: +$45,000 (verde)
  - Gasto: -$8,500 (rojo)
- Fechas ordenadas descendentes
- Resumen del día: Neto: +$251,500

**Caption:**
"Registra movimientos en 2 clics"

**Por qué:** Muestra funcionalidad de uso diario

---

### Screenshot 6: ALERTAS
**Qué mostrar:**
- Pantalla: Alertas
- Múltiples alertas con:
  1. 🟡 AMARILLA: "Estás al 65% (Cat. C) → pasarías a Cat. D"
     Barra progreso: 65%
  
  2. 🟢 VERDE: "Categoría estable"
     Barra progreso: 42%
  
  3. 🔴 ROJA: "¡Cuidado! Estás al 85% (Cat. C)"
     Barra progreso: 85%

**Caption:**
"Alertas automáticas personalizadas"

**Por qué:** Sistema único de notifications

---

### Screenshot 7: CLIENTES (Para contadores)
**Qué mostrar:**
- Pantalla: Clientes
- Tarjeta de cliente:
  - Nombre: "Carlos López"
  - CUIT: 23-12345678-9
  - Categoría: B (con badge celeste)
  - Total facturado: $285,000
  - % del límite: 42%
  - Botones: Editar, Email, WhatsApp, Más

**Caption:**
"Gestiona múltiples clientes fácilmente"

**Datos sugeridos:**
```
Cliente 1: Carlos López (Cat. B, 42%)
Cliente 2: María García (Cat. C, 68%)
Cliente 3: Juan Rodríguez (Cat. A, 25%)
```

**Por qué:** Demuestra capacidad de contador

---

### Screenshot 8: PERFIL
**Qué mostrar:**
- Header con datos personales
- Avatar/Inicial
- Nombre: "Juan Pérez"
- CUIT: XX-XXXXXXXX-X
- Email: juan@gmail.com
- Rol: Personal / Contador (toggle visible)
- Datos de usuario completados
- Botones: Editar, Cerrar Sesión, Más

**Caption:**
"Tu perfil seguro con Firebase"

**Por qué:** Transmite seguridad y profesionalismo

---

## 🎨 Colores a Usar

```
Celeste (Primario): #75AADB
Oro (Acentos): #FBB81C
Verde (Positivo): #4CAF50
Rojo (Negativo): #F44336
Gris (Neutro): #757575
Fondo: #FFFFFF (Light Mode)
```

---

## 📐 Especificaciones Técnicas

**Resolución:** 1080 x 1920 px
**Formato:** PNG o JPG
**Orientación:** Vertical (Portrait)
**DPI:** ~480 DPI (Pixel 6 estándar)

---

## 🚀 Datos de Prueba Recomendados

### Para Login
- Email: `prueba@gmail.com`
- Contraseña: `Prueba123!`

### Para Home
- Mes: Mayo 2026
- Total: $450,000
- Categoría: C (65% del límite)
- Ingresos: $120,000
- Gastos: $30,000

### Para Categoría
- Mayor énfasis en Categoría C (tu categoría actual)
- Mostrar rango completo A-K
- Datos reales de AFIP 2026

### Para Simulación
- Entrada: +$2,500,000
- Cambio visible: C → D
- Nueva cuota: $26,000

### Para Movimientos
- Últimos 5 movimientos
- Mix de ingresos/gastos
- Fechas variadas (últimos días)

### Para Alertas
- 1 Alerta ROJA (85%)
- 1 Alerta AMARILLA (65%)
- 1 Alerta VERDE (42%)

### Para Clientes
- 3 clientes con categorías diferentes
- Montos variados
- Porcentajes diferentes

### Para Perfil
- Datos completos
- Foto de perfil
- Email visible

---

## ✅ Checklist de Calidad

- [ ] Resolución: 1080x1920
- [ ] Sin notificaciones del sistema (limpiar barra superior)
- [ ] Sin botones de navegación visibles
- [ ] Light Mode (tema claro)
- [ ] Datos realistas y legibles
- [ ] Información visible sin scroll (donde posible)
- [ ] Colores consistentes
- [ ] Sin errores visibles
- [ ] Captions claras y atractivas
- [ ] Orden lógico (1-8)
- [ ] Comprimidas pero de buena calidad

---

## 🎬 Orden de Toma de Pantallas

```
1. Cierra app completamente
2. Abre app (muestra splash)
3. Captura Screenshot 1 (Splash/Login)
4. Inicia sesión
5. Captura Screenshot 2 (Home)
6. Ve a Categoría
7. Captura Screenshot 3 (Categoría)
8. Ve a Simulación
9. Ingresa dato (2,500,000)
10. Captura Screenshot 4 (Simulación)
11. Ve a Movimientos
12. Captura Screenshot 5(Movimientos)
13. Ve a Alertas
14. Captura Screenshot 6 (Alertas)
15. Ve a Clientes (o toggle a Contador)
16. Captura Screenshot 7 (Clientes)
17. Ve a Perfil
18. Captura Screenshot 8 (Perfil)
```

---

## 📧 Nombres de Archivo Recomendados

```
screenshot_01_splash_login.png
screenshot_02_home_dashboard.png
screenshot_03_categorias_afip.png
screenshot_04_simulacion_fiscal.png
screenshot_05_movimientos_historial.png
screenshot_06_alertas_personalizadas.png
screenshot_07_clientes_contador.png
screenshot_08_perfil_seguridad.png
```

---

## 🎯 Estrategia de Screenshots

**Objetivo:** Mostrar la aplicación completa en 8 pantallas

1. **Screenshot 1:** Atrae (bonita, profesional)
2. **Screenshot 2:** Engaña (funcionalidad principal)
3. **Screenshot 3:** Calidad (datos AFIP)
4. **Screenshot 4:** Diferencia (herramienta única)
5. **Screenshot 5:** Usabilidad (fácil de usar)
6. **Screenshot 6:** Valor (alertas automáticas)
7. **Screenshot 7:** Escalabilidad (para contadores)
8. **Screenshot 8:** Confianza (seguridad + firebase)

---

**⏱️ Tiempo total de toma:** 30-45 minutos
**🎨 Tiempo de edición (opcional):** 15-20 minutos
**✅ Resultado final:** 8 capturas profesionales para Play Store

¿Listo para capturar? 📸🚀
