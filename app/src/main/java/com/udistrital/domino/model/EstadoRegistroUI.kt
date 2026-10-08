package com.udistrital.domino.model

data class EstadoRegistroUI(
    val nombreUsuario: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val cargando: Boolean = false,
    val mensajeError: String? = null
)