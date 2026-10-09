package com.udistrital.domino.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.udistrital.domino.data.ConfigPartida
import com.udistrital.domino.model.EstadoCrearPartidaUI

/** ViewModel de la pantalla "Crear partida". */
class CrearPartidaViewModel : ViewModel() {

    var uiState by mutableStateOf(EstadoCrearPartidaUI())
        private set

    val opcionesJugadores = listOf(2)
    val opcionesDificultad = listOf("Fácil", "Normal", "Difícil")

    fun onNumJugadoresChange(n: Int) {
        uiState = uiState.copy(numJugadores = n, expandirJugadores = false)
    }

    fun onDificultadChange(d: String) {
        uiState = uiState.copy(dificultad = d, expandirDificultad = false)
    }

    fun expandirJugadores(v: Boolean) { uiState = uiState.copy(expandirJugadores = v) }
    fun expandirDificultad(v: Boolean) { uiState = uiState.copy(expandirDificultad = v) }

    /** Construye la configuración a partir del estado actual. */
    fun construirConfig(nombreJugador: String): ConfigPartida = ConfigPartida(
        nombreJugador = nombreJugador,
        modo = uiState.modo,
        numJugadores = uiState.numJugadores,
        dificultad = uiState.dificultad
    )
}
