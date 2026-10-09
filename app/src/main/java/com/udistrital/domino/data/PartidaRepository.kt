package com.udistrital.domino.data

import com.udistrital.domino.logica.EstadoJuego
import com.udistrital.domino.logica.Movimiento
import com.udistrital.domino.logica.Posicion
import kotlinx.coroutines.flow.StateFlow

/** Configuración con la que se crea una partida. */
data class ConfigPartida(
    val nombreJugador: String,
    val modo: String = "Dominó Conquista",
    val numJugadores: Int = 2,
    val dificultad: String = "Normal",
    val maxRondas: Int = 20,
    /** Código de sala preexistente (al unirse o compartir); null = se genera uno. */
    val codigo: String? = null
)

/** Colores por defecto para los jugadores (ARGB). La UI los usa para pintar. */
object ColoresJugador {
    const val JUGADOR_1 = 0xFF4CAF50 // verde
    const val JUGADOR_2 = 0xFF1565C0 // azul
}

/**
 * Fuente de verdad de una partida. La interfaz (ViewModel) solo habla con esta
 * abstracción, de modo que cambiar de "local" a "en línea con Firebase" no
 * obliga a tocar la UI ni el motor del juego.
 *
 * El [estado] se expone como un flujo observable: en local lo actualiza el
 * motor; en línea lo actualizará el listener de Firestore.
 */
interface PartidaRepository {
    /** Estado actual de la partida (null si aún no se ha creado). */
    val estado: StateFlow<EstadoJuego?>

    /** Id del jugador local (el que usa este dispositivo). */
    val miId: String

    /** Código de la sala (para unirse/compartir). */
    val codigoSala: String

    /** Crea/inicia una partida con la configuración dada. */
    fun iniciar(config: ConfigPartida)

    /** El jugador local coloca una ficha. */
    fun colocarFicha(movimiento: Movimiento)

    /** El jugador local roba del pozo o pierde el turno si no puede. */
    fun robarOPasar()

    /** Resuelve el portal moviendo una ficha de [origen] a [destino]. */
    fun usarPortal(origen: Posicion, destino: Posicion)

    /** El jugador local decide no usar el portal. */
    fun omitirPortal()

    /** Reinicia la partida con la misma configuración. */
    fun reiniciar()

    /** Abandona/cierra la partida y libera recursos. */
    fun abandonar()
}
