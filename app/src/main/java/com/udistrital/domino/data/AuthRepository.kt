package com.udistrital.domino.data

/** Usuario autenticado (mínimo para la app). */
data class Usuario(val id: String, val nombre: String, val correo: String)

/** Resultado de una operación de autenticación. */
sealed class ResultadoAuth {
    data class Exito(val usuario: Usuario) : ResultadoAuth()
    data class Error(val mensaje: String) : ResultadoAuth()
}

/**
 * Autenticación de usuarios. La UI (Login/Registro) habla con esta abstracción.
 *
 * Implementaciones: [AuthLocalRepository] (pruebas sin red) y AuthFirebaseRepository.
 */
interface AuthRepository {
    val usuarioActual: Usuario?
    suspend fun iniciarSesion(correo: String, contrasena: String): ResultadoAuth
    suspend fun registrar(nombre: String, correo: String, contrasena: String): ResultadoAuth
    suspend fun entrarComoInvitado(): ResultadoAuth
    /** Login con el idToken de Google. Solo lo implementa la versión Firebase. */
    suspend fun iniciarSesionConGoogle(idToken: String): ResultadoAuth =
        ResultadoAuth.Error("Login con Google no disponible en modo local")
    fun cerrarSesion()
}

/**
 * Implementación local de prueba: acepta cualquier credencial no vacía. Sirve
 * para navegar y probar la app sin backend.
 */
class AuthLocalRepository : AuthRepository {
    override var usuarioActual: Usuario? = null
        private set

    override suspend fun iniciarSesion(correo: String, contrasena: String): ResultadoAuth {
        if (correo.isBlank() || contrasena.isBlank())
            return ResultadoAuth.Error("Ingresa correo y contraseña")
        val u = Usuario(id = correo, nombre = correo.substringBefore("@"), correo = correo)
        usuarioActual = u
        return ResultadoAuth.Exito(u)
    }

    override suspend fun registrar(nombre: String, correo: String, contrasena: String): ResultadoAuth {
        if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank())
            return ResultadoAuth.Error("Completa todos los campos")
        val u = Usuario(id = correo, nombre = nombre, correo = correo)
        usuarioActual = u
        return ResultadoAuth.Exito(u)
    }

    override suspend fun entrarComoInvitado(): ResultadoAuth {
        val n = (100..999).random()
        val u = Usuario(id = "invitado_$n", nombre = "Invitado_$n", correo = "")
        usuarioActual = u
        return ResultadoAuth.Exito(u)
    }

    override fun cerrarSesion() { usuarioActual = null }
}

// La implementación real con Firebase está en AuthFirebaseRepository.kt
