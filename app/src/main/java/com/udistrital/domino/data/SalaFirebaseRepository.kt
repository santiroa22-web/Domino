package com.udistrital.domino.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** Jugador dentro de la sala de espera. */
data class JugadorSala(val id: String, val nombre: String)

/** Estado de la sala tal como lo ve la pantalla "Sala de espera". */
data class InfoSala(
    val codigo: String,
    val anfitrionId: String,
    val jugadores: List<JugadorSala>,
    val maxJugadores: Int,
    val estado: String // ESPERANDO | EN_CURSO | FINALIZADA | ABANDONADA
) {
    val llena: Boolean get() = jugadores.size >= maxJugadores
}

object EstadoSala {
    const val ESPERANDO = "ESPERANDO"
    const val EN_CURSO = "EN_CURSO"
    const val FINALIZADA = "FINALIZADA"
    const val ABANDONADA = "ABANDONADA"
}

/**
 * Crear sala / unirse por código / escuchar quién entra.
 *
 * Documento: salas/{codigo}
 * {
 *   codigo, anfitrionId, estado, maxJugadores, maxRondas, dificultad,
 *   jugadores:   [ {id, nombre} ],      // para mostrar
 *   jugadorIds:  [ uid, uid ],          // para las reglas de seguridad
 *   creadaEn, actualizadoEn, version,
 *   juego: { ...EstadoJuego serializado... }   // lo escribe PartidaFirebaseRepository
 * }
 */
class SalaFirebaseRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private fun ref(codigo: String) = db.collection(Colecciones.SALAS).document(codigo)
    private val uid: String get() = auth.currentUser?.uid ?: error("Debes iniciar sesión")

    /** Crea la sala con un código de 4 caracteres que no exista. Devuelve el código. */
    suspend fun crearSala(config: ConfigPartida, nombre: String): String {
        repeat(5) {
            val codigo = generarCodigo()
            val creada = db.runTransaction { tx ->
                if (tx.get(ref(codigo)).exists()) return@runTransaction false
                tx.set(
                    ref(codigo), mapOf(
                        "codigo" to codigo,
                        "anfitrionId" to uid,
                        "estado" to EstadoSala.ESPERANDO,
                        "maxJugadores" to config.numJugadores,
                        "maxRondas" to config.maxRondas,
                        "dificultad" to config.dificultad,
                        "jugadores" to listOf(mapOf("id" to uid, "nombre" to nombre)),
                        "jugadorIds" to listOf(uid),
                        "version" to 0L,
                        "creadaEn" to FieldValue.serverTimestamp()
                    )
                )
                true
            }.await()
            if (creada) return codigo
        }
        error("No se pudo generar un código de sala único")
    }

    /** Se une a una sala existente. Lanza excepción con mensaje legible si no se puede. */
    suspend fun unirse(codigo: String, nombre: String) {
        db.runTransaction { tx ->
            val snap = tx.get(ref(codigo))
            if (!snap.exists()) throw IllegalStateException("No existe la sala $codigo")
            @Suppress("UNCHECKED_CAST")
            val ids = (snap.get("jugadorIds") as? List<String>).orEmpty()
            if (uid in ids) return@runTransaction Unit // ya estaba (p. ej. reconexión)
            if (snap.getString("estado") != EstadoSala.ESPERANDO) throw IllegalStateException("La partida ya comenzó")
            val max = (snap.getLong("maxJugadores") ?: 2L).toInt()
            if (ids.size >= max) throw IllegalStateException("La sala está llena")
            tx.update(
                ref(codigo), mapOf(
                    "jugadores" to FieldValue.arrayUnion(mapOf("id" to uid, "nombre" to nombre)),
                    "jugadorIds" to FieldValue.arrayUnion(uid)
                )
            )
            Unit
        }.await()
    }

    /** Flujo en tiempo real de la sala (la Sala de espera hace collect de esto). */
    fun observar(codigo: String): Flow<InfoSala?> = callbackFlow {
        val registro = ref(codigo).addSnapshotListener { snap, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            if (snap == null || !snap.exists()) { trySend(null); return@addSnapshotListener }
            @Suppress("UNCHECKED_CAST")
            val jugadores = (snap.get("jugadores") as? List<Map<String, Any?>>).orEmpty()
                .map { JugadorSala(it["id"] as String, it["nombre"] as String) }
            trySend(
                InfoSala(
                    codigo = codigo,
                    anfitrionId = snap.getString("anfitrionId").orEmpty(),
                    jugadores = jugadores,
                    maxJugadores = (snap.getLong("maxJugadores") ?: 2L).toInt(),
                    estado = snap.getString("estado") ?: EstadoSala.ESPERANDO
                )
            )
        }
        awaitClose { registro.remove() } // se quita el listener al salir de la pantalla
    }

    /** El anfitrión cancela (borra la sala); un invitado solo se sale. */
    suspend fun salir(codigo: String) {
        val snap = ref(codigo).get().await()
        if (!snap.exists()) return
        if (snap.getString("anfitrionId") == uid) {
            ref(codigo).delete().await()
        } else {
            @Suppress("UNCHECKED_CAST")
            val miEntrada = (snap.get("jugadores") as? List<Map<String, Any?>>).orEmpty().firstOrNull { it["id"] == uid }
            ref(codigo).update(
                mapOf(
                    "jugadores" to FieldValue.arrayRemove(miEntrada),
                    "jugadorIds" to FieldValue.arrayRemove(uid)
                )
            ).await()
        }
    }

    private fun generarCodigo(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..4).map { chars.random() }.joinToString("")
    }
}
