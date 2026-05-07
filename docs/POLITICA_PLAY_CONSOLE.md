# Publicar la política de privacidad (Play Console)

Google Play exige una **URL pública** que muestre la política de privacidad. En este proyecto la versión lista para web está en **`docs/index.html`**.

## Opción recomendada: GitHub Pages (gratis)

Si el código está en GitHub (por ejemplo `reeb-dev/monocontrol-privacy`):

1. En el repositorio: **Settings → Pages** (Configuración → Páginas).
2. **Source / Fuente**: *Deploy from a branch* / Publicar desde una rama.
3. **Branch / Rama**: `main` (o la rama principal).
4. **Folder / Carpeta**: **`/docs`** (no la raíz del repo).
5. Guardá. En uno o dos minutos la web quedará en una URL del tipo:

   **`https://reeb-dev.github.io/NOMBRE-DEL-REPO/`**

   Con el repo `monocontrol-privacy`, la URL suele ser:

   **`https://reeb-dev.github.io/monocontrol-privacy/`**

6. Abrí esa URL en el navegador y comprobá que se vea la política.

### Play Console

1. **Play Console** → tu app **MonoControl** (`com.reeb.controlmonotributoar`).
2. **Política y programas** → **Política de la app** (o el asistente de cumplimiento), o en la ficha de la app el campo **Política de privacidad**.
3. Pegá la URL de GitHub Pages de arriba (debe ser **https**, sin login).

## Opción alternativa: enlace al Markdown en GitHub

A veces se acepta la vista del archivo en GitHub (menos elegible en móvil):

`https://github.com/reeb-dev/monocontrol-privacy/blob/main/PRIVACY_POLICY.md`

Si Play la rechaza o preferís una página “limpia”, usá **GitHub Pages** con `docs/index.html`.

## Coherencia con el cuestionario de datos

En **Contenido de la app** / **Seguridad de los datos** (Data safety), respondé de forma coherente con `PRIVACY_POLICY.md` y con lo que la app hace (cuenta, datos fiscales, Firebase, Firestore, notificaciones, etc.).
