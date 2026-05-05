# ✅ Sistema de Notificaciones por Email - IMPLEMENTADO

## 📧 Qué se agregó

### 1. **Campo Email en Clientes**
- `ClienteEntity.email` (nuevo campo en BD, version 7)
- Input de email en el diálogo "Nuevo Cliente"
- Validación y formato visible en la tarjeta del cliente

### 2. **UseCase de Recordatorio**
`EnviarRecordatorioEmailUseCase` genera un Intent de email con:
- **Destinatario**: email del cliente
- **Asunto**: "Recordatorio: Vencimiento Monotributo - [Mes] [Año]"
- **Cuerpo** con:
  - Saludo personalizado con nombre del cliente
  - Categoría actual (calculada o forzada)
  - Monto de la cuota mensual según rubro
  - Total facturado en el año
  - Límite anual de su categoría
  - Link a AFIP Monotributo: https://monotributo.afip.gob.ar/
  - Firma del contador

### 3. **Botón de Enviar en ClienteCard**
- Icono 📧 en amarillo (visible solo si el cliente tiene email)
- Al hacer click:
  - Calcula la categoría actual del cliente
  - Genera el email con toda la info
  - Abre la app de email predeterminada (Gmail, Outlook, etc.)
  - Muestra confirmación: "✓ Email abierto"

### 4. **Integración con MovimientoRepository**
- `ClientesViewModel` recibe `MovimientoRepository`
- Método `enviarRecordatorio()` que:
  - Obtiene total facturado en el año
  - Aplica override de categoría si existe
  - Calcula categoría automática si no
  - Genera Intent con todos los datos

---

## 🎯 Cómo usarlo

### Para Contadores:

1. **Agregar cliente con email**:
   - Ir a Clientes → botón "+"
   - Completar: Nombre, CUIT, **Email**, Categoría, Rubro
   - Guardar

2. **Enviar recordatorio**:
   - En la lista de clientes, buscar al cliente
   - Click en el botón 📧 (amarillo)
   - Se abre Gmail/Outlook con el email pre-armado
   - Revisar y enviar

### Ejemplo de Email Generado:

```
Hola Juan Pérez,

Te recordamos que el vencimiento del Monotributo es el día 20 de abril.

📊 Resumen de tu situación:
• Categoría actual: E
• Cuota mensual: $89.714,31
• Total facturado en el año: $18.500.000,00
• Límite anual de tu categoría: $26.977.793,60

💳 Podés pagar desde:
https://monotributo.afip.gob.ar/

Si tenés alguna duda, no dudes en consultarme.

Saludos,
Tu Contador
```

---

## 🔧 Ventajas de esta implementación

✅ **No requiere backend** - usa la app de email nativa
✅ **No requiere API keys** - no hay costo ni configuración externa
✅ **Privacidad total** - el email nunca sale de la app hasta que el usuario lo envía
✅ **Personalizable** - el contador puede editar el mensaje antes de enviar
✅ **Multiplataforma** - funciona con cualquier app de email (Gmail, Outlook, Yahoo, etc.)
✅ **Offline** - se puede redactar sin internet, enviar cuando haya conexión

---

## 📝 Archivos modificados/creados

1. `ClienteEntity.kt` → +campo email
2. `AppDatabase.kt` → v7
3. `EnviarRecordatorioEmailUseCase.kt` → NUEVO
4. `ClientesViewModel.kt` → +enviarRecordatorio()
5. `ClientesScreen.kt` → +campo email + botón 📧
6. `AppNavGraph.kt` → pasa movimientoRepo a ClientesViewModel

---

## ⚠️ Nota

El email **NO se envía automáticamente**. El contador puede:
- Revisar el contenido antes de enviar
- Editar el mensaje si quiere personalizar algo
- Agregar archivos adjuntos
- Copiar a más destinatarios
- Cancelar el envío

Esto da **total control** al contador y cumple con privacidad (GDPR/normativas locales).

---

**Estado**: ✅ IMPLEMENTADO Y LISTO PARA USAR

