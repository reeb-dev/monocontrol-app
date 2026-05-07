# Política de Privacidad - MonoControl

**Última actualización: Mayo 2026**

## Información General

MonoControl ("nosotros", "nuestro" o "la aplicación") es una aplicación móvil desarrollada para ayudar a monotributistas y contadores argentinos a gestionar sus obligaciones fiscales. Esta política de privacidad explica cómo recopilamos, usamos y protegemos su información.

## Información que Recopilamos

### Información de Autenticación
- Dirección de email
- Nombre completo (opcional)
- Método de autenticación (email/contraseña o Google Sign-In), mediante **Firebase Authentication** (Google)

### Información Fiscal
- CUIT (Código Único de Identificación Tributaria)
- Categoría de monotributo
- Información de facturación y límites anuales

### Información de Clientes (Solo para Contadores)
- Nombres y CUIT de clientes
- Direcciones de email de clientes
- Información fiscal de clientes
- Configuraciones de recordatorios  
  Estos datos pueden sincronizarse con **Cloud Firestore** (Google) cuando hay conexión y según la configuración del servicio.

### Información Técnica
- Versión de la aplicación
- Sistema operativo del dispositivo
- Idioma del dispositivo
- Configuraciones de notificaciones

### Configuración remota
- La app puede descargar parámetros o textos de ayuda (por ejemplo tarifas o mensajes) desde internet para mantener la información actualizada.

## Cómo Usamos su Información

### Propósitos Principales
- **Autenticación**: Verificar su identidad y mantener su sesión activa
- **Gestión Fiscal**: Calcular límites, categorías y alertas de monotributo
- **Funcionalidad**: Proporcionar todas las características de la aplicación
- **Recordatorios**: Enviar notificaciones sobre vencimientos (con su consentimiento)

### Para Contadores
- Gestionar información de múltiples clientes
- Enviar recordatorios de pago por email o WhatsApp
- Generar reportes y exportaciones

## Compartir de Información

**No vendemos su información personal.** Los datos pueden tratarse por:

- **Google Firebase / Google Cloud**: autenticación (Firebase Authentication), base de datos en la nube cuando corresponda (p. ej. Firestore para sincronización de clientes), según la implementación vigente de la app y la política de privacidad de Google.
- **Requisitos Legales**: Si es requerido por ley o autoridad competente.
- **Su Consentimiento**: Cuando usted autoriza explícitamente compartir información (por ejemplo al enviar un correo o WhatsApp desde la app con los datos que usted elija).

## Seguridad de Datos

- **Encriptación**: Todos los datos se transmiten y almacenan encriptados
- **Firebase Security**: Utilizamos las mejores prácticas de seguridad de Google
- **Acceso Limitado**: Solo usted tiene acceso a sus datos
- **Backups Seguros**: Copias de seguridad encriptadas

## Sus Derechos

Usted tiene derecho a:
- **Acceder** a toda su información almacenada
- **Modificar** o actualizar sus datos
- **Eliminar** su cuenta y todos los datos asociados
- **Exportar** sus datos en formato legible
- **Revocar** consentimiento para comunicaciones

## Retención de Datos

- **Cuenta Activa**: Datos retenidos mientras mantenga su cuenta
- **Cuenta Eliminada**: Datos eliminados permanentemente dentro de 30 días
- **Backups**: Pueden retenerse hasta 90 días por razones de seguridad

## Cookies y Tecnologías Similares

La aplicación no utiliza cookies tradicionales, pero Firebase puede utilizar tecnologías similares para mantener sesiones seguras.

## Cambios a esta Política

Podemos actualizar esta política ocasionalmente. Le notificaremos sobre cambios significativos mediante:
- Notificación en la aplicación
- Email registrado
- Actualización en la Play Store

## Contacto

Para preguntas sobre esta política o sus datos:
- **Email**: jesusreeb@hotmail.com
- **Desarrollador**: Manuel Reeb

## Cumplimiento Legal

Esta aplicación cumple con:
- **Ley de Protección de Datos Personales** (Argentina)
- **RGPD** (para usuarios europeos)
- **Políticas de Google Play**
- **Términos de Servicio de Firebase**

## Publicación (Google Play)

Para cumplir con el requisito de URL de política de privacidad en Play Console, esta política puede publicarse en una página accesible públicamente; en el repositorio del proyecto, la versión HTML para **GitHub Pages** está en `docs/index.html` (ver `docs/POLITICA_PLAY_CONSOLE.md`).

---

*Al usar MonoControl, usted acepta los términos de esta política de privacidad.*
