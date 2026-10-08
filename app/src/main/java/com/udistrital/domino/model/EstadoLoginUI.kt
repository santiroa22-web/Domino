package com.udistrital.domino.model

data class EstadoLoginUI(
    val correo: String = "",
    val contrasena: String = "",
    val cargando: Boolean = false,
    val mensajeError: String? = null
)