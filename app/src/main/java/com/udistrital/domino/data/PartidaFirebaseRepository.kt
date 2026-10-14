package com.udistrital.domino.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.udistrital.domino.logica.EstadoJuego
import com.udistrital.domino.logica.FaseJuego
import com.udistrital.domino.logica.Jugador
import com.udistrital.domino.logica.MotorDomino
import com.udistrital.domino.logica.Movimiento
import com.udistrital.domino.logica.Posicion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Partida en línea sincronizada con Firestore. Mismo contrato que
 * [PartidaLocalRepository]: el ViewModel y las pantallas no cambian.
 *
 * Modelo de sincronización ("cliente con autoridad + transacción"):
 *  - TODO el estado vive en un solo documento salas/{codigo}.juego, así una
 *    jugada (tablero + mano + pozo + turno + puntaje) se escribe de forma atómica.
 *  - Cada jugada se hace en una transacción: se lee el estado actual, se valida
 *    que sea MI turno, se aplica el [MotorDomino] y se escribe el nuevo estado.
 *    Si dos dispositivos escriben a la vez, Firestore reintenta la transacción y
 *    el segundo ve que ya no es su turno → no hay estados corruptos.
 *  - Todos los dispositivos escuchan el documento con addSnapshotListener y
 *    publican el estado en [estado]; Compose se recompone solo.
 */
