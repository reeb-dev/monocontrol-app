package com.manuelreeb.monocontrol.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manuelreeb.monocontrol.data.local.CredencialesStore
import com.manuelreeb.monocontrol.data.local.entity.PerfilEntity
import com.manuelreeb.monocontrol.data.repository.AuthRepository
import com.manuelreeb.monocontrol.data.repository.PerfilRepository
import com.manuelreeb.monocontrol.domain.model.UserRole
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Error(val mensaje: String) : AuthUiState
    data class Success(val rol: UserRole) : AuthUiState
}

class AuthViewModel(
    private val authRepo: AuthRepository,
    private val perfilRepo: PerfilRepository,
    private val credenciales: CredencialesStore? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun resetEstado() { _uiState.value = AuthUiState.Idle }

    fun yaLogueado(): Boolean = authRepo.estaLogueado

    /** Email guardado por "Recordarme" (vacío si no hay). */
    fun emailRecordado(): String = credenciales?.email.orEmpty()

    /** Contraseña guardada por "Recordarme" (vacía si no hay). */
    fun passwordRecordado(): String = credenciales?.password.orEmpty()

    /** Si el usuario tildó "Recordarme" la última vez. */
    fun recordarme(): Boolean = credenciales?.recordar ?: false

    /**
     * @param recordar Si es true, guarda email + password en almacén encriptado
     *                 para pre-cargarlos en el próximo arranque.
     */
    fun login(email: String, password: String, recordar: Boolean = false) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepo.signIn(email, password)
                .onSuccess { user ->
                    val perfilActual = perfilRepo.observar().first()
                    val rolGuardado = UserRole.fromName(perfilActual?.rol)
                    perfilRepo.guardar(
                        (perfilActual ?: PerfilEntity()).copy(
                            email = user.email.orEmpty(),
                            rol = rolGuardado.name
                        )
                    )
                    // Persistir credenciales sólo si lo pidió.
                    credenciales?.let { store ->
                        if (recordar) store.guardar(email, password) else store.limpiar()
                    }
                    _uiState.value = AuthUiState.Success(rolGuardado)
                }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Error") }
        }
    }

    fun registrar(email: String, password: String, rol: UserRole) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepo.signUp(email, password)
                .onSuccess { user ->
                    val actual = perfilRepo.observar().first() ?: PerfilEntity()
                    perfilRepo.guardar(
                        actual.copy(
                            email = user.email.orEmpty(),
                            rol = rol.name
                        )
                    )
                    _uiState.value = AuthUiState.Success(rol)
                }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Error") }
        }
    }

    /**
     * Login con Google. Se llama después de que el usuario selecciona su cuenta.
     * Debe recibir el GoogleSignInAccount del Intent result.
     */
    fun loginWithGoogle(account: GoogleSignInAccount, defaultRole: UserRole = UserRole.PERSONAL) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepo.signInWithGoogle(account)
                .onSuccess { user ->
                    // Verificar si ya existe un perfil, sino crear uno con rol por defecto
                    val perfilActual = perfilRepo.observar().first()
                    val rol = if (perfilActual != null && perfilActual.rol.isNotEmpty()) {
                        UserRole.fromName(perfilActual.rol)
                    } else {
                        defaultRole
                    }

                    perfilRepo.guardar(
                        (perfilActual ?: PerfilEntity()).copy(
                            email = user.email.orEmpty(),
                            nombre = user.displayName ?: perfilActual?.nombre ?: "",
                            rol = rol.name
                        )
                    )
                    // Login con Google: limpiamos credenciales de email/password
                    // (si las hubiera) porque ahora la sesión va por OAuth.
                    credenciales?.limpiar()
                    _uiState.value = AuthUiState.Success(rol)
                }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Error con Google") }
        }
    }

    fun recuperarPassword(email: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            authRepo.enviarMailRecupero(email)
                .onSuccess { onResult("Te enviamos un email para resetear la contraseña.") }
                .onFailure { onResult(it.message ?: "No se pudo enviar el email") }
        }
    }

    fun cerrarSesion() {
        authRepo.signOut()
        // Al cerrar sesión, también borramos las credenciales guardadas.
        credenciales?.limpiar()
        _uiState.value = AuthUiState.Idle
    }
}


