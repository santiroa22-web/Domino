package com.udistrital.domino.model

data class EstadoLobbyUI(
    val nombreUsuario: String = "Jugador1",
    val monedas: Int = 1250,
    val fotoPerfilUrl: String? = null,
    val codigoIngreso: String = "",
    val mostrandoDialogoUnirse: Boolean = false,
    val mensajeError: String? = null
)