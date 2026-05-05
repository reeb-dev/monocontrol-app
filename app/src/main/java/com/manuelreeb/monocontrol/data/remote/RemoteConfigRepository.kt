package com.manuelreeb.monocontrol.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Repositorio que mantiene en memoria el `RemoteConfig` activo.
 *
 * Estrategia de carga (en este orden):
 *  1. Caché local (SharedPreferences) -> instantáneo.
 *  2. Si nunca cargó nada -> assets/app_config.json (fallback siempre disponible).
 *  3. En background, intenta `refrescar()` desde la URL remota
 *     (configurable). Si falla por red o por JSON inválido, deja
 *     activa la versión cacheada/asset.
 *
 * Recomendación de hosting del JSON remoto:
 *  - Lo más simple: subir el archivo `app_config.json` a un repositorio
 *    público de GitHub y usar la URL "raw" como [REMOTE_URL]. Ej:
 *    https://raw.githubusercontent.com/<usuario>/<repo>/main/app_config.json
 *  - Alternativas: Firebase Remote Config (más complejo) o un endpoint propio.
 *
 * El JSON remoto debe respetar el mismo esquema que `assets/app_config.json`.
 * Cuando subas un cambio, incrementá `"version"` para que la app sepa
 * que hay novedad y la pueda mostrar al usuario.
 */
class RemoteConfigRepository(
    private val context: Context,
    private val remoteUrl: String = REMOTE_URL
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(cargarInicial().also { TarifasService.actualizar(it) })
    val config: StateFlow<RemoteConfig> = _config.asStateFlow()

    private val _ultimaActualizacion = MutableStateFlow(prefs.getLong(KEY_TIMESTAMP, 0L))
    val ultimaActualizacion: StateFlow<Long> = _ultimaActualizacion.asStateFlow()

    /**
     * Carga el primer estado disponible: cache local o, en su defecto, assets.
     */
    private fun cargarInicial(): RemoteConfig {
        val cached = prefs.getString(KEY_JSON, null)
        if (cached != null) {
            runCatching { return parsear(cached) }
                .onFailure { Log.w(TAG, "Caché inválido, vuelvo a assets", it) }
        }
        return cargarDesdeAssets()
    }

    private fun cargarDesdeAssets(): RemoteConfig {
        val raw = context.assets.open(ASSET_FILE).bufferedReader().use { it.readText() }
        return parsear(raw)
    }

    /**
     * Intenta descargar la última versión del JSON remoto.
     * Devuelve `true` si bajó una versión más nueva (mayor `version`).
     * Nunca lanza excepción: ante cualquier error, mantiene la versión actual.
     */
    suspend fun refrescar(): RefreshResult = withContext(Dispatchers.IO) {
        runCatching {
            val raw = descargar(remoteUrl)
            val nuevo = parsear(raw)
            val actual = _config.value
            if (nuevo.version > actual.version || raw != prefs.getString(KEY_JSON, null)) {
                prefs.edit()
                    .putString(KEY_JSON, raw)
                    .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
                    .putInt(KEY_VERSION, nuevo.version)
                    .apply()
                _config.value = nuevo
                TarifasService.actualizar(nuevo)
                _ultimaActualizacion.value = System.currentTimeMillis()
                if (nuevo.version > actual.version) RefreshResult.Updated(nuevo.version)
                else RefreshResult.NoChanges
            } else {
                RefreshResult.NoChanges
            }
        }.getOrElse {
            Log.w(TAG, "No se pudo refrescar config remota: ${it.message}")
            RefreshResult.Error(it.message ?: "Error desconocido")
        }
    }

    /** Restaura la versión incluida en assets (descarta caché). */
    fun restaurarPorDefecto() {
        prefs.edit().clear().apply()
        val nuevo = cargarDesdeAssets()
        _config.value = nuevo
        TarifasService.actualizar(nuevo)
        _ultimaActualizacion.value = 0L
    }

    private fun descargar(url: String): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 5_000
            readTimeout = 5_000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
        }
        try {
            if (conn.responseCode !in 200..299) {
                error("HTTP ${conn.responseCode}")
            }
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    private fun parsear(raw: String): RemoteConfig {
        val root = JSONObject(raw)
        val tarifasJson = root.getJSONArray("tarifas")
        val tarifas = (0 until tarifasJson.length()).map { i ->
            val t = tarifasJson.getJSONObject(i)
            RemoteConfig.TarifaCategoria(
                letra = t.getString("letra"),
                limiteAnual = t.getDouble("limiteAnual"),
                cuotaServicios = t.getDouble("cuotaServicios"),
                cuotaVentaMuebles = t.getDouble("cuotaVentaMuebles")
            )
        }
        require(tarifas.isNotEmpty()) { "Lista de tarifas vacía" }

        val donJson = root.getJSONObject("donaciones")
        return RemoteConfig(
            version = root.optInt("version", 1),
            actualizadoEl = root.optString("actualizadoEl", ""),
            fuente = root.optString("fuente", ""),
            tarifas = tarifas,
            donaciones = RemoteConfig.Donaciones(
                mensaje = donJson.optString("mensaje", ""),
                aliasMercadoPago = donJson.optString("aliasMercadoPago", ""),
                cbu = donJson.optString("cbu", ""),
                cafecitoUrl = donJson.optString("cafecitoUrl", "")
            )
        )
    }

    sealed class RefreshResult {
        data class Updated(val nuevaVersion: Int) : RefreshResult()
        data object NoChanges : RefreshResult()
        data class Error(val mensaje: String) : RefreshResult()
    }

    companion object {
        private const val TAG = "RemoteConfigRepo"
        private const val PREFS_NAME = "remote_config_prefs"
        private const val KEY_JSON = "config_json"
        private const val KEY_TIMESTAMP = "config_ts"
        private const val KEY_VERSION = "config_version"
        private const val ASSET_FILE = "app_config.json"

        /**
         * 🌐 URL del JSON remoto. Cambiala por la tuya.
         *
         * Para empezar gratis y sin backend, recomendamos GitHub:
         *   1. Subí `app_config.json` a un repo (puede ser el mismo de la app).
         *   2. Copiá el botón "Raw" -> URL final (raw.githubusercontent.com).
         *   3. Pegala acá.
         *
         * Mientras esta URL apunte a algo no accesible, la app usa el JSON
         * de `assets/` y todo sigue funcionando offline.
         */
        const val REMOTE_URL =
            "https://raw.githubusercontent.com/manuelreeb/limite-monotributo-config/main/app_config.json"
    }
}

