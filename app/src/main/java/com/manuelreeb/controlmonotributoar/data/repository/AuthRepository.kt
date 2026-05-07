package com.reeb.controlmonotributoar.data.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * Wrapper sobre Firebase Authentication.
 *
 * Toma `FirebaseAuth.getInstance()` perezosamente para que la app pueda
 * compilar y arrancar incluso si todavía no se configuró google-services.json.
 * Si Firebase no está inicializado, [auth] tira [IllegalStateException] y
 * [signIn]/[signUp] devuelven un Result.failure con un mensaje claro.
 */
class AuthRepository {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val usuarioActual: FirebaseUser?
        get() = runCatching { auth.currentUser }.getOrNull()

    val estaLogueado: Boolean
        get() = usuarioActual != null

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> = runCatching {
        validar(email, password, esRegistro = false)
        val res = auth.signInWithEmailAndPassword(email.trim(), password).await()
        res.user ?: error("Login fallido: respuesta vacía")
    }.recoverCatching { mapError(it) }

    suspend fun signUp(email: String, password: String): Result<FirebaseUser> = runCatching {
        validar(email, password, esRegistro = true)
        val res = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        res.user ?: error("Registro fallido: respuesta vacía")
    }.recoverCatching { mapError(it) }

    /**
     * Inicia sesión con Google usando el token del GoogleSignInAccount.
     * Este método se llama después de que el usuario selecciona su cuenta de Google.
     */
    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<FirebaseUser> = runCatching {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        val res = auth.signInWithCredential(credential).await()
        res.user ?: error("Login con Google fallido: respuesta vacía")
    }.recoverCatching { mapError(it) }

    fun signOut() {
        runCatching { auth.signOut() }
    }

    suspend fun enviarMailRecupero(email: String): Result<Unit> = runCatching {
        if (email.isBlank()) error("Ingresá tu email primero")
        auth.sendPasswordResetEmail(email.trim()).await()
        Unit
    }.recoverCatching { mapError(it) }

    private fun validar(email: String, password: String, esRegistro: Boolean) {
        when {
            email.isBlank() -> error("Ingresá tu email")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() ->
                error("El email no tiene un formato válido")
            password.isBlank() -> error("Ingresá tu contraseña")
            esRegistro && password.length < 6 ->
                error("La contraseña debe tener al menos 6 caracteres")
        }
    }

    /** Convierte excepciones de Firebase en mensajes claros para el usuario. */
    private fun mapError(t: Throwable): Nothing {
        val msg = when (t) {
            is FirebaseAuthInvalidUserException ->
                "No existe una cuenta con ese email. ¿Querés registrarte?"
            is FirebaseAuthInvalidCredentialsException ->
                "Email o contraseña incorrectos."
            is FirebaseAuthUserCollisionException ->
                "Ya existe una cuenta con ese email. Iniciá sesión."
            is FirebaseAuthWeakPasswordException ->
                "Contraseña muy débil. Usá al menos 6 caracteres con letras y números."
            is FirebaseNetworkException ->
                "Sin conexión a internet. Probá de nuevo."
            is IllegalStateException ->
                t.message ?: "Firebase no está configurado todavía."
            is FirebaseException ->
                t.localizedMessage ?: "Error de Firebase. Intentá de nuevo."
            else ->
                t.localizedMessage ?: "Ocurrió un error inesperado."
        }
        throw RuntimeException(msg, t)
    }
}

