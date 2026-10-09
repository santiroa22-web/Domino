package com.udistrital.domino.model

/** Estado de la pantalla "Sala de espera". */
data class EstadoSalaEsperaUI(
    val codigo: String = "----",
    val nombreAnfitrion: String = "Tú",
    val oponenteConectado: Boolean = false,
    val nombreOponente: String = "Esperando jugador...",
    val dificultad: String = "Normal"
)
