package com.udistrital.domino.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udistrital.domino.data.AuthFirebaseRepository
import com.udistrital.domino.data.AuthRepository
import com.udistrital.domino.data.ResultadoAuth
import com.udistrital.domino.data.Usuario
import com.udistrital.domino.model.EstadoRegistroUI
import kotlinx.coroutines.launch

/** ViewModel de "Crear cuenta": valida y crea el usuario en Firebase Auth. */
class RegistroViewModel(
    private val auth: AuthRepository = AuthFirebaseRepository()
) : ViewModel() {

    var uiState by mutableStateOf(EstadoRegistroUI())
        private set

    fun onNombreChange(nuevoNombre: String) {
        uiState = uiState.copy(nombreUsuario = nuevoNombre, mensajeError = null)
    }

    fun onCorreoChange(nuevoCorreo: String) {
        uiState = uiState.copy(correo = nuevoCorreo, mensajeError = null)
    }

    fun onContrasenaChange(nuevaContrasena: String) {
        uiState = uiState.copy(contrasena = nuevaContrasena, mensajeError = null)
    }

    fun onConfirmarContrasenaChange(nuevaConfirmacion: String) {
        uiState = uiState.copy(confirmarContrasena = nuevaConfirmacion, mensajeError = null)
    }

    fun registrarUsuario(onExito: (Usuario) -> Unit) {
        if (uiState.nombreUsuario.isBlank() || uiState.correo.isBlank() || uiState.contrasena.isBlank()) {
            uiState = uiState.copy(mensajeError = "Por favor completa todos los campos")
            return
        }
        if (uiState.contrasena != uiState.confirmarContrasena) {
            uiState = uiState.copy(mensajeError = "Las contraseñas no coinciden")
            return
        }
        if (uiState.contrasena.length < 6) {
            uiState = uiState.copy(mensajeError = "La contraseña debe tener al menos 6 caracteres")
            return
        }
        if (uiState.cargando) return
        uiState = uiState.copy(cargando = true, mensajeError = null)
        viewModelScope.launch {
            when (val r = auth.registrar(uiState.nombreUsuario.trim(), uiState.correo, uiState.contrasena)) {
                is ResultadoAuth.Exito -> {
                    uiState = EstadoRegistroUI()
                    onExito(r.usuario)
                }
                is ResultadoAuth.Error -> uiState = uiState.copy(cargando = false, mensajeError = r.mensaje)
            }
        }
    }
}
