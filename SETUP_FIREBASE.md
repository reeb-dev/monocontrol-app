# Firebase Authentication - CONFIGURADO ✅

La app ya está conectada con Firebase Authentication usando el proyecto:

**Proyecto Firebase**: `profile-68864`  
**Package Android**: `com.example.lmitemonotributo`  
**Project ID**: `profile-68864`  
**API Key**: `AIzaSyCcX7p91l_jD2qdxDGo-nr5zOO6SHyPShg`

El archivo `app/google-services.json` ya fue creado con la configuración correcta.

## Estado actual

✅ Firebase SDK configurado en `app/build.gradle.kts`  
✅ Archivo `google-services.json` agregado  
✅ Plugin Google Services aplicado automáticamente  
✅ Firebase Authentication listo para usar

## Próximos pasos para activar la autenticación

## 1) Habilitar el método de email/password en la consola

**IMPORTANTE**: Necesitás completar este paso manualmente en la consola de Firebase:

1. Abrí <https://console.firebase.google.com/project/profile-68864/authentication>
2. Si es la primera vez, click en **"Get started"** en Authentication.
3. En la pestaña **Sign-in method**, habilitá **Email/Password**.
4. Guardá los cambios.

**Sin este paso, la app no podrá registrar usuarios.**

## 2) Probar la app

- Compilá la app.
- En el splash, ahora siempre redirige a **Login** si no hay sesión.
- Probá *Crear cuenta* con un email cualquiera (al menos 6 caracteres en la
  contraseña). Vas a verla aparecer en Firebase → Authentication → Users.
- Probá errores típicos: email inválido, contraseña corta, contraseña mal,
  email ya registrado, sin internet → todos muestran un mensaje claro.

## Roles

Al registrarse, el usuario elige uno de dos roles:

- **Personal**: monotributista que controla su propia facturación.
  La app oculta la sección *Clientes* (no la necesita).
- **Contador**: profesional que administra varios clientes.
  Ve la sección *Clientes guardados* completa, con búsqueda y simulación.

El rol queda guardado en la tabla local `perfil` y se puede cambiar más
adelante editando el código (`PerfilEntity.rol`). Si querés exponerlo en la
UI para que el usuario lo cambie, avisame y lo agrego como un toggle en
*Perfil*.

## Seguridad básica recomendada

- En Firebase Authentication → Settings → **User actions**, habilitá
  *Verificación por email* si querés exigirlo.
- En **Authentication → Settings → Authorized domains**, dejá solo los que uses
  (por ahora *localhost* alcanza).
- Si más adelante guardás datos en Firestore, definí *Security Rules* para que
  cada usuario solo vea sus propios documentos
  (`request.auth.uid == resource.data.uid`).

