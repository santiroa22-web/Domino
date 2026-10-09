package com.udistrital.domino.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udistrital.domino.data.ConfigPartida
import com.udistrital.domino.model.EstadoSalaEsperaUI
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ViewModel de la "Sala de espera".
 *
 * En modo local simula que un oponente (CPU) se une a la sala tras un momento,
 * para habilitar el botón de comenzar. Cuando exista Firebase, este ViewModel
 * escuchará la sala real y mostrará a los jugadores que se conecten.
 */
class SalaEsperaViewModel : ViewModel() {

    var uiState by mutableStateOf(EstadoSalaEsperaUI())
        private set

    private var config: ConfigPartida? = null

    /**
     * Abre una sala. Si [codigoExistente] es null, el jugador es anfitrión y se
     * genera un código nuevo (y se simula que la CPU se une). Si se provee un
     * código, el jugador se está uniendo a una sala existente.
     */
    fun abrirSala(
        config: ConfigPartida,
        nombreJugador: String,
        codigoExistente: String? = null
    ) {
        val codigo = codigoExistente?.takeIf { it.isNotBlank() } ?: generarCodigo()
        this.config = config.copy(codigo = codigo)
        val uniendose = codigoExistente != null
        uiState = EstadoSalaEsperaUI(
            codigo = codigo,
            nombreAnfitrion = nombreJugador.ifBlank { "Tú" },
            oponenteConectado = uniendose,
            nombreOponente = if (uniendose) "Anfitrión" else "Esperando jugador...",
            dificultad = config.dificultad
        )
        if (!uniendose) {
            // Simula que el segundo jugador (CPU) se une a la sala.
            viewModelScope.launch {
                delay(1500)
                uiState = uiState.copy(
                    oponenteConectado = true,
                    nombreOponente = nombreCPU(config.dificultad)
                )
            }
        }
    }

    /** Configuración lista para iniciar la partida (incluye el código de sala). */
    fun configParaIniciar(): ConfigPartida? = config

    fun cancelar() {
        config = null
        uiState = EstadoSalaEsperaUI()
    }

    private fun nombreCPU(dificultad: String) = when (dificultad) {
        "Fácil" -> "CPU (Fácil)"
        "Difícil" -> "CPU (Difícil)"
        else -> "CPU"
    }

    private fun generarCodigo(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..4).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }
}
