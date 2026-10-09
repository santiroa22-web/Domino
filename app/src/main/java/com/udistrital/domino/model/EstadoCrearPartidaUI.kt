package com.udistrital.domino.model

/** Estado de la pantalla "Crear partida". */
data class EstadoCrearPartidaUI(
    val modo: String = "Dominó Conquista",
    val numJugadores: Int = 2,
    val dificultad: String = "Normal",
    val expandirJugadores: Boolean = false,
    val expandirDificultad: Boolean = false
)
