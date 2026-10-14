package com.udistrital.domino.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.udistrital.domino.logica.EstadoJuego
import com.udistrital.domino.logica.MotorDomino
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Historial en usuarios/{uid}/historial/{codigoSala}.
 *
 * Cada jugador guarda su propio registro al terminar la partida
 * (lo llama PartidaFirebaseRepository).
 */
class HistorialFirebaseRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : HistorialRepository {

    private fun coleccion(uid: String) =
        db.collection(Colecciones.USUARIOS).document(uid).collection(Colecciones.HISTORIAL)

    override suspend fun obtenerHistorial(): List<RegistroPartida> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val formato = SimpleDateFormat("d MMM yyyy", Locale("es", "CO"))
        return coleccion(uid)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(30)
            .get().await()
            .documents.map { d ->
                RegistroPartida(
                    id = d.id,
                    oponente = d.getString("oponente").orEmpty(),
                    gano = d.getBoolean("gano") ?: false,
                    misPuntos = (d.getLong("misPuntos") ?: 0L).toInt(),
                    puntosOponente = (d.getLong("puntosOponente") ?: 0L).toInt(),
                    fecha = d.getTimestamp("fecha")?.toDate()?.let(formato::format).orEmpty()
                )
            }
    }

    /**
     * Cada jugador guarda SU registro al terminar (idempotente: el id del
     * documento es el código de sala, así que repetirlo no duplica).
     */
    suspend fun guardar(codigoSala: String, estado: EstadoJuego, miId: String) {
        val resultado = MotorDomino.resultado(estado)
        val rival = estado.jugadores.firstOrNull { it.id != miId } ?: return
        coleccion(miId).document(codigoSala).set(
            mapOf(
                "oponente" to rival.nombre,
                "gano" to (resultado.ganadorId == miId),
                "empate" to resultado.empate,
                "misPuntos" to (resultado.puntajes[miId] ?: 0),
                "puntosOponente" to (resultado.puntajes[rival.id] ?: 0),
                "misTerritorios" to (resultado.territorios[miId] ?: 0),
                "fecha" to Timestamp.now()
            )
        ).await()
        // Estadísticas agregadas del perfil (para el Lobby).
        db.collection(Colecciones.USUARIOS).document(miId).set(
            mapOf(
                "partidasJugadas" to FieldValue.increment(1),
                "victorias" to FieldValue.increment(if (resultado.ganadorId == miId) 1 else 0)
            ),
            SetOptions.merge()
        ).await()
    }
}
