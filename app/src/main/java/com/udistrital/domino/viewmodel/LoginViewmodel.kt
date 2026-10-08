package com.udistrital.domino.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.udistrital.domino.model.EstadoLoginUI

class LoginViewModel : ViewModel() {

    var uiState by mutableStateOf(EstadoLoginUI())
        private set

    fun onCorreoChange(nuevoCorreo: String) {
        uiState = uiState.copy(correo = nuevoCorreo, mensajeError = null)
    }

    fun onContrasenaChange(nuevaContrasena: String) {
        uiState = uiState.copy(contrasena = nuevaContrasena, mensajeError = null)
    }

    fun iniciarSesion(onExito: () -> Unit) {
        if (uiState.correo.isBlank() || uiState.contrasena.isBlank()) {
            uiState = uiState.copy(mensajeError = "Ingresa correo y contraseña")
            return
        }

        uiState = uiState.copy(cargando = true)
        onExito()
    }
}