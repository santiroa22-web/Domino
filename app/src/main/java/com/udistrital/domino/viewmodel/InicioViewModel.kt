package com.udistrital.domino.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.udistrital.domino.model.EstadoInicioUI

class InicioViewModel : ViewModel() {

    var uiState by mutableStateOf(EstadoInicioUI())
        private set

    fun onEntrarComoInvitado(onExito: (String) -> Unit) {
        // Genera un nombre aleatorio como 'Invitado_384'
        val numeroRandom = (100..999).random()
        val nombreInvitado = "Invitado_$numeroRandom"

        onExito(nombreInvitado)
    }
}