package com.reeb.controlmonotributoar.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Maneja backup y restauración de configuración en Firestore.
 * Se usa como fallback secundario si GitHub no está disponible.
 *
 * Estructura en Firestore:
 * ```
 * /admin/
 *   /config/
 *     app_config_v2 (document con el JSON de app_config.json)
 * ```
 */
class FirestoreConfigBackup(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun guardarConfiguracion(json: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val root = JSONObject(json)
            val version = root.optInt("version", 1)
            val documentName = "app_config_v$version"

            firestore.collection("admin").document("config")
                .set(mapOf(documentName to json))
                .await()
            Log.d(TAG, "Configuración guardada en Firestore: $documentName")
            true
        }.onFailure { e ->
            Log.w(TAG, "Error guardando en Firestore: ${e.message}")
        }.getOrElse { false }
    }

    suspend fun descargarConfiguracion(version: Int): String? = withContext(Dispatchers.IO) {
        runCatching {
            val documentName = "app_config_v$version"
            val doc = firestore.collection("admin").document("config")
                .get()
                .await()

            val json = doc.get(documentName) as? String
            if (json != null) {
                Log.d(TAG, "Configuración encontrada en Firestore: $documentName")
            } else {
                Log.w(TAG, "No se encontró $documentName en Firestore")
            }
            json
        }.onFailure { e ->
            Log.w(TAG, "Error descargando de Firestore: ${e.message}")
        }.getOrNull()
    }

    suspend fun obtenerUltimaVersion(): Int? = withContext(Dispatchers.IO) {
        runCatching {
            val doc = firestore.collection("admin").document("config")
                .get()
                .await()

            // Buscar el documento con versión más alta
            var maxVersion = 0
            doc.data?.keys?.forEach { key ->
                if (key.startsWith("app_config_v")) {
                    val versionStr = key.removePrefix("app_config_v")
                    val version = versionStr.toIntOrNull() ?: 0
                    if (version > maxVersion) {
                        maxVersion = version
                    }
                }
            }

            if (maxVersion > 0) {
                Log.d(TAG, "Última versión en Firestore: $maxVersion")
                maxVersion
            } else {
                Log.w(TAG, "No se encontraron versiones en Firestore")
                null
            }
        }.onFailure { e ->
            Log.w(TAG, "Error obteniendo última versión: ${e.message}")
        }.getOrNull()
    }

    companion object {
        private const val TAG = "FirestoreConfigBackup"
    }
}

