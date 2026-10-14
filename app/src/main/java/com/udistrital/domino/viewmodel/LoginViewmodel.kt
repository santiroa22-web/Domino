package com.udistrital.domino.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udistrital.domino.data.AuthFirebaseRepository
import com.udistrital.domino.data.AuthRepository
import com.udistrital.domino.data.GoogleSignInHelper
import com.udistrital.domino.data.ResultadoAuth
import com.udistrital.domino.data.Usuario
import com.udistrital.domino.model.EstadoLoginUI
import kotlinx.coroutines.launch

/**
 * ViewModel del Login. Valida el formulario y delega la autenticación en el
 * [AuthRepository] (Firebase Auth). La pantalla solo pinta [uiState].
 */
class LoginViewModel(
    private val auth: AuthRepository = AuthFirebaseRepository()
) : ViewModel() {

    var uiState by mutableStateOf(EstadoLoginUI())
        private set

    fun onCorreoChange(nuevoCorreo: String) {
        uiState = uiState.copy(correo = nuevoCorreo, mensajeError = null)
    }

    fun onContrasenaChange(nuevaContrasena: String) {
        uiState = uiState.copy(contrasena = nuevaContrasena, mensajeError = null)
    }

    fun iniciarSesion(onExito: (Usuario) -> Unit) {
        if (uiState.correo.isBlank() || uiState.contrasena.isBlank()) {
            uiState = uiState.copy(mensajeError = "Ingresa correo y contraseña")
            return
        }
        if (uiState.cargando) return
        uiState = uiState.copy(cargando = true, mensajeError = null)
        viewModelScope.launch {
            manejar(auth.iniciarSesion(uiState.correo, uiState.contrasena), onExito)
        }
    }

    /** Botón "Continuar con Google": pide la cuenta y abre sesión en Firebase. */
    fun iniciarSesionConGoogle(activityContext: Context, onExito: (Usuario) -> Unit) {
        if (uiState.cargando) return
        uiState = uiState.copy(cargando = true, mensajeError = null)
        viewModelScope.launch {
            when (val g = GoogleSignInHelper.obtenerIdToken(activityContext)) {
                is GoogleSignInHelper.Resultado.Token -> manejar(auth.iniciarSesionConGoogle(g.idToken), onExito)
                is GoogleSignInHelper.Resultado.Error -> uiState = uiState.copy(cargando = false, mensajeError = g.mensaje)
                GoogleSignInHelper.Resultado.Cancelado -> uiState = uiState.copy(cargando = false)
            }
        }
    }

    /** Muestra el error o navega, y siempre apaga el indicador de carga. */
    private fun manejar(resultado: ResultadoAuth, onExito: (Usuario) -> Unit) {
        when (resultado) {
            is ResultadoAuth.Exito -> {
                uiState = EstadoLoginUI()
                onExito(resultado.usuario)
            }
            is ResultadoAuth.Error -> uiState = uiState.copy(cargando = false, mensajeError = resultado.mensaje)
        }
    }
}
