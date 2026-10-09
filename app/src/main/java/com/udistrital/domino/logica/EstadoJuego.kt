package com.udistrital.domino.logica

/**
 * Estado completo e inmutable de una partida en un instante dado.
 *
 * Es un "snapshot": el [MotorDomino] nunca muta este objeto, sino que produce
 * copias nuevas. Esto hace la lógica predecible y fácil de sincronizar más
 * adelante con Firebase (basta con serializar/escuchar este estado).
 *
 * El tablero se guarda como filas x columnas de [Casilla].
 */
data class EstadoJuego(
    val tablero: List<List<Casilla>>,
    val jugadores: List<Jugador>,
    val turnoActual: Int,
    val pozo: List<Ficha>,
    val ronda: Int,
    val maxRondas: Int,
    val fase: FaseJuego,
    val mensaje: String? = null,
    /**
     * Cuando un jugador conquista un Portal, gana el derecho a reubicar una de
     * sus fichas ya colocadas. Mientras esto sea true el turno no avanza hasta
     * que el jugador use el portal o lo omita.
     */
    val accionPortalPendiente: Boolean = false
) {
    val filas: Int get() = tablero.size
    val columnas: Int get() = tablero.firstOrNull()?.size ?: 0
    val jugadorEnTurno: Jugador get() = jugadores[turnoActual]
    val terminada: Boolean get() = fase == FaseJuego.FINALIZADA

    fun casilla(p: Posicion): Casilla? = tablero.getOrNull(p.fila)?.getOrNull(p.columna)

    fun jugador(id: String): Jugador? = jugadores.firstOrNull { it.id == id }

    /** Todas las casillas del tablero en una sola lista. */
    fun todasLasCasillas(): List<Casilla> = tablero.flatten()

    /** Devuelve una copia del estado con una casilla transformada. */
    fun conCasilla(p: Posicion, transform: (Casilla) -> Casilla): EstadoJuego {
        val nuevoTablero = tablero.mapIndexed { f, fila ->
            fila.mapIndexed { c, cas ->
                if (f == p.fila && c == p.columna) transform(cas) else cas
            }
        }
        return copy(tablero = nuevoTablero)
    }

    /** Devuelve una copia del estado con un jugador transformado. */
    fun conJugador(id: String, transform: (Jugador) -> Jugador): EstadoJuego {
        val nuevos = jugadores.map { if (it.id == id) transform(it) else it }
        return copy(jugadores = nuevos)
    }

    /** true si existe al menos una ficha colocada en el tablero. */
    fun hayFichasEnTablero(): Boolean = todasLasCasillas().any { it.ocupada }
}
