package com.udistrital.domino.model

// Data class pura que define el estado de la pantalla de inicio
data class EstadoInicioUI(
    val cargandoGoogle: Boolean = false,
    val mensajeError: String? = null
)