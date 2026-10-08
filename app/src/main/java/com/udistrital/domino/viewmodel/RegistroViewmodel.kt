package com.udistrital.domino.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.udistrital.domino.model.EstadoRegistroUI

class RegistroViewModel : ViewModel() {

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

    fun registrarUsuario(onExito: () -> Unit) {
        if (uiState.nombreUsuario.isBlank() || uiState.correo.isBlank() || uiState.contrasena.isBlank()) {
            uiState = uiState.copy(mensajeError = "Por favor completa todos los campos")
            return
        }

        if (uiState.contrasena != uiState.confirmarContrasena) {
            uiState = uiState.copy(mensajeError = "Las contraseñas no coinciden")
            return
        }

        uiState = uiState.copy(cargando = true)
        onExito()
    }
}