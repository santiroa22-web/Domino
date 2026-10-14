package com.udistrital.domino.model

/** Estado de la pantalla "Sala de espera". */
data class EstadoSalaEsperaUI(
    val codigo: String = "----",
    val nombreAnfitrion: String = "Tú",
    val oponenteConectado: Boolean = false,
    val nombreOponente: String = "Esperando jugador...",
    val dificultad: String = "Normal",
    /** true si este dispositivo creó la sala (solo él puede pulsar "Comenzar"). */
    val esAnfitrion: Boolean = true,
    /** Mientras se crea la sala o se valida el código en Firestore. */
    val cargando: Boolean = false,
    /** El anfitrión comenzó: los invitados navegan solos al tablero. */
    val partidaIniciada: Boolean = false,
    val mensajeError: String? = null
)