class PartidaFirebaseRepository(
    private val scope: CoroutineScope,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val historial: HistorialFirebaseRepository = HistorialFirebaseRepository(db, auth)
) : PartidaRepository {

    private val _estado = MutableStateFlow<EstadoJuego?>(null)
    override val estado: StateFlow<EstadoJuego?> = _estado.asStateFlow()

    /** Errores de red/transacción para mostrarlos en un Snackbar. */
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    override val miId: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")
    override var codigoSala: String = ""
        private set

    private var config: ConfigPartida? = null
    private var listener: ListenerRegistration? = null
    private var historialGuardado = false

    private fun salaRef() = db.collection(Colecciones.SALAS).document(codigoSala)

    // ------------------------------------------------------------------
    // Ciclo de vida
    // ------------------------------------------------------------------

    /** Lo llaman TODOS los jugadores al entrar a la pantalla de juego. */
    override fun iniciar(config: ConfigPartida) {
        this.config = config
        codigoSala = requireNotNull(config.codigo) { "En línea se necesita el código de sala" }
        historialGuardado = false
        scope.launch {
            try {
                crearJuegoSiNoExiste(config)
                escuchar()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    /**
     * Solo el PRIMER cliente que llega crea el estado inicial (reparto de fichas,
     * tablero aleatorio). La transacción garantiza que no se reparta dos veces.
     */
    private suspend fun crearJuegoSiNoExiste(config: ConfigPartida, forzar: Boolean = false) {
        db.runTransaction { tx ->
            val snap = tx.get(salaRef())
            if (!forzar && snap.get(CAMPO_JUEGO) != null) return@runTransaction false
            @Suppress("UNCHECKED_CAST")
            val jugadores = (snap.get("jugadores") as? List<Map<String, Any?>>).orEmpty()
                .mapIndexed { i, j ->
                    Jugador(
                        id = j["id"] as String,
                        nombre = j["nombre"] as String,
                        color = COLORES[i % COLORES.size]
                    )
                }
            val maxRondas = (snap.getLong("maxRondas") ?: config.maxRondas.toLong()).toInt()
            val inicial = MotorDomino.nuevaPartida(jugadores, maxRondas = maxRondas)
            tx.update(
                salaRef(), mapOf(
                    CAMPO_JUEGO to EstadoJuegoMapper.aMapa(inicial),
                    "estado" to EstadoSala.EN_CURSO,
                    "version" to FieldValue.increment(1),
                    "actualizadoEn" to FieldValue.serverTimestamp()
                )
            )
            true
        }.await()
    }

    /** Listener en tiempo real: cada cambio del documento llega a todos los jugadores. */
    private fun escuchar() {
        listener?.remove()
        listener = salaRef().addSnapshotListener { snap, err ->
            if (err != null) { _error.value = err.message; return@addSnapshotListener }
            if (snap == null || !snap.exists()) return@addSnapshotListener
            @Suppress("UNCHECKED_CAST")
            val juego = snap.get(CAMPO_JUEGO) as? Map<String, Any?> ?: return@addSnapshotListener
            val nuevo = EstadoJuegoMapper.desdeMapa(juego)
            _estado.value = nuevo
            if (nuevo.terminada && !historialGuardado) {
                historialGuardado = true
                scope.launch { runCatching { historial.guardar(codigoSala, nuevo, miId) } }
            }
        }
    }

    // ------------------------------------------------------------------
    // Jugadas: todas pasan por la misma transacción
    // ------------------------------------------------------------------

    override fun colocarFicha(movimiento: Movimiento) =
        jugar { e, yo -> MotorDomino.aplicarMovimiento(e, movimiento, yo) }

    override fun robarOPasar() =
        jugar { e, yo -> MotorDomino.robarOPasar(e, yo) }

    override fun usarPortal(origen: Posicion, destino: Posicion) =
        jugar { e, yo -> MotorDomino.usarPortal(e, origen, destino, yo) }

    override fun omitirPortal() =
        jugar { e, _ -> MotorDomino.omitirPortal(e) }

    /**
     * Lee → valida turno → aplica motor → escribe, todo atómico.
     * Si no es mi turno o la partida terminó, no escribe nada.
     */
    private fun jugar(accion: (EstadoJuego, String) -> EstadoJuego) {
        val yo = miId
        scope.launch {
            try {
                db.runTransaction { tx ->
                    val snap = tx.get(salaRef())
                    @Suppress("UNCHECKED_CAST")
                    val actual = EstadoJuegoMapper.desdeMapa(snap.get(CAMPO_JUEGO) as Map<String, Any?>)
                    if (actual.terminada || actual.jugadorEnTurno.id != yo) return@runTransaction false
                    val nuevo = accion(actual, yo)
                    if (nuevo == actual) return@runTransaction false
                    tx.update(
                        salaRef(), mapOf(
                            CAMPO_JUEGO to EstadoJuegoMapper.aMapa(nuevo),
                            "estado" to if (nuevo.terminada) EstadoSala.FINALIZADA else EstadoSala.EN_CURSO,
                            "version" to FieldValue.increment(1),
                            "actualizadoEn" to FieldValue.serverTimestamp()
                        )
                    )
                    true
                }.await()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // ------------------------------------------------------------------
    // Reiniciar / abandonar
    // ------------------------------------------------------------------

    /** Reparte de nuevo con los mismos jugadores; a todos les llega por el listener. */
    override fun reiniciar() {
        val c = config ?: return
        historialGuardado = false
        scope.launch {
            try { crearJuegoSiNoExiste(c, forzar = true) } catch (e: Exception) { _error.value = e.message }
        }
    }

    /**
     * Marca la partida como terminada para el rival (le salta la pantalla de
     * resultados) y quita el listener local.
     */
    override fun abandonar() {
        if (codigoSala.isBlank()) return
        val yo = auth.currentUser?.uid
        val nombre = _estado.value?.jugador(yo.orEmpty())?.nombre ?: "Un jugador"
        val ref = salaRef()
        listener?.remove()
        listener = null
        _estado.value = null
        scope.launch {
            runCatching {
                db.runTransaction { tx ->
                    val snap = tx.get(ref)
                    @Suppress("UNCHECKED_CAST")
                    val juego = snap.get(CAMPO_JUEGO) as? Map<String, Any?>
                    if (juego == null || juego["fase"] == FaseJuego.FINALIZADA.name) return@runTransaction false
                    tx.update(
                        ref, mapOf(
                            "$CAMPO_JUEGO.fase" to FaseJuego.FINALIZADA.name,
                            "$CAMPO_JUEGO.mensaje" to "$nombre abandonó la partida.",
                            "estado" to EstadoSala.ABANDONADA,
                            "actualizadoEn" to FieldValue.serverTimestamp()
                        )
                    )
                    true
                }.await()
            }
        }
        codigoSala = ""
    }

    companion object {
        const val CAMPO_JUEGO = "juego"
        private val COLORES = listOf(ColoresJugador.JUGADOR_1, ColoresJugador.JUGADOR_2, 0xFFE65100, 0xFF8E24AA)
    }
}
