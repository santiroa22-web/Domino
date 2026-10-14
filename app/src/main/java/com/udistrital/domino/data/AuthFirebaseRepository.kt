package com.udistrital.domino.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Implementación real de [AuthRepository] con Firebase Authentication.
 *
 * REEMPLAZA la clase stub `AuthFirebaseRepository` que hoy está al final de
 * AuthRepository.kt (bórrala de ese archivo para no duplicar el nombre).
 *
 * Además de autenticar, crea/actualiza el perfil en `usuarios/{uid}` para que el
 * Lobby muestre el nombre real y el historial tenga dónde colgar.
 */
class AuthFirebaseRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override val usuarioActual: Usuario?
        get() = auth.currentUser?.aUsuario()

    override suspend fun iniciarSesion(correo: String, contrasena: String): ResultadoAuth {
        if (correo.isBlank() || contrasena.isBlank()) return ResultadoAuth.Error("Ingresa correo y contraseña")
        return ejecutar {
            auth.signInWithEmailAndPassword(correo.trim(), contrasena).await().user!!.aUsuario()
        }
    }

    override suspend fun registrar(nombre: String, correo: String, contrasena: String): ResultadoAuth {
        if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank())
            return ResultadoAuth.Error("Completa todos los campos")
        return ejecutar {
            val user = auth.createUserWithEmailAndPassword(correo.trim(), contrasena).await().user!!
            user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(nombre).build()).await()
            guardarPerfil(user.uid, nombre, correo.trim())
            Usuario(user.uid, nombre, correo.trim())
        }
    }

    override suspend fun entrarComoInvitado(): ResultadoAuth = ejecutar {
        // Requiere habilitar "Anónimo" en Authentication > Sign-in method.
        val user = auth.signInAnonymously().await().user!!
        val nombre = "Invitado_${user.uid.take(4)}"
        user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(nombre).build()).await()
        guardarPerfil(user.uid, nombre, "")
        Usuario(user.uid, nombre, "")
    }

    /**
     * Login con Google. El idToken lo obtiene la UI con Credential Manager
     * (ver GoogleSignInHelper.kt) porque necesita un Context de Activity.
     */
    suspend fun iniciarSesionConGoogle(idToken: String): ResultadoAuth = ejecutar {
        val credencial = GoogleAuthProvider.getCredential(idToken, null)
        val user = auth.signInWithCredential(credencial).await().user!!
        guardarPerfil(user.uid, user.displayName ?: "Jugador", user.email.orEmpty(), user.photoUrl?.toString())
        user.aUsuario()
    }

    override fun cerrarSesion() = auth.signOut()

    // ---------------------------------------------------------------------

    private suspend fun guardarPerfil(uid: String, nombre: String, correo: String, foto: String? = null) {
        db.collection(Colecciones.USUARIOS).document(uid).set(
            mapOf(
                "nombre" to nombre,
                "correo" to correo,
                "fotoUrl" to foto,
                "ultimoIngreso" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge() // no pisa victorias/partidasJugadas si ya existen
        ).await()
    }

    private inline fun ejecutar(bloque: () -> Usuario): ResultadoAuth = try {
        ResultadoAuth.Exito(bloque())
    } catch (e: Exception) {
        ResultadoAuth.Error(traducirError(e))
    }

    /** Mensajes en español para la UI. El orden importa: WeakPassword hereda de InvalidCredentials. */
    private fun traducirError(e: Exception): String = when (e) {
        is FirebaseAuthWeakPasswordException -> "La contraseña debe tener al menos 6 caracteres"
        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo"
        is FirebaseAuthInvalidUserException -> "No existe una cuenta con ese correo"
        is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos"
        else -> e.message ?: "Error de autenticación"
    }
}

private fun FirebaseUser.aUsuario() = Usuario(
    id = uid,
    nombre = displayName?.takeIf { it.isNotBlank() } ?: email?.substringBefore("@") ?: "Jugador",
    correo = email.orEmpty()
)

/** Nombres de colecciones en un solo lugar. */
object Colecciones {
    const val USUARIOS = "usuarios"
    const val HISTORIAL = "historial"   // subcolección: usuarios/{uid}/historial
    const val SALAS = "salas"
    const val MOVIMIENTOS = "movimientos" // subcolección opcional: salas/{codigo}/movimientos
}
