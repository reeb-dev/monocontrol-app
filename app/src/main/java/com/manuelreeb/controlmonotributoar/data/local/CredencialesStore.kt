package com.reeb.controlmonotributoar.data.local

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Almacén local para "Recordarme" en el login.
 *
 * - Email: siempre en claro (no es dato sensible).
 * - Password: cifrado AES-256-GCM con Android Keystore cuando está disponible.
 *   Si el Keystore falla (emulador, reset de dispositivo, etc.) usa un fallback
 *   en SharedPreferences para evitar que el campo quede en blanco.
 */
class CredencialesStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    var recordar: Boolean
        get() = prefs.getBoolean(KEY_REMEMBER, false)
        set(v) { prefs.edit().putBoolean(KEY_REMEMBER, v).apply() }

    /** Email siempre en claro — no requiere cifrado. */
    var email: String
        get() = prefs.getString(KEY_EMAIL, "").orEmpty()
        set(v) { prefs.edit().putString(KEY_EMAIL, v.trim()).apply() }

    /** Contraseña: intenta descifrar con Keystore; si falla usa fallback. */
    var password: String
        get() {
            // 1. Intento Keystore
            val cifrado = prefs.getString(KEY_PASSWORD, "").orEmpty()
            if (cifrado.isNotBlank()) {
                val descifrada = runCatching { descifrar(cifrado) }.getOrNull()
                if (!descifrada.isNullOrBlank()) return descifrada
            }
            // 2. Fallback (guardado en claro cuando Keystore no está disponible)
            return prefs.getString(KEY_PASSWORD_FALLBACK, "").orEmpty()
        }
        set(v) {
            val cifrado = runCatching { cifrar(v) }.getOrNull()
            if (!cifrado.isNullOrBlank()) {
                prefs.edit()
                    .putString(KEY_PASSWORD, cifrado)
                    .remove(KEY_PASSWORD_FALLBACK)
                    .apply()
            } else {
                // Keystore no disponible → guardar fallback (menos seguro, pero funcional)
                prefs.edit()
                    .remove(KEY_PASSWORD)
                    .putString(KEY_PASSWORD_FALLBACK, v)
                    .apply()
            }
        }

    /** Guarda email + password marcando "recordar = true". */
    fun guardar(email: String, password: String) {
        this.email = email.trim()
        this.password = password
        prefs.edit().putBoolean(KEY_REMEMBER, true).apply()
    }

    /** Borra todo lo guardado (al hacer logout o destildar "Recordarme"). */
    fun limpiar() {
        prefs.edit()
            .remove(KEY_REMEMBER)
            .remove(KEY_EMAIL)
            .remove(KEY_PASSWORD)
            .remove(KEY_PASSWORD_FALLBACK)
            .apply()
    }

    // ── Cifrado AES-256-GCM con Android Keystore ────────────────────────

    private fun obtenerOCrearClave(): SecretKey {
        val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (ks.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }

        val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        kg.init(spec)
        return kg.generateKey()
    }

    private fun cifrar(plain: String): String {
        if (plain.isEmpty()) return ""
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, obtenerOCrearClave())
        val iv = cipher.iv
        val ct = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(iv, Base64.NO_WRAP) + ":" +
                Base64.encodeToString(ct, Base64.NO_WRAP)
    }

    private fun descifrar(cifrado: String): String {
        val partes = cifrado.split(":")
        if (partes.size != 2) return ""
        val iv = Base64.decode(partes[0], Base64.NO_WRAP)
        val ct = Base64.decode(partes[1], Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, obtenerOCrearClave(), GCMParameterSpec(128, iv))
        return String(cipher.doFinal(ct), Charsets.UTF_8)
    }

    companion object {
        private const val FILE_NAME             = "credenciales_seguras"
        private const val KEY_REMEMBER          = "remember"
        private const val KEY_EMAIL             = "email"
        private const val KEY_PASSWORD          = "password_cipher"
        private const val KEY_PASSWORD_FALLBACK = "password_plain"
        private const val ANDROID_KEYSTORE      = "AndroidKeyStore"
        private const val KEY_ALIAS             = "monocontrol_credenciales_v1"
    }
}
