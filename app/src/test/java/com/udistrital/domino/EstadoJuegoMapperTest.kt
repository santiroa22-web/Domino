package com.udistrital.domino

import com.udistrital.domino.data.EstadoJuegoMapper
import com.udistrital.domino.logica.Jugador
import com.udistrital.domino.logica.MotorDomino
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Garantiza que lo que se sube a Firestore vuelve idéntico. Si este test falla,
 * los dos dispositivos verían partidas distintas.
 */
class EstadoJuegoMapperTest {

    private val jugadores = listOf(
        Jugador("uidA", "Ana", 0xFF4CAF50),
        Jugador("uidB", "Beto", 0xFF1565C0)
    )

    /** Simula lo que hace Firestore: los Int vuelven como Long. */
    private fun comoFirestore(v: Any?): Any? = when (v) {
        is Int -> v.toLong()
        is Map<*, *> -> v.mapValues { comoFirestore(it.value) }
        is List<*> -> v.map { comoFirestore(it) }
        else -> v
    }

    @Suppress("UNCHECKED_CAST")
    private fun idaYVuelta(e: com.udistrital.domino.logica.EstadoJuego) =
        EstadoJuegoMapper.desdeMapa(comoFirestore(EstadoJuegoMapper.aMapa(e)) as Map<String, Any?>)

    @Test
    fun estadoInicialSobreviveIdaYVuelta() {
        val inicial = MotorDomino.nuevaPartida(jugadores, seed = 42)
        assertEquals(inicial, idaYVuelta(inicial))
    }

    @Test
    fun estadoConJugadasSobreviveIdaYVuelta() {
        var e = MotorDomino.nuevaPartida(jugadores, seed = 7)
        // Juega varias jugadas reales con la IA para tener fichas en el tablero.
        val ia = com.udistrital.domino.logica.EstrategiaIA(kotlin.random.Random(1))
        repeat(6) {
            if (e.terminada) return@repeat
            val id = e.jugadorEnTurno.id
            e = if (e.accionPortalPendiente) MotorDomino.omitirPortal(e)
            else ia.mejorMovimiento(e, id)?.let { MotorDomino.aplicarMovimiento(e, it, id) }
                ?: MotorDomino.robarOPasar(e, id)
        }
        assertTrue(e.hayFichasEnTablero())
        assertEquals(e, idaYVuelta(e))
    }

    @Test
    fun tableroSeGuardaPlanoSinListasAnidadas() {
        val mapa = EstadoJuegoMapper.aMapa(MotorDomino.nuevaPartida(jugadores, seed = 3))
        val tablero = mapa["tablero"] as List<*>
        assertEquals(25, tablero.size)
        assertFalse("Firestore no admite arrays anidados", tablero.any { it is List<*> })
    }

    @Test
    fun guardaPuntajesYTurnoParaLaConsola() {
        val e = MotorDomino.nuevaPartida(jugadores, seed = 5)
        val mapa = EstadoJuegoMapper.aMapa(e)
        assertEquals(e.jugadorEnTurno.id, mapa["turnoDe"])
        assertEquals(setOf("uidA", "uidB"), (mapa["puntajes"] as Map<*, *>).keys)
    }
}
