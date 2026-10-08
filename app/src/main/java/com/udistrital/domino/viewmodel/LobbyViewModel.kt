package com.udistrital.domino.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.udistrital.domino.model.EstadoLobbyUI

class LobbyViewModel : ViewModel() {

    var uiState by mutableStateOf(EstadoLobbyUI())
        private set

    fun onCodigoChange(nuevoCodigo: String) {
        uiState = uiState.copy(codigoIngreso = nuevoCodigo, mensajeError = null)
    }

    fun mostrarDialogoUnirse(mostrar: Boolean) {
        uiState = uiState.copy(mostrandoDialogoUnirse = mostrar, mensajeError = null)
    }

    fun unirseAPartidaPorCodigo(onExito: (String) -> Unit) {
        if (uiState.codigoIngreso.isBlank()) {
            uiState = uiState.copy(mensajeError = "Ingresa un código válido")
            return
        }

        // Procesa la entrada a la sala y navega
        val codigo = uiState.codigoIngreso.uppercase()
        uiState = uiState.copy(mostrandoDialogoUnirse = false, codigoIngreso = "")
        onExito(codigo)
    }
}