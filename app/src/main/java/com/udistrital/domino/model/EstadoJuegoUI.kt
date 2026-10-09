package com.udistrital.domino.model

import com.udistrital.domino.logica.Posicion

/**
 * Estado de interfaz de la pantalla de juego (lo que NO forma parte del estado
 * lógico de la partida): qué ficha está seleccionada, si se está usando un
 * portal, y qué diálogos están abiertos.
 */
data class EstadoJuegoUI(
    val fichaSeleccionadaId: Int? = null,
    val rotadas: Set<Int> = emptySet(),
    val modoPortal: Boolean = false,
    val origenPortal: Posicion? = null,
    val mostrarTiposCasilla: Boolean = false,
    val mostrarDialogoSalir: Boolean = false
)
