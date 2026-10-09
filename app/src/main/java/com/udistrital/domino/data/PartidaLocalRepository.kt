package com.udistrital.domino.data

import com.udistrital.domino.logica.EstadoJuego
import com.udistrital.domino.logica.EstrategiaIA
import com.udistrital.domino.logica.Jugador
import com.udistrital.domino.logica.MotorDomino
import com.udistrital.domino.logica.Movimiento
import com.udistrital.domino.logica.Posicion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Implementación LOCAL (sin red) del repositorio de partida.
 *
 * El jugador local es "p1"; el oponente "p2" lo controla la [EstrategiaIA]. Esto
 * permite jugar y demostrar toda la lógica en un solo dispositivo mientras se
 * conecta Firebase. Los movimientos de la IA se ejecutan con una pequeña pausa
 * para que la partida se sienta natural.
 */
class PartidaLocalRepository(
    private val scope: CoroutineScope,
    private val seed: Long = System.nanoTime(),
    private val pausaIaMs: Long = 750L
) : PartidaRepository {

    private val _estado = MutableStateFlow<EstadoJuego?>(null)
    override val estado: StateFlow<EstadoJuego?> = _estado.asStateFlow()

    override val miId: String = "p1"
    override var codigoSala: String = ""
        private set

    private val ia = EstrategiaIA(Random(seed))
    private var config: ConfigPartida? = null
    private var jobIA: Job? = null

    override fun iniciar(config: ConfigPartida) {
        this.config = config
        codigoSala = config.codigo?.takeIf { it.isNotBlank() } ?: generarCodigo()
        val jugadores = listOf(
            Jugador(
                id = miId,
                nombre = config.nombreJugador.ifBlank { "Tú" },
                color = ColoresJugador.JUGADOR_1,
                esIA = false
            ),
            Jugador(
                id = "p2",
                nombre = nombreOponente(config.dificultad),
                color = ColoresJugador.JUGADOR_2,
                esIA = true
            )
        )
        _estado.value = MotorDomino.nuevaPartida(
            configJugadores = jugadores,
            maxRondas = config.maxRondas,
            seed = seed
        )
        procesarTurnosIA()
    }

    override fun colocarFicha(movimiento: Movimiento) {
        val e = _estado.value ?: return
        if (e.jugadorEnTurno.id != miId || e.terminada) return
        _estado.value = MotorDomino.aplicarMovimiento(e, movimiento, miId)
        procesarTurnosIA()
    }

    override fun robarOPasar() {
        val e = _estado.value ?: return
        if (e.jugadorEnTurno.id != miId || e.terminada) return
        _estado.value = MotorDomino.robarOPasar(e, miId)
        procesarTurnosIA()
    }

    override fun usarPortal(origen: Posicion, destino: Posicion) {
        val e = _estado.value ?: return
        if (e.jugadorEnTurno.id != miId || !e.accionPortalPendiente) return
        _estado.value = MotorDomino.usarPortal(e, origen, destino, miId)
        procesarTurnosIA()
    }

    override fun omitirPortal() {
        val e = _estado.value ?: return
        if (e.jugadorEnTurno.id != miId || !e.accionPortalPendiente) return
        _estado.value = MotorDomino.omitirPortal(e)
        procesarTurnosIA()
    }

    override fun reiniciar() {
        config?.let { iniciar(it) }
    }

    override fun abandonar() {
        jobIA?.cancel()
        _estado.value = null
        codigoSala = ""
    }

    /** Ejecuta, con pausas, todos los turnos consecutivos que correspondan a la IA. */
    private fun procesarTurnosIA() {
        if (jobIA?.isActive == true) return
        jobIA = scope.launch {
            while (true) {
                val e = _estado.value ?: return@launch
                if (e.terminada || !e.jugadorEnTurno.esIA) return@launch
                delay(pausaIaMs)
                val actual = _estado.value ?: return@launch
                if (actual.terminada || !actual.jugadorEnTurno.esIA) return@launch
                val id = actual.jugadorEnTurno.id
                _estado.value = if (actual.accionPortalPendiente) {
                    val decision = ia.decidirPortal(actual, id)
                    if (decision != null) MotorDomino.usarPortal(actual, decision.first, decision.second, id)
                    else MotorDomino.omitirPortal(actual)
                } else {
                    val mov = ia.mejorMovimiento(actual, id)
                    if (mov != null) MotorDomino.aplicarMovimiento(actual, mov, id)
                    else MotorDomino.robarOPasar(actual, id)
                }
            }
        }
    }

    private fun nombreOponente(dificultad: String): String = when (dificultad) {
        "Fácil" -> "CPU (Fácil)"
        "Difícil" -> "CPU (Difícil)"
        else -> "CPU"
    }

    private fun generarCodigo(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..4).map { chars[Random(seed + it).nextInt(chars.length)] }.joinToString("")
    }
}
