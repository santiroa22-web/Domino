package com.udistrital.domino.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udistrital.domino.AppConfig
import com.udistrital.domino.data.ConfigPartida
import com.udistrital.domino.data.EstadoSala
import com.udistrital.domino.data.SalaFirebaseRepository
import com.udistrital.domino.model.EstadoSalaEsperaUI
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ViewModel de la "Sala de espera".
 *
 * En línea: crea la sala o se une por código en Firestore y ESCUCHA el documento
 * salas/{codigo} en tiempo real, así ambos jugadores ven quién está conectado y
 * el invitado detecta cuando el anfitrión comienza la partida.
 *
 * Local (AppConfig.MODO_EN_LINEA = false): simula que una CPU se une.
 */
class SalaEsperaViewModel(
    private val salas: SalaFirebaseRepository = SalaFirebaseRepository()
) : ViewModel() {

    var uiState by mutableStateOf(EstadoSalaEsperaUI())
        private set

    private var config: ConfigPartida? = null
    private var jobSala: Job? = null

    /**
     * Si [codigoExistente] es null este jugador es el anfitrión y se crea una
     * sala nueva; si no, se une a la sala con ese código.
     */
    fun abrirSala(config: ConfigPartida, nombreJugador: String, codigoExistente: String? = null) {
        jobSala?.cancel()
        if (!AppConfig.MODO_EN_LINEA) {
            abrirSalaLocal(config, nombreJugador, codigoExistente)
            return
        }
        val nombre = nombreJugador.ifBlank { "Jugador" }
        val soyAnfitrion = codigoExistente == null
        uiState = EstadoSalaEsperaUI(
            nombreAnfitrion = if (soyAnfitrion) nombre else "Anfitrión",
            esAnfitrion = soyAnfitrion,
            dificultad = config.dificultad,
            cargando = true
        )
        viewModelScope.launch {
            try {
                val codigo = if (soyAnfitrion) {
                    salas.crearSala(config, nombre)
                } else {
                    salas.unirse(codigoExistente!!, nombre)
                    codigoExistente
                }
                this@SalaEsperaViewModel.config = config.copy(codigo = codigo)
                uiState = uiState.copy(codigo = codigo, cargando = false)
                escuchar(codigo)
            } catch (e: Exception) {
                uiState = uiState.copy(cargando = false, mensajeError = e.message ?: "No se pudo abrir la sala")
            }
        }
    }

    /** Listener en tiempo real de la sala. */
    private fun escuchar(codigo: String) {
        jobSala = viewModelScope.launch {
            salas.observar(codigo)
                .catch { e -> uiState = uiState.copy(mensajeError = e.message) }
                .collect { info ->
                    if (info == null) {
                        uiState = uiState.copy(
                            oponenteConectado = false,
                            mensajeError = "El anfitrión canceló la sala"
                        )
                        return@collect
                    }
                    val anfitrion = info.jugadores.firstOrNull { it.id == info.anfitrionId }
                    val invitado = info.jugadores.firstOrNull { it.id != info.anfitrionId }
                    uiState = uiState.copy(
                        nombreAnfitrion = anfitrion?.nombre ?: "Anfitrión",
                        nombreOponente = invitado?.nombre ?: "Esperando jugador...",
                        oponenteConectado = info.llena,
                        partidaIniciada = info.estado == EstadoSala.EN_CURSO
                    )
                }
        }
    }

    /** Configuración lista para iniciar la partida (incluye el código de sala). */
    fun configParaIniciar(): ConfigPartida? = config

    /** Anfitrión: borra la sala. Invitado: se sale de ella. */
    fun cancelar() {
        val codigo = config?.codigo
        jobSala?.cancel()
        config = null
        uiState = EstadoSalaEsperaUI()
        if (AppConfig.MODO_EN_LINEA && codigo != null) {
            viewModelScope.launch { runCatching { salas.salir(codigo) } }
        }
    }

    // ---------------- Modo local (CPU) ----------------

    private fun abrirSalaLocal(config: ConfigPartida, nombreJugador: String, codigoExistente: String?) {
        val codigo = codigoExistente?.takeIf { it.isNotBlank() } ?: generarCodigo()
        this.config = config.copy(codigo = codigo)
        uiState = EstadoSalaEsperaUI(
            codigo = codigo,
            nombreAnfitrion = nombreJugador.ifBlank { "Tú" },
            dificultad = config.dificultad
        )
        jobSala = viewModelScope.launch {
            delay(1500)
            uiState = uiState.copy(oponenteConectado = true, nombreOponente = nombreCPU(config.dificultad))
        }
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
