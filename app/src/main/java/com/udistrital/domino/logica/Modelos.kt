package com.udistrital.domino.logica

/**
 * Posición de una casilla dentro del tablero (fila, columna), ambas base 0.
 * [etiqueta] produce coordenadas tipo "A1", "B3" como en el mockup.
 */
data class Posicion(val fila: Int, val columna: Int) {
    val etiqueta: String get() = "${('A' + columna)}${fila + 1}"

    /** Las 4 posiciones ortogonalmente adyacentes (arriba, abajo, izq, der). */
    fun adyacentes(): List<Posicion> = listOf(
        Posicion(fila - 1, columna),
        Posicion(fila + 1, columna),
        Posicion(fila, columna - 1),
        Posicion(fila, columna + 1)
    )
}

/**
 * Una casilla/territorio del tablero.
 *
 * - [tipo] define el efecto especial y los puntos.
 * - [ficha] es la ficha de dominó colocada encima (null si está vacía).
 * - [conquistadaPor] es el id del jugador que controla el territorio (null si
 *   nadie lo controla todavía).
 */
data class Casilla(
    val posicion: Posicion,
    val tipo: TipoCasilla,
    val ficha: Ficha? = null,
    val conquistadaPor: String? = null
) {
    val ocupada: Boolean get() = ficha != null
    val controlada: Boolean get() = conquistadaPor != null
}

/**
 * Un jugador de la partida.
 *
 * - [mano] son las fichas que tiene disponibles para jugar.
 * - [color] es un color ARGB (Long) que la interfaz usa para pintar los
 *   territorios y fichas de este jugador. Es solo un dato; la lógica no lo usa.
 * - [esIA] indica si lo controla la máquina (modo local sin conexión).
 */
data class Jugador(
    val id: String,
    val nombre: String,
    val color: Long,
    val mano: List<Ficha> = emptyList(),
    val esIA: Boolean = false
)

/** Un movimiento: colocar [ficha] en [posicion]. */
data class Movimiento(val ficha: Ficha, val posicion: Posicion)

/** Fase global de la partida. */
enum class FaseJuego { EN_CURSO, FINALIZADA }

/**
 * Resultado final de la partida, calculado al terminar.
 * [puntajes] y [territorios] están indexados por id de jugador.
 */
data class ResultadoPartida(
    val puntajes: Map<String, Int>,
    val territorios: Map<String, Int>,
    val bonos: Map<String, Int>,
    val penalizaciones: Map<String, Int>,
    val ganadorId: String?,
    val empate: Boolean
)
