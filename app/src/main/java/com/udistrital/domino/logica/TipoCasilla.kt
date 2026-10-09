package com.udistrital.domino.logica

/**
 * Tipos de casilla (territorio) del tablero de Dominó Conquista.
 *
 * Cada tipo define cuántos puntos otorga al jugador que conquista el territorio
 * y una descripción corta que se muestra en la pantalla de "Tipos de casillas".
 *
 * Esta clase es LÓGICA PURA: no depende de Android ni de Compose, de modo que
 * la regla de juego queda totalmente separada de la interfaz gráfica.
 */
enum class TipoCasilla(
    val etiqueta: String,
    val puntos: Int,
    val descripcion: String
) {
    NORMAL("Normal", 0, "Sin efecto especial."),
    BONUS("Bonus", 3, "+3 puntos al final de la partida."),
    TRAMPA("Trampa", -2, "-2 puntos al final de la partida."),
    FORTALEZA("Fortaleza", 4, "Necesita dos conquistas para ser controlada."),
    TESORO("Tesoro", 5, "+5 puntos al final de la partida."),
    PORTAL("Portal", 0, "Permite mover una ficha colocada a otra casilla válida.");

    val esEspecial: Boolean get() = this != NORMAL
}
