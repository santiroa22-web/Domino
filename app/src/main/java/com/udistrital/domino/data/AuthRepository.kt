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
 * Implementación LOCAL por ahora; la de Firebase queda pendiente (ver abajo).
 */
interface AuthRepository {
    val usuarioActual: Usuario?
    suspend fun iniciarSesion(correo: String, contrasena: String): ResultadoAuth
    suspend fun registrar(nombre: String, correo: String, contrasena: String): ResultadoAuth
    suspend fun entrarComoInvitado(): ResultadoAuth
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

/**
 * ⚠️ PENDIENTE DE CONEXIÓN A LA BASE DE DATOS (Firebase Auth).
 *
 * Debe envolver FirebaseAuth:
 *  - iniciarSesion -> signInWithEmailAndPassword
 *  - registrar     -> createUserWithEmailAndPassword (+ guardar nombre en perfil/Firestore)
 *  - entrarComoInvitado -> signInAnonymously
 *  - usuarioActual -> FirebaseAuth.getInstance().currentUser
 * Opcionalmente, login con Google mediante GoogleAuthProvider.
 */
class AuthFirebaseRepository : AuthRepository {
    override val usuarioActual: Usuario? get() = TODO("FirebaseAuth.currentUser")
    override suspend fun iniciarSesion(correo: String, contrasena: String): ResultadoAuth =
        TODO("signInWithEmailAndPassword")
    override suspend fun registrar(nombre: String, correo: String, contrasena: String): ResultadoAuth =
        TODO("createUserWithEmailAndPassword")
    override suspend fun entrarComoInvitado(): ResultadoAuth =
        TODO("signInAnonymously")
    override fun cerrarSesion() = TODO("FirebaseAuth.signOut()")
}
